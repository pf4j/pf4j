package ro.fortsoft.pf4j;

import org.aspectj.weaver.loadtime.WeavingURLClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Resource;
import sun.misc.URLClassPath;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author LeanderK
 * @version 1.0
 */
//TODO remove laziness!
public class IzouPluginClassLoader extends URLClassLoader {

    private static final Logger log = LoggerFactory.getLogger(IzouPluginClassLoader.class);

    public static final String PLUGIN_PACKAGE_PREFIX_PF4J = "ro.fortsoft.pf4j.";
    public static final String PLUGIN_PACKAGE_PREFIX_IZOU = "org.intellimate.izou";
    public static final String PLUGIN_PACKAGE_PREFIX_IZOU_SDK = "org.intellimate.izou.sdk";
    public static final String PLUGIN_PACKAGE_PREFIX_LOG_SL4J = "org.slf4j";
    public static final String PLUGIN_PACKAGE_PREFIX_LOG_LOG4J = "org.apache.logging.log4j";
    public static final String PLUGIN_ZIP_FILE_MANAGER = "ZipFileManager";

    private final ConcurrentMap<String, Class<?>> aspectsOrAffectedClass;
    private final Map<String, AspectOrAffected> aspectOrAffectedMap;
    private PluginManager pluginManager;
    private PluginDescriptor pluginDescriptor;
    private URLClassPath classesClassPath = new URLClassPath(new URL[0]);
    private WeavingURLClassLoaderHelper weaver;

    public IzouPluginClassLoader(PluginManager pluginManager, PluginDescriptor pluginDescriptor, ClassLoader parent, List<AspectOrAffected> aspectOrAffectedList) {
        super(new URL[0], parent);
        this.pluginManager = pluginManager;
        this.pluginDescriptor = pluginDescriptor;
        //aspects.forEach(url -> this.aspectsOrAffected.put(url, null));
        weaver = new WeavingURLClassLoaderHelper(new URL[0], this);
        aspectOrAffectedList.stream()
                .filter(AspectOrAffected::isAspect)
                .map(AspectOrAffected::getDirectory)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(weaver::addURL);
        aspectOrAffectedMap = aspectOrAffectedList.stream()
                .collect(Collectors.toMap(AspectOrAffected::getClassName, Function.identity()));
        aspectsOrAffectedClass = new ConcurrentHashMap<>();
        ConcurrentMap<String, Class<?>> classes = aspectOrAffectedList.stream()
                .collect(Collectors.toConcurrentMap(AspectOrAffected::getClassName, aspectOrAffected1 -> {
                    if (aspectsOrAffectedClass.get(aspectOrAffected1.getClassName()) != null)
                        return aspectsOrAffectedClass.get(aspectOrAffected1.getClassName());
                    InputStream is = this.getResourceAsStream(aspectOrAffected1.getClassName().replace('.', '/') + ".class");
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                    int nRead;
                    byte[] data = new byte[16384];

                    try {
                        while ((nRead = is.read(data, 0, data.length)) != -1) {
                            buffer.write(data, 0, nRead);
                        }
                    } catch (IOException e) {
                        return null;
                    }

                    try {
                        buffer.flush();
                    } catch (IOException e) {
                        return null;
                    }
                    byte[] array = buffer.toByteArray();
                    try {
                        Class aClass = weaver.defineClass(aspectOrAffected1.getClassName(), array,
                                new CodeSource(aspectOrAffected1.getPath(), (Certificate[]) null));
                        return aspectOrAffected1.getCallback().apply(aClass);
                    } catch (IOException e) {
                        return null;
                    }
                }));
        aspectsOrAffectedClass.putAll(classes);
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
        log.debug("Received request to load class '{}'", className);
        Class<?> weaved = checkAndWeave(className);
        if (weaved != null) return weaved;
        // if the class it's a part of the plugin engine use parent class loader
        if (className.startsWith(PLUGIN_PACKAGE_PREFIX_PF4J)) {
            log.debug("Delegate the loading of class '{}' to parent", className);
            try {
                return getClass().getClassLoader().loadClass(className);
            } catch (ClassNotFoundException e) {
                // try next step
                // TODO if I uncomment below lines (the correct approach) I received ClassNotFoundException for demo (ro.fortsoft.pf4j.demo)
                //  log.error(e.getMessage(), e);
                //  throw e;
            }
        }
        else if (className.startsWith(PLUGIN_PACKAGE_PREFIX_LOG_SL4J) ||
                className.startsWith(PLUGIN_PACKAGE_PREFIX_IZOU) ||
                className.startsWith(PLUGIN_PACKAGE_PREFIX_LOG_LOG4J)) {
            try {
                //directing to parent
                return super.getParent().loadClass(className);
            } catch (ClassNotFoundException e) {
                //try next step
            }
        }

        return loadCustomClass(className);
    }

    /**
     * checks if the class should be weaved and weaves it
     * @param className the classname
     * @return null if not eligible or an error occurred
     */
    private Class<?> checkAndWeave(String className) {
        return aspectsOrAffectedClass.get(className);
    }

    /**
     * Same as the {@link #loadClass} method, except that it guarantees to load a class on its own, and not delegate the
     * task to a parent
     *
     * @param className the name of the class to load
     * @return the loaded class
     * @throws ClassNotFoundException thrown when the class was not able to be loaded
     */
    private Class<?> loadCustomClass(String className) throws ClassNotFoundException {
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

    private class WeavingURLClassLoaderHelper extends WeavingURLClassLoader {

        public WeavingURLClassLoaderHelper(URL[] classURLs, URL[] aspectURLs, ClassLoader parent) {
            super(classURLs, aspectURLs, parent);
        }

        public WeavingURLClassLoaderHelper(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        /**
         * Override to weave class using WeavingAdaptor
         *
         * @param name
         * @param b
         * @param cs
         */
        @Override
        public Class defineClass(String name, byte[] b, CodeSource cs) throws IOException {
            return super.defineClass(name, b, cs);
        }

        @Override
        protected void addURL(URL url) {
            super.addURL(url);
        }

        /**
         * Loads the class with the specified <a href="#name">binary name</a>.
         * This method searches for classes in the same manner as the {@link
         * #loadClass(String, boolean)} method.  It is invoked by the Java virtual
         * machine to resolve class references.  Invoking this method is equivalent
         * to invoking {@link #loadClass(String, boolean) <tt>loadClass(name,
         * false)</tt>}.
         *
         * @param name The <a href="#name">binary name</a> of the class
         * @return The resulting <tt>Class</tt> object
         * @throws ClassNotFoundException If the class was not found
         */
        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            Class<?> aClass = checkAndWeave(name);
            if (aClass != null) {
                return aClass;
            } else {
                return super.loadClass(name);
            }
        }
    }
}
