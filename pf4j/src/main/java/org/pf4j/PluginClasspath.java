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
import java.util.Objects;
import java.util.Set;

/**
 * The classpath of the plugin.
 * <p>
 * It contains {@code classes} directories (directories that contain classes files)
 * and {@code jars} directories (directories that contain jars files).
 * <p>
 * The classpath is used to create the {@link ClassLoader} for the plugin.
 *
 * @author Decebal Suiu
 */
public class PluginClasspath {

    private final Set<String> classesDirectories = new HashSet<>();
    private final Set<String> jarsDirectories = new HashSet<>();

    /**
     * Get the classes directories.
     *
     * @return a set of directories that contain classes files
     */
    public Set<String> getClassesDirectories() {
        return classesDirectories;
    }

    /**
     * Add classes directories.
     *
     * @param classesDirectories a set of directories that contain classes files
     * @return this object for chaining
     */
    public PluginClasspath addClassesDirectories(String... classesDirectories) {
        return addClassesDirectories(Arrays.asList(classesDirectories));
    }

    /**
     * Add classes directories.
     *
     * @param classesDirectories a collection of directories that contain classes files
     * @return this object for chaining
     */
    public PluginClasspath addClassesDirectories(Collection<String> classesDirectories) {
        this.classesDirectories.addAll(classesDirectories);

        return this;
    }

    /**
     * Get the jars directories.
     *
     * @return a set of directories that contain jars files
     */
    public Set<String> getJarsDirectories() {
        return jarsDirectories;
    }

    /**
     * Add jars directories.
     *
     * @param jarsDirectories a set of directories that contain jars files
     * @return this object for chaining
     */
    public PluginClasspath addJarsDirectories(String... jarsDirectories) {
        return addJarsDirectories(Arrays.asList(jarsDirectories));
    }

    /**
     * Add jars directories.
     *
     * @param jarsDirectories a collection of directories that contain jars files
     * @return this object for chaining
     */
    public PluginClasspath addJarsDirectories(Collection<String> jarsDirectories) {
        this.jarsDirectories.addAll(jarsDirectories);

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PluginClasspath)) return false;
        PluginClasspath that = (PluginClasspath) o;
        return classesDirectories.equals(that.classesDirectories) &&
            jarsDirectories.equals(that.jarsDirectories);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classesDirectories, jarsDirectories);
    }

}
