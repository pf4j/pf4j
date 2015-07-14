package ro.fortsoft.pf4j;

public interface PluginClassLoaderFactory {
   
    PluginClassLoader getPluginClassLoader(PluginManager pluginManager, PluginDescriptor pluginDescriptor, ClassLoader parent);
}
