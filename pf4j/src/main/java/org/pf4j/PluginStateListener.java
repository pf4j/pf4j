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

import java.util.EventListener;

/**
 * Defines the interface for an object that listens to plugin state changes.
 * <p>
 * The class that is interested in processing a plugin state event implements this interface.
 *
 * @see PluginStateEvent
 * @author Decebal Suiu
 */
public interface PluginStateListener extends EventListener {

    /**
     * Invoked when a plugin's state (for example {@link PluginState#DISABLED}, {@link PluginState#STARTED}) is changed.
     *
     * @param event the plugin state event
     */
    void pluginStateChanged(PluginStateEvent event);

}
