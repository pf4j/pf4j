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
package org.pf4j;

/**
 * This class provides access to the current Pf4j version which might otherwise not be possible:
 * e.g. in Uber-Jars where the Manifest was merged and the Pf4j info was overridden
 * @author Wolfram Haussig
 */
public class Pf4jInfo {
    /**
     * the current Pf4j version
     */
    public static final String VERSION = "${project.version}";
}
