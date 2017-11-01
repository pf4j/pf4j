package org.pf4j;

import java.util.List;

/**
 * A plugin descriptor contains information about a plug-in obtained
 * from the manifest (META-INF) file.
 *
 * @author Decebal Suiu
 */
public interface PluginDescriptor {

    String getPluginId();

    String getPluginDescription();

    String getPluginClass();

    String getVersion();

    String getRequires();

    String getProvider();

    String getLicense();

    List<PluginDependency> getDependencies();
}
