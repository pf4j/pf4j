/*
 * Copyright 2013 Decebal Suiu
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
package ro.fortsoft.pf4j.util;

import ro.fortsoft.pf4j.PluginDescriptor;
import ro.fortsoft.pf4j.PluginException;

/**
 * Various utility methods for descriptors
 */
public class PluginDescriptorUtils {
    public static void simpleValidation(PluginDescriptor descriptor) throws PluginException {
        if (StringUtils.isEmpty(descriptor.getPluginId())) {
            throw new PluginException("id cannot be empty");
        }
        if (StringUtils.isEmpty(descriptor.getPluginClass())) {
            throw new PluginException("class cannot be empty");
        }
        if (descriptor.getVersion() == null) {
            throw new PluginException("version cannot be empty");
        }
    }
}
