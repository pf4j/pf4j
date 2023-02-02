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

import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

/**
 * A simple {@link Plugin}.
 * In real applications you don't need to create a plugin like this if you are not interested in lifecycle events.
 * {@code PF4J} will automatically create a plugin similar to this (empty / dummy) if no class plugin is specified.
 *
 * @author Mario Franco
 */
public class TestPlugin extends Plugin {

    public TestPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

}
