/*
 * Copyright 2012 Decebal Suiu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
 * the License. You may obtain a copy of the License in the LICENSE file, or at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ro.fortsoft.pf4j;

import java.util.List;
import java.util.Set;

/**
 * @author Decebal Suiu
 */
public interface ExtensionFinder {

	/**
	 * Retrieves a list of all extensions found for the extension point.
	 * 
	 * @param extensionPoint
	 *          An extension point.
	 * @return A list of all extensions found for the extension point
	 */
	public <T> List<ExtensionWrapper<T>> find(Class<T> extensionPoint);

	/**
	 * Retrieves a list of all extension class names found for a plug-in.
	 * 
	 * @param pluginId
	 *          The unique ID of a plug-in.
	 * @return A list of all extension class names found for a plug-in.
	 */
	public Set<String> findClassNames(String pluginId);

}
