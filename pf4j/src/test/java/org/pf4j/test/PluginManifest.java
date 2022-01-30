package org.pf4j.test;

import org.pf4j.ManifestPluginDescriptorFinder;
import org.pf4j.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * @author Decebal Suiu
 */
public class PluginManifest {

    private final String pluginId;
    private final String pluginClass;
    private final String pluginVersion;
    private final String pluginDescription;
    private final String pluginProvider;
    private final List<String> pluginDependencies;
    private final String pluginRequires;
    private final String pluginLicense;

    private Manifest manifest;

    protected PluginManifest(Builder builder) {
        this.pluginId = builder.pluginId;
        this.pluginClass = builder.pluginClass;
        this.pluginVersion = builder.pluginVersion;
        this.pluginDescription = builder.pluginDescription;
        this.pluginProvider = builder.pluginProvider;
        this.pluginDependencies = builder.pluginDependencies;
        this.pluginRequires = builder.pluginRequires;
        this.pluginLicense = builder.pluginLicense;
    }

    public String pluginId() {
        return pluginId;
    }

    public String pluginClass() {
        return pluginClass;
    }

    public String pluginVersion() {
        return pluginVersion;
    }

    public String pluginDescription() {
        return pluginDescription;
    }

    public String pluginProvider() {
        return pluginProvider;
    }

    public List<String> pluginDependencies() {
        return pluginDependencies;
    }

    public String pluginRequires() {
        return pluginRequires;
    }

    public String pluginLicense() {
        return pluginLicense;
    }

    public Manifest manifest() {
        if (manifest == null) {
            Map<String, String> map = new LinkedHashMap<>();
            map.put(ManifestPluginDescriptorFinder.PLUGIN_ID, pluginId);

            if (StringUtils.isNotNullOrEmpty(pluginVersion)) {
                map.put(ManifestPluginDescriptorFinder.PLUGIN_VERSION, pluginVersion);
            }

            if (StringUtils.isNotNullOrEmpty(pluginClass)) {
                map.put(ManifestPluginDescriptorFinder.PLUGIN_CLASS, pluginClass);
            }

            if (StringUtils.isNotNullOrEmpty(pluginDescription)) {
                map.put(ManifestPluginDescriptorFinder.PLUGIN_DESCRIPTION, pluginDescription);
            }

            if (StringUtils.isNotNullOrEmpty(pluginProvider)) {
                map.put(ManifestPluginDescriptorFinder.PLUGIN_PROVIDER, pluginProvider);
            }

            if (StringUtils.isNotNullOrEmpty(pluginProvider)) {
                map.put(ManifestPluginDescriptorFinder.PLUGIN_PROVIDER, pluginProvider);
            }

            if (pluginDependencies != null && !pluginDependencies.isEmpty()) {
                map.put(ManifestPluginDescriptorFinder.PLUGIN_DEPENDENCIES, String.join(",", pluginDependencies));
            }

            if (StringUtils.isNotNullOrEmpty(pluginRequires)) {
                map.put(ManifestPluginDescriptorFinder.PLUGIN_REQUIRES, pluginRequires);
            }


            if (StringUtils.isNotNullOrEmpty(pluginLicense)) {
                map.put(ManifestPluginDescriptorFinder.PLUGIN_LICENSE, pluginLicense);
            }

            manifest = new Manifest();
            Attributes attributes = manifest.getMainAttributes();
            attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0.0");
            for (Map.Entry<String, String> entry : map.entrySet()) {
                attributes.put(new Attributes.Name(entry.getKey()), entry.getValue());
            }
        }

        return manifest;
    }

    public static class Builder {

        private final String pluginId;

        private String pluginClass;
        private String pluginVersion;
        private String pluginDescription;
        private String pluginProvider;
        private List<String> pluginDependencies;
        private String pluginRequires;
        private String pluginLicense;

        public Builder(String pluginId) {
            this.pluginId = pluginId;
        }

        public Builder pluginClass(String pluginClass) {
            this.pluginClass = pluginClass;

            return this;
        }

        public Builder pluginVersion(String pluginVersion) {
            this.pluginVersion = pluginVersion;

            return this;
        }

        public Builder pluginDescription(String pluginDescription) {
            this.pluginDescription = pluginDescription;

            return this;
        }

        public Builder pluginProvider(String pluginProvider) {
            this.pluginProvider = pluginProvider;

            return this;
        }

        public Builder pluginDependencies(List<String> pluginDependencies) {
            this.pluginDependencies = pluginDependencies;

            return this;
        }

        public Builder pluginDependency(String pluginDependency) {
            if (pluginDependencies == null) {
                pluginDependencies = new LinkedList<>();
            }

            pluginDependencies.add(pluginDependency);

            return this;
        }

        public Builder pluginRequires(String pluginRequires) {
            this.pluginRequires = pluginRequires;

            return this;
        }

        public Builder pluginLicense(String pluginLicense) {
            this.pluginLicense = pluginLicense;

            return this;
        }

        public PluginManifest build() {
            return new PluginManifest(this);
        }

    }

}
