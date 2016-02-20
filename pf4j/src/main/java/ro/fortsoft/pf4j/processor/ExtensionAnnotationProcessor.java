/*
 * Copyright 2013 Decebal Suiu
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
package ro.fortsoft.pf4j.processor;

import ro.fortsoft.pf4j.Extension;
import ro.fortsoft.pf4j.ExtensionPoint;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Decebal Suiu
 */
public class ExtensionAnnotationProcessor extends AbstractProcessor {

    private static final String STORAGE_CLASS_NAME = "pf4j.storageClassName";

    private Map<String, Set<String>> extensions = new HashMap<>(); // the key is the extension point
    private Map<String, Set<String>> oldExtensions = new HashMap<>(); // the key is the extension point

    private ExtensionStorage storage;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        info("%s%.init()", ExtensionAnnotationProcessor.class);
        storage = createStorage();
    }

    @Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latest();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		Set<String> annotationTypes = new HashSet<>();
        annotationTypes.add(Extension.class.getName());

        return annotationTypes;
	}

    @Override
    public Set<String> getSupportedOptions() {
        Set<String> options = new HashSet<>();
        options.add(STORAGE_CLASS_NAME);

        return options;
    }

    @Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.processingOver()) {
            return false;
        }

        info("Processing @%s%", Extension.class);
		for (Element element : roundEnv.getElementsAnnotatedWith(Extension.class)) {
            // check if @Extension is put on class and not on method or constructor
            if (!(element instanceof TypeElement)) {
				continue;
			}

            // check if class extends/implements an extension point
            if (!isExtension(element.asType())) {
                continue;
            }

            TypeElement extensionElement = (TypeElement) element;
//            Extension annotation = element.getAnnotation(Extension.class);
            List<TypeElement> extensionPointElements = findExtensionPoints(extensionElement);
            if (extensionPointElements.isEmpty()) {
                // TODO throw error ?
                continue;
            }

            String extension = getBinaryName(extensionElement);
            for (TypeElement extensionPointElement : extensionPointElements) {
                String extensionPoint = getBinaryName(extensionPointElement);
                Set<String> extensionPoints = extensions.get(extensionPoint);
                if (extensionPoints == null) {
                    extensionPoints = new TreeSet<>();
                    extensions.put(extensionPoint, extensionPoints);
                }
                extensionPoints.add(extension);
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

    private List<TypeElement> findExtensionPoints(TypeElement extensionElement) {
        List<TypeElement> extensionPointElements = new ArrayList<>();

        // search in interfaces
        for (TypeMirror item : extensionElement.getInterfaces()) {
            boolean isExtensionPoint = processingEnv.getTypeUtils().isSubtype(item, getExtensionPointType());
            if (isExtensionPoint) {
                TypeElement extensionPointElement = (TypeElement) ((DeclaredType) item).asElement();
                extensionPointElements.add(extensionPointElement);
            }
        }

        // search in superclass
        TypeMirror superclass = extensionElement.getSuperclass();
        if (superclass.getKind() != TypeKind.NONE) {
            boolean isExtensionPoint = processingEnv.getTypeUtils().isSubtype(superclass, getExtensionPointType());
            if (isExtensionPoint) {
                TypeElement extensionPointElement = (TypeElement) ((DeclaredType) superclass).asElement();
                extensionPointElements.add(extensionPointElement);
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

    private ExtensionStorage createStorage() {
        ExtensionStorage storage = null;

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

        return storage;
    }

}
