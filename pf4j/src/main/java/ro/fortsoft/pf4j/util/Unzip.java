/*
 * Copyright 2012 Decebal Suiu
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class extracts the containt of the plugin archive into a directory.
 * It's a class for only the internal use.
 *
 * @author Decebal Suiu
 */
public class Unzip {

	private static final Logger log = LoggerFactory.getLogger(Unzip.class);

    /**
     * Holds the destination directory.
     * File will be unzipped into the destination directory.
     */
    private File destination;

    /**
     * Holds path to zip file.
     */
    private File source;

    public Unzip() {
    }

    public Unzip(File source, File destination) {
        this.source = source;
        this.destination = destination;
    }

    public void setSource(File source) {
        this.source = source;
    }

    public void setDestination(File destination) {
        this.destination = destination;
    }

    public void extract() throws IOException {
        log.debug("Extract content of '{}' to '{}'", source, destination);

        // delete destination file if exists
        removeDirectory(destination);

    	ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(source));
	    ZipEntry zipEntry = null;

    	while ((zipEntry = zipInputStream.getNextEntry()) != null) {
	        try {
		        File file = new File(destination, zipEntry.getName());

        		// create intermediary directories - sometimes zip don't add them
        		File dir = new File(file.getParent());
        		dir.mkdirs();

        		if (zipEntry.isDirectory()) {
		            file.mkdirs();
        		} else {
		            byte[] buffer = new byte[1024];
        		    int length = 0;
		            FileOutputStream fos = new FileOutputStream(file);

        		    while ((length = zipInputStream.read(buffer)) >= 0) {
		            	fos.write(buffer, 0, length);
        		    }

		            fos.close();
                }
    	    } catch (FileNotFoundException e) {
    	    	log.error("File '{}' not found", zipEntry.getName());
    	    }
	    }

    	zipInputStream.close();
    }

    private boolean removeDirectory(File directory) {
        if (!directory.exists()) {
            return true;
        }

        if (!directory.isDirectory()) {
            return false;
        }

        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                removeDirectory(file);
            } else {
                file.delete();
            }
        }

        return directory.delete();
    }

}
