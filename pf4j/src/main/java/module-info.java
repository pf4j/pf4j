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

/**
 * Module descriptor for PF4J.
 *
 * @author Decebal Suiu
 * @author Andreas Rudolph
 */
module org.pf4j {
    requires java.base;

    // provides javax.annotation
    requires java.compiler;

    // provided by the ASM library
    requires org.objectweb.asm;

    // The SLF4J library currently does not provide a module.
    // Version 1.8 provides a module called "org.slf4j". But this version is
    // currently in beta stage. Therefore I'm not sure, if we already like to
    // use it.
    requires org.slf4j;

    // The java-semver library currently does not provide a module.
    // Maybe we should send them a pull request, that at least they provide an
    // automatic module name in their MANIFEST file.
    requires java.semver;

    // Maybe we should reconsider the package hierarchy, that only classes are
    // exported, which are required by 3rd party developers.
    exports org.pf4j;
    exports org.pf4j.processor;
}
