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

import java.util.EventObject;

/**
 * Event object that indicates a change in the state of a plugin.
 * The event is propagated to all registered listeners.
 * The event source is the {@link PluginManager} that changed the state of the plugin.
 * The event object contains the plugin that changed its state and the old state.
 *
 * @see PluginStateListener
 * @author Decebal Suiu
 */
public class PluginStateEvent extends EventObject {

    private PluginWrapper plugin;
    private PluginState oldState;

    public PluginStateEvent(PluginManager source, PluginWrapper plugin, PluginState oldState) {
        super(source);

        this.plugin = plugin;
        this.oldState = oldState;
    }

    /**
     * The object on which the Event initially occurred.
     *
     * @return the PluginManager that changed the state of the plugin
     */
    @Override
    public PluginManager getSource() {
        return (PluginManager) super.getSource();
    }

    /**
     * The plugin that changed its state.
     *
     * @return the plugin that changed its state
     */
    public PluginWrapper getPlugin() {
        return plugin;
    }

    /**
     * The new state of the plugin.
     *
     * @return the new state of the plugin
     */
    public PluginState getPluginState() {
        return plugin.getPluginState();
    }

    /**
     * The old state of the plugin.
     *
     * @return the old state of the plugin
     */
    public PluginState getOldState() {
        return oldState;
    }

    @Override
    public String toString() {
        return "PluginStateEvent [plugin=" + plugin.getPluginId() +
                ", newState=" + getPluginState() +
                ", oldState=" + oldState +
                ']';
    }

}
