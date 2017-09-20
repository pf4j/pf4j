/*
 * Copyright 2014 Decebal Suiu
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
 * A wrapper over extension instance.
 *
 * @author Decebal Suiu
 */
public class ExtensionWrapper<T> implements Comparable<ExtensionWrapper<T>> {

    ExtensionDescriptor descriptor;
    ExtensionFactory extensionFactory;
    T extension; // cache

	public ExtensionWrapper(ExtensionDescriptor descriptor) {
        this.descriptor = descriptor;
	}

	@SuppressWarnings("unchecked")
    public T getExtension() {
        if (extension == null) {
            extension = (T) extensionFactory.create(descriptor.getExtensionClass());
        }

        return extension;
	}

    public ExtensionDescriptor getDescriptor() {
        return descriptor;
    }

    public int getOrdinal() {
		return descriptor.getOrdinal();
	}

	@Override
	public int compareTo(ExtensionWrapper<T> o) {
		return (getOrdinal() - o.getOrdinal());
	}

    void setExtensionFactory(ExtensionFactory extensionFactory) {
        this.extensionFactory = extensionFactory;
    }

}
