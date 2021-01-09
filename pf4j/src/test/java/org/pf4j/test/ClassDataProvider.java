/*
 * Copyright (C) 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pf4j.test;

/**
 * Defines the interface for classes that know to supply class data for a class name.
 * The idea is to have the possibility to retrieve the data for a class from different sources:
 * <ul>
 * <li>Class path - the class is already loaded by the class loader</li>
 * <li>String - the string (the source code) is compiled dynamically via {@link javax.tools.JavaCompiler}</>
 * <li>Generate the source code programmatically using something like {@code https://github.com/square/javapoet}</li>
 * </ul>
 *
 * @author Decebal Suiu
 */
public interface ClassDataProvider {

    byte[] getClassData(String className);

}
