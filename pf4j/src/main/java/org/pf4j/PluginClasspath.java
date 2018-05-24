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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * The classpath of the plugin.
 * It contains {@code classes} directories and {@code lib} directories (directories that contains jars).
 *
 * @author Decebal Suiu
 */
public class PluginClasspath {

    private Set<String> classesDirectories = new HashSet<>();
    private Set<String> libDirectories = new HashSet<>();

    public Set<String> getClassesDirectories() {
        return classesDirectories;
    }

    public PluginClasspath addClassesDirectories(String... classesDirectories) {
        return addClassesDirectories(Arrays.asList(classesDirectories));
    }

    public PluginClasspath addClassesDirectories(Collection<String> classesDirectories) {
        this.classesDirectories.addAll(classesDirectories);

        return this;
    }

    public Set<String> getLibDirectories() {
        return libDirectories;
    }

    public PluginClasspath addLibDirectories(String... libDirectories) {
        return addLibDirectories(Arrays.asList(libDirectories));
    }

    public PluginClasspath addLibDirectories(Collection<String> libDirectories) {
        this.libDirectories.addAll(libDirectories);

        return this;
    }

}
