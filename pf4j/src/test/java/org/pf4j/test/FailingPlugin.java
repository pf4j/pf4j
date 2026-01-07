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
 * A {@link Plugin} that throws an exception when started.
 * Used for testing exception handling in plugin lifecycle.
 *
 * @author Decebal Suiu
 */
public class FailingPlugin extends Plugin {

    public FailingPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public void start() {
        throw new RuntimeException("Intentional failure for testing");
    }

}