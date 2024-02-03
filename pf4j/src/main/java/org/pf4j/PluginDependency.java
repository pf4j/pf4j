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

import java.util.Objects;

/**
 * A plugin dependency is a dependency that the plugin has on another plugin.
 * <p>
 * The dependency is defined by the plugin id and the version of the plugin that is required.
 * <p>
 * A dependency is considered as optional, if the plugin id ends with a question mark.
 * For example, the plugin id "my-plugin?" is considered as optional.
 * <p>
 * The plugin id and the version are separated by the '@' character.
 * For example, the dependency "my-plugin@1.0.0" means that the plugin "my-plugin" with version "1.0.0" is required.
 * If the version is not specified, then the plugin is required with any version.
 * For example, the dependency "my-plugin" means that the plugin "my-plugin" with any version is required.
 *
 * @see VersionManager
 * @author Decebal Suiu
 */
public class PluginDependency {

    private String pluginId;
    private String pluginVersionSupport = "*";
    private final boolean optional;

    public PluginDependency(String dependency) {
        int index = dependency.indexOf('@');
        if (index == -1) {
            this.pluginId = dependency;
        } else {
            this.pluginId = dependency.substring(0, index);
            if (dependency.length() > index + 1) {
                this.pluginVersionSupport = dependency.substring(index + 1);
            }
        }

        // A dependency is considered as optional, if the plugin id ends with a question mark.
        this.optional = this.pluginId.endsWith("?");
        if (this.optional) {
            this.pluginId = this.pluginId.substring(0, this.pluginId.length() - 1);
        }
    }

    /**
     * Returns the unique identifier of the plugin.
     *
     * @return the plugin id
     */
    public String getPluginId() {
        return pluginId;
    }

    /**
     * Returns the version of the plugin that is required.
     *
     * @return the version of the plugin that is required
     */
    public String getPluginVersionSupport() {
        return pluginVersionSupport;
    }

    /**
     * Returns {@code true} if the dependency is optional, {@code false} otherwise.
     *
     * @return {@code true} if the dependency is optional, {@code false} otherwise
     */
    public boolean isOptional() {
        return optional;
    }

    @Override
    public String toString() {
        return "PluginDependency [pluginId=" + pluginId + ", pluginVersionSupport="
            + pluginVersionSupport + ", optional="
            + optional + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PluginDependency)) return false;
        PluginDependency that = (PluginDependency) o;
        return optional == that.optional &&
            pluginId.equals(that.pluginId) &&
            pluginVersionSupport.equals(that.pluginVersionSupport);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginId, pluginVersionSupport, optional);
    }

}
