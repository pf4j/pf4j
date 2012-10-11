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
package org.pf4j;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/**
 * Using Sezpoz(http://sezpoz.java.net/) for extensions discovery.
 * 
 * @author Decebal Suiu
 */
public class DefaultExtensionFinder implements ExtensionFinder {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultExtensionFinder.class);
	
	private volatile List<IndexItem<Extension, Object>> indices;
	private ClassLoader classLoader;
	
	public DefaultExtensionFinder(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}
	
	@Override
	public <T> List<ExtensionWrapper<T>> find(Class<T> type) {
		LOG.debug("Find extensions for " + type);
        List<ExtensionWrapper<T>> result = new ArrayList<ExtensionWrapper<T>>();
		getIndices();
//		System.out.println("indices =  "+ indices);
		for (IndexItem<Extension, Object> item : indices) {
            try {
            	AnnotatedElement element = item.element();
            	Class<?> extensionType = (Class<?>) element;
            	LOG.debug("Checking extension type " + extensionType);
            	if (type.isAssignableFrom(extensionType)) {
                    Object instance = item.instance();
                    if (instance != null) {
                		LOG.debug("Added extension " + extensionType);
						result.add(new ExtensionWrapper<T>(type.cast(instance), item.annotation().ordinal()));
                    }
                }
            } catch (InstantiationException e) {
            	LOG.error(e.getMessage(), e);
			}
		}
		
		return result;
	}

	 private List<IndexItem<Extension, Object>> getIndices() {
         if (indices == null) {
             indices = new ArrayList<IndexItem<Extension, Object>>(); 
             Iterator<IndexItem<Extension, Object>> it = Index.load(Extension.class, Object.class, classLoader).iterator();
             while (it.hasNext()) {
            	 indices.add(it.next());
             }
         }
         
         return indices;
     }
	 
}
