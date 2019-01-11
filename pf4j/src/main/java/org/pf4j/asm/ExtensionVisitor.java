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

package org.pf4j.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.pf4j.Extension;

import java.util.Arrays;

/**
 * This visitor extracts an {@link ExtensionInfo} from any class,
 * that holds an {@link Extension} annotation.
 * <p>
 * The annotation parameters are extracted from byte code by using the
 * <a href="https://asm.ow2.io/">ASM library</a>. This makes it possible to
 * access the {@link Extension} parameters without loading the class into
 * the class loader. This avoids possible {@link NoClassDefFoundError}'s
 * for extensions, that can't be loaded due to missing dependencies.
 *
 * @author Andreas Rudolph
 * @author Decebal Suiu
 */
class ExtensionVisitor extends ClassVisitor {
    //private static final Logger log = LoggerFactory.getLogger(ExtensionVisitor.class);
    private static final int ASM_VERSION = Opcodes.ASM7;
    private final ExtensionInfo extensionInfo;

    ExtensionVisitor(ExtensionInfo extensionInfo) {
        super(ASM_VERSION);
        this.extensionInfo = extensionInfo;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        //if (!descriptor.equals("Lorg/pf4j/Extension;")) {
        if (!Type.getType(descriptor).getClassName().equals(Extension.class.getName())) {
            return super.visitAnnotation(descriptor, visible);
        }

        return new AnnotationVisitor(ASM_VERSION) {

            @Override
            public AnnotationVisitor visitArray(final String name) {
                if ("ordinal".equals(name) || "plugins".equals(name) || "points".equals(name)) {
                    return new AnnotationVisitor(ASM_VERSION, super.visitArray(name)) {

                        @Override
                        public void visit(String key, Object value) {
                            //log.debug("Load annotation attribute {} = {} ({})", name, value, value.getClass().getName());

                            if ("ordinal".equals(name)) {
                                extensionInfo.ordinal = Integer.parseInt(value.toString());
                            } else if ("plugins".equals(name)) {
                                if (value instanceof String) {
                                    //log.debug("found plugin " + value);
                                    extensionInfo.plugins.add((String) value);
                                } else if (value instanceof String[]) {
                                    //log.debug("found plugins " + Arrays.toString((String[]) value));
                                    extensionInfo.plugins.addAll(Arrays.asList((String[]) value));
                                } else {
                                    //log.debug("found plugin " + value.toString());
                                    extensionInfo.plugins.add(value.toString());
                                }
                            } else if ("points".equals(name)) {
                                String pointClassName = ((Type) value).getClassName();
                                //log.debug("found point " + pointClassName);
                                extensionInfo.points.add(pointClassName);
                            }

                            super.visit(key, value);
                        }
                    };
                }

                return super.visitArray(name);
            }
        };
    }
}
