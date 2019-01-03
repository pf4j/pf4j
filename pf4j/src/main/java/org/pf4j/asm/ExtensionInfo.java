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

import org.objectweb.asm.ClassReader;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class holds the parameters of an {@link org.pf4j.Extension}
 * annotation defined for a certain class.
 *
 * @author Andreas Rudolph
 * @author Decebal Suiu
 */
public final class ExtensionInfo {
    private static final Logger log = LoggerFactory.getLogger(ExtensionInfo.class);
    private final String className;
    int ordinal = 0;
    List<String> plugins = new ArrayList<>();
    // TODO: enable after https://github.com/pf4j/pf4j/pull/265 was merged
    //List<String> points = new ArrayList<>();

    ExtensionInfo(String className) {
        super();
        this.className = className;
    }

    /**
     * Get the name of the class, for which extension info was created.
     *
     * @return absolute class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * Get the {@link Extension#ordinal()} value, that was assigned to the extension.
     *
     * @return ordinal value
     */
    public int getOrdinal() {
        return ordinal;
    }

    /**
     * Get the {@link Extension#plugins()} value, that was assigned to the extension.
     *
     * @return ordinal value
     */
    public List<String> getPlugins() {
        return Collections.unmodifiableList(plugins);
    }

    /*
     * Get the {@link Extension#points()} value, that was assigned to the extension.
     *
     * TODO: enable after https://github.com/pf4j/pf4j/pull/265 was merged
     *
     * @return ordinal value
     *
    public List<String> getPoints() {
        return Collections.unmodifiableList(points);
    }*/

    /**
     * Load an {@link ExtensionInfo} for a certain class.
     *
     * @param className absolute class name
     * @param classLoader class loader to access the class
     * @return the {@link ExtensionInfo}, if the class was annotated with an {@link Extension}, otherwise null
     */
    public static ExtensionInfo load(String className, ClassLoader classLoader) {
        try (InputStream input = classLoader.getResourceAsStream(className.replace('.', '/') + ".class")) {
            ExtensionInfo info = new ExtensionInfo(className);
            new ClassReader(input).accept(new ExtensionVisitor(info), ClassReader.SKIP_DEBUG);
            return info;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }
}
