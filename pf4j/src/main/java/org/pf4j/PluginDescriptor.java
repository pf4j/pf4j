package org.pf4j;

import java.util.List;

public interface PluginDescriptor {

    String getPluginId();

    String getPluginDescription();

    String getPluginClass();

    String getVersion();

    String getRequires();

    String getProvider();

    String getLicense();

    List<PluginDependency> getDependencies();

    @Override
    String toString();
}
