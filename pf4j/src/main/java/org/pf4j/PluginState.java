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

/**
 * The state of a plugin.
 * <p>
 * Lifecycle of a plugin:
 * <pre>
 * CREATED -> RESOLVED -> STARTED -> STOPPED -> UNLOADED
 * CREATED -> DISABLED
 * CREATED -> FAILED
 *
 * @author Decebal Suiu
 */
public enum PluginState {

    /**
     * The runtime knows the plugin is there. It knows about the plugin path, the plugin descriptor.
     */
    CREATED("CREATED"),

    /**
     * The plugin cannot be used.
     */
    DISABLED("DISABLED"),

    /**
     * The plugin is created. All the dependencies are created and resolved.
     * The plugin is ready to be started.
     */
    RESOLVED("RESOLVED"),

    /**
     * The {@link Plugin#start()} has executed. A started plugin may contribute extensions.
     */
    STARTED("STARTED"),

    /**
     * The {@link Plugin#stop()} has executed.
     */
    STOPPED("STOPPED"),

    /**
     * Plugin failed to start.
     */
    FAILED("FAILED"),

    /**
     * The plugin has been unloaded. After this event has been completed, the plugin's
     * {@link ClassLoader} will be closed.
     */
    UNLOADED("UNLOADED"),
    ;

    private final String status;

    PluginState(String status) {
        this.status = status;
    }

    /**
     * Returns {@code true} if the value is {@link #CREATED}.
     */
    public boolean isCreated() {
        return this == CREATED;
    }

    /**
     * Returns {@code true} if the value is {@link #DISABLED}.
     */
    public boolean isDisabled() {
        return this == DISABLED;
    }

    /**
     * Returns {@code true} if the value is {@link #RESOLVED}.
     */
    public boolean isResolved() {
        return this == RESOLVED;
    }

    /**
     * Returns {@code true} if the value is {@link #STARTED}.
     */
    public boolean isStarted() {
        return this == STARTED;
    }

    /**
     * Returns {@code true} if the value is {@link #STOPPED}.
     */
    public boolean isStopped() {
        return this == STOPPED;
    }

    /**
     * Returns {@code true} if the value is {@link #FAILED}.
     */
    public boolean isFailed() {
        return this == FAILED;
    }

    /**
     * Returns {@code true} if the value is {@link #UNLOADED}.
     */
    public boolean isUnloaded() {
        return this == UNLOADED;
    }

    public boolean equals(String status) {
        return this.status.equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return status;
    }

    /**
     * Parse a string to a {@link PluginState}.
     *
     * @param string the string to parse
     * @return the {@link PluginState} or null if the string is not a valid state
     */
    public static PluginState parse(String string) {
        for (PluginState status : values()) {
            if (status.equals(string)) {
                return status;
            }
        }

        return null;
    }

}
