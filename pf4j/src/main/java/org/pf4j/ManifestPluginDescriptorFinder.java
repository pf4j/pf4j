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

import org.pf4j.util.FileUtils;
import org.pf4j.util.StringUtils;
import org.pf4j.util.io.NamePathFilter;
import org.pf4j.util.io.PathFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Read the plugin descriptor from the manifest file.
 *
 * @author Decebal Suiu
 */
public class ManifestPluginDescriptorFinder implements PluginDescriptorFinder {

    private static final Logger log = LoggerFactory.getLogger(ManifestPluginDescriptorFinder.class);

    public static final String PLUGIN_ID = "Plugin-Id";
    public static final String PLUGIN_DESCRIPTION = "Plugin-Description";
    public static final String PLUGIN_CLASS = "Plugin-Class";
    public static final String PLUGIN_VERSION = "Plugin-Version";
    public static final String PLUGIN_PROVIDER = "Plugin-Provider";
    public static final String PLUGIN_DEPENDENCIES = "Plugin-Dependencies";
    public static final String PLUGIN_REQUIRES = "Plugin-Requires";
    public static final String PLUGIN_LICENSE = "Plugin-License";

    private static final PathFilter MANIFEST_PATH = new NamePathFilter("MANIFEST.MF", true);

    @Override
    public boolean isApplicable(Path pluginPath) {
        return Files.exists(pluginPath) && (Files.isDirectory(pluginPath) || FileUtils.isZipOrJarFile(pluginPath));
    }

    @Override
    public PluginDescriptor find(Path pluginPath) {
        Manifest manifest = readManifest(pluginPath);

        return createPluginDescriptor(manifest);
    }

    protected Manifest readManifest(Path pluginPath) {
        if (FileUtils.isJarFile(pluginPath)) {
            return readManifestFromJar(pluginPath);
        }

        if (FileUtils.isZipFile(pluginPath)) {
            return readManifestFromZip(pluginPath);
        }

        return readManifestFromDirectory(pluginPath);
    }

    protected PluginDescriptor createPluginDescriptor(Manifest manifest) {
        DefaultPluginDescriptor pluginDescriptor = createPluginDescriptorInstance();

        // TODO validate !!!
        Attributes attributes = manifest.getMainAttributes();
        String id = attributes.getValue(PLUGIN_ID);
        pluginDescriptor.setPluginId(id);

        String description = attributes.getValue(PLUGIN_DESCRIPTION);
        if (StringUtils.isNullOrEmpty(description)) {
            pluginDescriptor.setPluginDescription("");
        } else {
            pluginDescriptor.setPluginDescription(description);
        }

        String clazz = attributes.getValue(PLUGIN_CLASS);
        if (StringUtils.isNotNullOrEmpty(clazz)) {
            pluginDescriptor.setPluginClass(clazz);
        }

        String version = attributes.getValue(PLUGIN_VERSION);
        if (StringUtils.isNotNullOrEmpty(version)) {
            pluginDescriptor.setPluginVersion(version);
        }

        String provider = attributes.getValue(PLUGIN_PROVIDER);
        pluginDescriptor.setProvider(provider);
        String dependencies = attributes.getValue(PLUGIN_DEPENDENCIES);
        pluginDescriptor.setDependencies(dependencies);

        String requires = attributes.getValue(PLUGIN_REQUIRES);
        if (StringUtils.isNotNullOrEmpty(requires)) {
            pluginDescriptor.setRequires(requires);
        }

        pluginDescriptor.setLicense(attributes.getValue(PLUGIN_LICENSE));

        return pluginDescriptor;
    }

    protected DefaultPluginDescriptor createPluginDescriptorInstance() {
        return new DefaultPluginDescriptor();
    }

    protected Manifest readManifestFromJar(Path jarPath) {
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            return jar.getManifest();
        } catch (IOException e) {
            throw new PluginRuntimeException(e, "Cannot read manifest from {}", jarPath);
        }
    }

    protected Manifest readManifestFromZip(Path zipPath) {
        try (ZipFile zip = new ZipFile(zipPath.toFile())) {
            ZipEntry manifestEntry = zip.getEntry("classes/META-INF/MANIFEST.MF");
            try (InputStream manifestInput = zip.getInputStream(manifestEntry)) {
                return new Manifest(manifestInput);
            }
        } catch (IOException e) {
            throw new PluginRuntimeException(e, "Cannot read manifest from {}", zipPath);
        }
    }

    protected Manifest readManifestFromDirectory(Path pluginPath) {
        // legacy (the path is something like "classes/META-INF/MANIFEST.MF")
        Path manifestPath = FileUtils.findPaths(pluginPath,MANIFEST_PATH)
            .findAny()
            .orElseThrow(() -> new PluginRuntimeException("Cannot find the manifest path"));

        log.debug("Lookup plugin descriptor in '{}'", manifestPath);
        if (Files.notExists(manifestPath)) {
            throw new PluginRuntimeException("Cannot find '{}' path", manifestPath);
        }

        try (InputStream input = Files.newInputStream(manifestPath)) {
            return new Manifest(input);
        } catch (IOException e) {
            throw new PluginRuntimeException(e, "Cannot read manifest from {}", pluginPath);
        }
    }

}
