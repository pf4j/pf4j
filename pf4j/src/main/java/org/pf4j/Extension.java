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
package org.pf4j;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An extension is a class that extends an extension point.
 * Use this annotation to mark a class as an extension.
 * The extension class must implement the extension point interface or extend the extension point abstract class.
 *
 * @author Decebal Suiu
 */
@Retention(RUNTIME)
@Target(TYPE)
@Inherited
@Documented
public @interface Extension {

    /**
     * The order of the extension.
     * The ordinal is used to sort the extensions.
     *
     * @return the order of the extension
     */
    int ordinal() default 0;

    /**
     * An array of extension points, that are implemented by this extension.
     * This explicit configuration overrides the automatic detection of extension points in the
     * {@link org.pf4j.processor.ExtensionAnnotationProcessor}.
     * <p>
     * In case your extension is directly derived from an extension point this attribute is NOT required.
     * But under certain <a href="https://github.com/pf4j/pf4j/issues/264">more complex scenarios</a> it
     * might be useful to explicitly set the extension points for an extension.
     *
     * @return classes of extension points, that are implemented by this extension
     */
    Class<?>[] points() default {};

    /**
     * An array of plugin IDs, that have to be available in order to load this extension.
     * The {@link AbstractExtensionFinder} won't load this extension, if these plugins are not
     * available / started at runtime.
     * <p>
     * Notice: This feature requires the optional <a href="https://asm.ow2.io/">ASM library</a>
     * to be available on the applications classpath and has to be explicitly enabled via
     * {@link AbstractExtensionFinder#setCheckForExtensionDependencies(boolean)}.
     *
     * @return plugin IDs, that have to be available in order to load this extension
     */
    String[] plugins() default {};

}
