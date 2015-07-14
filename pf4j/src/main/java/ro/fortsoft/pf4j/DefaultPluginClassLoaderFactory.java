package ro.fortsoft.pf4j;

public class DefaultPluginClassLoaderFactory implements PluginClassLoaderFactory {
  
    @Override
    public PluginClassLoader getPluginClassLoader(final PluginManager pluginManager,
            final PluginDescriptor pluginDescriptor, final ClassLoader parent) {
        return new PluginClassLoader(pluginManager, pluginDescriptor, parent);
    }
}