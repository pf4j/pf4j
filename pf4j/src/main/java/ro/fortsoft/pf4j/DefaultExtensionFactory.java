/*
 * Copyright 2014 Decebal Suiu
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default implementation for ExtensionFactory.
 * It uses Class.newInstance() method.
 * 
 * @author Decebal Suiu
 */
public class DefaultExtensionFactory implements ExtensionFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExtensionFactory.class);

	/**
	 * Creates an extension instance. If an error occurs, then the error is logged and the method returns null.
	 * 
	 * @param extensionClass
	 *          An extension class.
	 * @return An extension class object or null if an error occurs.
	 */
	@Override
	public Object create(Class<?> extensionClass) {
		LOGGER.debug("Create an instance for extension '{}'", extensionClass.getName());
		try {
			return extensionClass.newInstance();
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		return null;
	}

}
