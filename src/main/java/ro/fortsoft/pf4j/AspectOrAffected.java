package ro.fortsoft.pf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.function.Function;

/**
 * this class contains information about aspect.
 * @author LeanderK
 * @version 1.0
 */
public class AspectOrAffected {
    private final URL path;
    private final String className;
    private final Function<Class<?>, Class<?>> callback;
    private final boolean isAspect;
    private static final Logger log = LoggerFactory.getLogger(IzouPluginClassLoader.class);


    public AspectOrAffected(URL path, String className, Function<Class<?>, Class<?>> callback, boolean isAspect) {
        this.path = path;
        this.className = className;
        this.callback = callback;
        this.isAspect = isAspect;
    }

    public URL getPath() {
        return path;
    }

    public Optional<URL> getDirectory() {
        if (!path.getProtocol().equals("jar")) {
            URL finalURL = path;
            if (!new File(finalURL.getFile()).isDirectory())
                try {
                    File file = new File(path.getFile());
                    return Optional.of(file.getParentFile().toURI().toURL());
                } catch (MalformedURLException e) {
                    log.error("illegal url");
                }
            return Optional.empty();
        } else {
            final JarURLConnection connection;
            try {
                connection = (JarURLConnection) path.openConnection();
            } catch (IOException e) {
                log.error("an error occured while trying to fetch directory");
                return Optional.empty();
            }
            URL url = connection.getJarFileURL();
            return Optional.of(url);
        }
    }

    public String getClassName() {
        return className;
    }

    public Function<Class<?>, Class<?>> getCallback() {
        return callback;
    }

    public boolean isAspect() {
        return isAspect;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AspectOrAffected)) return false;

        AspectOrAffected that = (AspectOrAffected) o;

        if (isAspect != that.isAspect) return false;
        if (path != null ? !path.equals(that.path) : that.path != null) return false;
        return !(className != null ? !className.equals(that.className) : that.className != null);

    }

    @Override
    public int hashCode() {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (className != null ? className.hashCode() : 0);
        result = 31 * result + (isAspect ? 1 : 0);
        return result;
    }
}
