/*
 * Copyright 2015 Decebal Suiu
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

import java.util.List;

/**
 * A plugin descriptor contains information about a plug-in obtained
 * from the manifest (META-INF) file.
 *
 * @author Decebal Suiu
 */
public interface PluginDescriptor {

    String getPluginId();

    String getPluginDescription();

    String getPluginClass();

    String getVersion();

    String getRequires();

    String getProvider();

    String getLicense();

    List<PluginDependency> getDependencies();
}
