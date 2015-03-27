package ro.fortsoft.pf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Resource;
import sun.misc.URLClassPath;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * @author LeanderK
 * @version 1.0
 */
public class IzouPluginClassLoader extends URLClassLoader {

    private static final Logger log = LoggerFactory.getLogger(IzouPluginClassLoader.class);

    private static final String PLUGIN_PACKAGE_PREFIX_PF4J = "ro.fortsoft.pf4j.";
    private static final String PLUGIN_PACKAGE_PREFIX_IZOU = "intellimate.izou";
    private static final String PLUGIN_PACKAGE_PREFIX_LOG_SL4J = "org.slf4j";
    private static final String PLUGIN_PACKAGE_PREFIX_LOG_LOG4J = "org.apache.logging.log4j";

    private PluginManager pluginManager;
    private PluginDescriptor pluginDescriptor;

    private URLClassPath classesClassPath = new URLClassPath(new URL[0]);

    public IzouPluginClassLoader(PluginManager pluginManager, PluginDescriptor pluginDescriptor, ClassLoader parent) {
        super(new URL[0], parent);
        this.pluginManager = pluginManager;
        this.pluginDescriptor = pluginDescriptor;
    }

    @Override
    public void addURL(URL url) {
        File file;
        try {
            file = new File(url.toURI());
        } catch (URISyntaxException e) {
            log.error("URI Syntax Exception", e);
            return;
        }
        if (!file.isDirectory())
            file = file.getParentFile();
        if (file.getName().equals("classes")) {
            classesClassPath.addURL(url);
        }
        super.addURL(url);
    }

    /**
     * This implementation of loadClass uses a child first delegation model rather than the standard parent first.
     * If the requested class cannot be found in this class loader, the parent class loader will be consulted
     * via the standard ClassLoader.loadClass(String) mechanism.
     */
    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        Class<?> ignoreClass = null;
        //jundl77.izou.izouclock.TTSOutputExtension
        log.debug("Received request to load class '{}'", className);
        // if the class it's a part of the plugin engine use parent class loader
        if (className.startsWith(PLUGIN_PACKAGE_PREFIX_PF4J)) {
            log.debug("Delegate the loading of class '{}' to parent", className);
            try {
                return getClass().getClassLoader().loadClass(className);
            } catch (ClassNotFoundException e) {
                // try next step
                // TODO if I uncomment below lines (the correct approach) I received ClassNotFoundException for demo (ro.fortsoft.pf4j.demo)
//                log.error(e.getMessage(), e);
//                throw e;
            }
        } else if (className.startsWith(PLUGIN_PACKAGE_PREFIX_LOG_SL4J) |
                className.startsWith(PLUGIN_PACKAGE_PREFIX_IZOU) |
                className.startsWith(PLUGIN_PACKAGE_PREFIX_LOG_LOG4J)) {
            try {
                //directing to parent
                return super.getParent().loadClass(className);
            } catch (ClassNotFoundException e) {
                //try next step
            }
        }

        Class<?> clazz = null;

        // nope, try to load locally from classes
        try {
            return loadClassFromClasses(className);
        } catch (ClassNotFoundException e) {
            //try next step
        }

        // look in dependencies
        log.debug("Look in dependencies for class '{}'", className);
        List<PluginDependency> dependencies = pluginDescriptor.getDependencies();
        for (PluginDependency dependency : dependencies) {
            IzouPluginClassLoader classLoader = pluginManager.getPluginClassLoader(dependency.getPluginId());
            try {
                clazz =  classLoader.loadClassFromClasses(className);
                log.debug("class '{}' found in dependency '{}'", className, dependency.getPluginId());
                return clazz;
            } catch (ClassNotFoundException e) {
                //try next dependency
            }
        }

        // try to load locally from lib
        try {
            clazz = findClass(className);
            log.debug("Found class '{}' in plugin classpath", className);
            return clazz;
        } catch (ClassNotFoundException e) {
            // try next step
        }

        log.debug("Couldn't find class '{}' in plugin classpath. Delegating to parent");

        // use the standard URLClassLoader (which follows normal parent delegation)
        return super.loadClass(className);
    }

    /**
     * tries to load the Class only from the classes folder
     * @param className the name of the class
     * @return a class
     * @throws ClassNotFoundException if the class is not found
     */
    public Class<?> loadClassFromClasses(String className) throws ClassNotFoundException {
        // second check whether it's already been loaded
        Class<?> clazz = findLoadedClass(className);
        if (clazz != null) {
            log.debug("Found loaded class '{}'", className);
            return clazz;
        }
        String path = className.replace('.', '/').concat(".class");
        Resource resource = classesClassPath.getResource(path, false);
        if (resource == null) {
            throw new ClassNotFoundException();
        }
        clazz = findClass(className);
        log.debug("Found class '{}' in plugin classpath", className);
        return clazz;
    }

    /**
     * Load the named resource from this plugin. This implementation checks the plugin's classpath first
     * then delegates to the parent.
     *
     * @param name the name of the resource.
     * @return the URL to the resource, <code>null</code> if the resource was not found.
     */
    @Override
    public URL getResource(String name) {
        log.debug("Trying to find resource '{}' in plugin classpath", name);
        URL url = findResource(name);
        if (url != null) {
            log.debug("Found resource '{}' in plugin classpath", name);
            return url;
        }

        log.debug("Couldn't find resource '{}' in plugin classpath. Delegating to parent");

        return super.getResource(name);
    }

    /**
     * Release all resources acquired by this class loader.
     * The current implementation is incomplete.
     * For now, this instance can no longer be used to load
     * new classes or resources that are defined by this loader.
     */
    public void dispose() {
        classesClassPath.closeLoaders();
        try {
            close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * the PluginDescriptor of the associated Plugin
     * @return the PluginDescriptor
     */
    public PluginDescriptor getPluginDescriptor() {
        return pluginDescriptor;
    }
}
