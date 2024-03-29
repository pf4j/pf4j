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
 * An extension point is a formal declaration in a plugin (or in application API) where customization is allowed.
 * It's a place where custom code can be "plugged in".
 * <p>
 * An extension point is defined by an interface or an abstract class.
 * The extension point is used by the application to discover and use the custom implementations.
 *
 * @author Decebal Suiu
 */
public interface ExtensionPoint {

}
