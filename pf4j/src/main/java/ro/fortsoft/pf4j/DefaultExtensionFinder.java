/*
 * Copyright 2013 Decebal Suiu
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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default implementation for ExtensionFinder.
 * All extensions declared in a plugin are indexed in a file "META-INF/extensions.idx".
 * This class lookup extensions in all extensions index files "META-INF/extensions.idx". 
 * 
 * @author Decebal Suiu
 */
public class DefaultExtensionFinder implements ExtensionFinder {

	private static final Logger log = LoggerFactory.getLogger(DefaultExtensionFinder.class);
	
	private ClassLoader classLoader;
	private ExtensionFactory extensionFactory;
	private volatile Set<String> entries;
	
	public DefaultExtensionFinder(ClassLoader classLoader) {
		this.classLoader = classLoader;
		this.extensionFactory = createExtensionFactory();
	}
	
	@Override
	public <T> List<ExtensionWrapper<T>> find(Class<T> type) {
		log.debug("Find extensions for extension point {}", type.getName());
        List<ExtensionWrapper<T>> result = new ArrayList<ExtensionWrapper<T>>();
        if (entries == null) {
        	entries = readIndexFiles();
        }
                
        for (String entry : entries) {
        	try {
        		Class<?> extensionType = classLoader.loadClass(entry);
        		log.debug("Checking extension type {}", extensionType.getName());
            	if (type.isAssignableFrom(extensionType)) {
                    Object instance = extensionFactory.create(extensionType);
                    if (instance != null) {
                		Extension extension = extensionType.getAnnotation(Extension.class);
                		log.debug("Added extension {} with ordinal {}", extensionType.getName(), extension.ordinal());
                		result.add(new ExtensionWrapper<T>(type.cast(instance), extension.ordinal()));
                    }
            	} else {
            		log.warn("{} is not an extension for extension point {}", extensionType.getName(), type.getName());
            	}
        	} catch (ClassNotFoundException e) {
        		log.error(e.getMessage(), e);        	
			}
        }
        
        if (entries.isEmpty()) {
        	log.debug("No extensions found for extension point {}", type.getName());
        } else {
        	log.debug("Found {} extensions for extension point {}", entries.size(), type.getName());
        }

        // sort by "ordinal" property
        Collections.sort(result);
		
		return result;
	}
	
	/**
     * Add the possibility to override the ExtensionFactory.
     * The default implementation uses Class.newInstance() method.
     */
	protected ExtensionFactory createExtensionFactory() {
		return new ExtensionFactory() {
			
			@Override
			public Object create(Class<?> extensionType) {
				log.debug("Create instance for extension {}", extensionType.getName());
				
				try {
					return extensionType.newInstance();
				} catch (InstantiationException e) {
					log.error(e.getMessage(), e);
				} catch (IllegalAccessException e) {
					log.error(e.getMessage(), e);
				}
				
				return null;
			}
			
		};
	}
	
	private Set<String> readIndexFiles() {
		log.debug("Reading extensions index files");
		Set<String> entries = new HashSet<String>();
		
		try {
			Enumeration<URL> indexFiles = classLoader.getResources(ExtensionsIndexer.EXTENSIONS_RESOURCE);
			while (indexFiles.hasMoreElements()) {
				Reader reader = new InputStreamReader(indexFiles.nextElement().openStream(), "UTF-8");
				ExtensionsIndexer.readIndex(reader, entries);
			}
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}			

        if (entries.isEmpty()) {
        	log.debug("No extensions found");
        } else {
        	log.debug("Found possible {} extensions", entries.size());
        }

		return entries;
	}	
	
	/**
	 * Creates an extension instance.
	 */
	public static interface ExtensionFactory {

		public Object create(Class<?> extensionType);
		
	}

}
