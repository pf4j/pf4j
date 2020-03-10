/*
 * Copyright (C) 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pf4j.processor;

import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;
import org.pf4j.util.ClassUtils;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Processes {@link Extension} annotations and generates an {@link ExtensionStorage}.
 * You can specify the concrete {@link ExtensionStorage} via processor's environment options
 * ({@link ProcessingEnvironment#getOptions()}) or system property.
 * In both variants the option/property name is {@code pf4j.storageClassName}.
 *
 * @author Decebal Suiu
 */
public class ExtensionAnnotationProcessor extends AbstractProcessor {

    private static final String STORAGE_CLASS_NAME = "pf4j.storageClassName";
    private static final String IGNORE_EXTENSION_POINT = "pf4j.ignoreExtensionPoint";

    private Map<String, Set<String>> extensions = new HashMap<>(); // the key is the extension point
    private Map<String, Set<String>> oldExtensions = new HashMap<>(); // the key is the extension point

    private ExtensionStorage storage;
    private boolean ignoreExtensionPoint;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        info("%s init", ExtensionAnnotationProcessor.class.getName());
        info("Options %s", processingEnv.getOptions());

        initStorage();
        initIgnoreExtensionPoint();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton("*");
    }

    @Override
    public Set<String> getSupportedOptions() {
        Set<String> options = new HashSet<>();
        options.add(STORAGE_CLASS_NAME);
        options.add(IGNORE_EXTENSION_POINT);

        return options;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }

        info("Processing @%s", Extension.class.getName());
        for (Element element : roundEnv.getElementsAnnotatedWith(Extension.class)) {
            if (element.getKind() != ElementKind.ANNOTATION_TYPE) {
                processExtensionElement(element);
            }
        }

        // collect nested extension annotations
        List<TypeElement> extensionAnnotations = new ArrayList<>();
        for (TypeElement annotation : annotations) {
            if (ClassUtils.getAnnotationMirror(annotation, Extension.class) != null) {
                extensionAnnotations.add(annotation);
            }
        }

        // process nested extension annotations
        for (TypeElement te : extensionAnnotations) {
            info("Processing @%s", te);
            for (Element element : roundEnv.getElementsAnnotatedWith(te)) {
                processExtensionElement(element);
            }
        }

        // read old extensions
        oldExtensions = storage.read();
        for (Map.Entry<String, Set<String>> entry : oldExtensions.entrySet()) {
            String extensionPoint = entry.getKey();
            if (extensions.containsKey(extensionPoint)) {
                extensions.get(extensionPoint).addAll(entry.getValue());
            } else {
                extensions.put(extensionPoint, entry.getValue());
            }
        }

        // write extensions
        storage.write(extensions);

        return false;
    }

    public ProcessingEnvironment getProcessingEnvironment() {
        return processingEnv;
    }

    public void error(String message, Object... args) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format(message, args));
    }

    public void error(Element element, String message, Object... args) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format(message, args), element);
    }

    public void info(String message, Object... args) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, String.format(message, args));
    }

    public void info(Element element, String message, Object... args) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, String.format(message, args), element);
    }

    public String getBinaryName(TypeElement element) {
        return processingEnv.getElementUtils().getBinaryName(element).toString();
    }

    public Map<String, Set<String>> getExtensions() {
        return extensions;
    }

    public Map<String, Set<String>> getOldExtensions() {
        return oldExtensions;
    }

    public ExtensionStorage getStorage() {
        return storage;
    }

    private void processExtensionElement(Element element) {
        // check if @Extension is put on class and not on method or constructor
        if (!(element instanceof TypeElement)) {
            error(element, "Put annotation only on classes (no methods, no fields)");
            return;
        }

        // check if class extends/implements an extension point
        if (!ignoreExtensionPoint && !isExtension(element.asType())) {
            error(element, "%s is not an extension (it doesn't implement ExtensionPoint)", element);
            return;
        }

        TypeElement extensionElement = (TypeElement) element;
        List<TypeElement> extensionPointElements = findExtensionPoints(extensionElement);
        if (extensionPointElements.isEmpty()) {
            error(element, "No extension points found for extension %s", extensionElement);
            return;
        }

        String extension = getBinaryName(extensionElement);
        for (TypeElement extensionPointElement : extensionPointElements) {
            String extensionPoint = getBinaryName(extensionPointElement);
            Set<String> extensionPoints = extensions.computeIfAbsent(extensionPoint, k -> new TreeSet<>());
            extensionPoints.add(extension);
        }
    }

    @SuppressWarnings("unchecked")
    private List<TypeElement> findExtensionPoints(TypeElement extensionElement) {
        List<TypeElement> extensionPointElements = new ArrayList<>();

        // use extension points, that were explicitly set in the extension annotation
        AnnotationValue annotatedExtensionPoints = ClassUtils.getAnnotationValue(extensionElement, Extension.class, "points");
        List<? extends AnnotationValue> extensionPointClasses = (annotatedExtensionPoints != null) ?
            (List<? extends AnnotationValue>) annotatedExtensionPoints.getValue() :
            null;
        if (extensionPointClasses != null && !extensionPointClasses.isEmpty()) {
            for (AnnotationValue extensionPointClass : extensionPointClasses) {
                String extensionPointClassName = extensionPointClass.getValue().toString();
                TypeElement extensionPointElement = processingEnv.getElementUtils().getTypeElement(extensionPointClassName);
                extensionPointElements.add(extensionPointElement);
            }
        }
        // detect extension points automatically, if they are not explicitly configured (default behaviour)
        else {
            // search in interfaces
            List<? extends TypeMirror> interfaces = extensionElement.getInterfaces();
            for (TypeMirror item : interfaces) {
                boolean isExtensionPoint = processingEnv.getTypeUtils().isSubtype(item, getExtensionPointType());
                if (isExtensionPoint) {
                    extensionPointElements.add(getElement(item));
                }
            }

            // search in superclass
            TypeMirror superclass = extensionElement.getSuperclass();
            if (superclass.getKind() != TypeKind.NONE) {
                boolean isExtensionPoint = processingEnv.getTypeUtils().isSubtype(superclass, getExtensionPointType());
                if (isExtensionPoint) {
                    extensionPointElements.add(getElement(superclass));
                }
            }

            // pickup the first interface
            if (extensionPointElements.isEmpty() && ignoreExtensionPoint) {
                if (interfaces.isEmpty()) {
                    error(extensionElement, "Cannot use %s as extension point with %s compiler arg (it doesn't implement any interface)",
                        extensionElement, IGNORE_EXTENSION_POINT);
                } else if (interfaces.size() == 1) {
                    extensionPointElements.add(getElement(interfaces.get(0)));
                } else {
                    error(extensionElement, "Cannot use %s as extension point with %s compiler arg (it implements multiple interfaces)",
                        extensionElement, IGNORE_EXTENSION_POINT);
                }
            }
        }

        return extensionPointElements;
    }

    private boolean isExtension(TypeMirror typeMirror) {
        return processingEnv.getTypeUtils().isAssignable(typeMirror, getExtensionPointType());
    }

    private TypeMirror getExtensionPointType() {
        return processingEnv.getElementUtils().getTypeElement(ExtensionPoint.class.getName()).asType();
    }

    @SuppressWarnings("unchecked")
    private void initStorage() {
        // search in processing options
        String storageClassName = processingEnv.getOptions().get(STORAGE_CLASS_NAME);
        if (storageClassName == null) {
            // search in system properties
            storageClassName = System.getProperty(STORAGE_CLASS_NAME);
        }

        if (storageClassName != null) {
            // use reflection to create the storage instance
            try {
                Class storageClass = getClass().getClassLoader().loadClass(storageClassName);
                Constructor constructor = storageClass.getConstructor(ExtensionAnnotationProcessor.class);
                storage = (ExtensionStorage) constructor.newInstance(this);
            } catch (Exception e) {
                error(e.getMessage());
            }
        }

        if (storage == null) {
            // default storage
            storage = new LegacyExtensionStorage(this);
        }
    }

    private void initIgnoreExtensionPoint() {
        // search in processing options and system properties
        ignoreExtensionPoint = getProcessingEnvironment().getOptions().containsKey(IGNORE_EXTENSION_POINT) ||
            System.getProperty(IGNORE_EXTENSION_POINT) != null;
    }

    private TypeElement getElement(TypeMirror typeMirror) {
        return (TypeElement) ((DeclaredType) typeMirror).asElement();
    }

}
