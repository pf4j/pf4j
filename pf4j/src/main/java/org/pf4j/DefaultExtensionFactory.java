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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default implementation for {@link ExtensionFactory}.
 * It uses {@link Class#newInstance} method.
 *
 * @author Decebal Suiu
 */
public class DefaultExtensionFactory implements ExtensionFactory {

    private static final Logger log = LoggerFactory.getLogger(DefaultExtensionFactory.class);

    /**
     * Creates an extension instance.
     */
    @Override
    public <T> T create(Class<T> extensionClass) throws PluginException {
        log.debug("Create instance for extension '{}'", extensionClass.getName());
        try {
            return extensionClass.newInstance();
        } catch (Exception e) {
            throw new PluginException(e);
        }
    }

}
