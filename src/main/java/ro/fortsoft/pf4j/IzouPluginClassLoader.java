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

    public static final String PLUGIN_PACKAGE_PREFIX_PF4J = "ro.fortsoft.pf4j.";
    public static final String PLUGIN_PACKAGE_PREFIX_IZOU = "org.intellimate.izou";
    public static final String PLUGIN_PACKAGE_PREFIX_IZOU_SDK = "org.intellimate.izou.sdk";
    public static final String PLUGIN_PACKAGE_PREFIX_LOG_SL4J = "org.slf4j";
    public static final String PLUGIN_PACKAGE_PREFIX_LOG_LOG4J = "org.apache.logging.log4j";
    public static final String PLUGIN_ZIP_FILE_MANAGER = "ZipFileManager";

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
     * checks whether the Addon is allowed to access the class
     * @param parentResult the resulting class
     * @return the Class or an Exception
     */
    private Class<?> checkAccess(Class<?> parentResult, String classname) throws LinkageError {
        if (parentResult.isAnnotationPresent(AddonAccessible.class) && (parentResult.isInterface()
                || checkInstanceOf(parentResult, Throwable.class)) || parentResult.isEnum()) {
            return parentResult;
        } else {
            throw new LinkageError("Requested class: " + classname + " is not accessible for addons");
        }
    }

    /**
     * Checks if a subject class is a sub class of a super class
     *
     * @param subject The sub class to check if the superClass is a super class of it.
     * @param superClass The super class.
     * @return True if subject is a super class of superClass and otherwise false.
     */
    private boolean checkInstanceOf(Class subject, Class superClass) {
        Class subjectClass = subject;

        // Check for null
        if (subjectClass == null || superClass == null) {
            return false;
        }

        // Check all super classes
        while (subjectClass != Object.class) {
            if (subjectClass.getSuperclass() == superClass) {
                return true;
            }

            subjectClass = subjectClass.getSuperclass();
        }

        return false;
    }

    /**
     * This implementation of loadClass uses a child first delegation model rather than the standard parent first.
     * If the requested class cannot be found in this class loader, the parent class loader will be consulted
     * via the standard ClassLoader.loadClass(String) mechanism.
     */
    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        log.debug("Received request to load class '{}'", className);

        // if the class it's a part of the plugin engine use parent class loader
        if (className.startsWith(PLUGIN_PACKAGE_PREFIX_PF4J)) {
            log.debug("Delegate the loading of class '{}' to parent", className);
            try {
//                return checkAccess(super.getParent().loadClass(className), className);
                return super.getParent().loadClass(className);
            } catch (ClassNotFoundException e) {
                // try next step
                // TODO if I uncomment below lines (the correct approach) I received ClassNotFoundException for demo (ro.fortsoft.pf4j.demo)
                //  log.error(e.getMessage(), e);
                //  throw e;
            }
        }
        else
        //TODO resolve: what about the logging things
        if (//className.startsWith(PLUGIN_PACKAGE_PREFIX_LOG_SL4J) ||
                className.startsWith(PLUGIN_PACKAGE_PREFIX_IZOU) ) {
                        //||
                //className.startsWith(PLUGIN_PACKAGE_PREFIX_LOG_LOG4J)) {
            try {
                //directing to parent
                return checkAccess(super.getParent().loadClass(className), className);
            } catch (ClassNotFoundException e) {
                //try next step
            }
        }

        try {
            return customLoadClass(className);
        } catch (LinkageError e) {
            System.err.println("WTF");
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Same as the {@link #loadClass} method, except that it guarantees to load a class on its own, and not delegate the
     * task to a parent
     *
     * @param className the name of the class to load
     * @return the loaded class
     * @throws ClassNotFoundException thrown when the class was not able to be loaded
     */
    private Class<?> customLoadClass(String className) throws ClassNotFoundException {
        Class<?> clazz;

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
        return super.getParent().loadClass(className);
    }

    /**
     * tries to load the Class only from the classes folder
     * @param className the name of the class
     * @return a class
     * @throws ClassNotFoundException if the class is not found
     */
    public Class<?> loadClassFromClasses(String className) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(className)) {
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
