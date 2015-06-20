package ro.fortsoft.pf4j;

import org.aspectj.weaver.loadtime.WeavingURLClassLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
public class IzouWeavingClassLoader extends WeavingURLClassLoader {
    private final ConcurrentMap<String, Class<?>> aspectsOrAffectedClass;
    private final Map<String, AspectOrAffected> aspectOrAffectedMap;

    public IzouWeavingClassLoader(ClassLoader parent, List<AspectOrAffected> aspectOrAffectedList) {
        super(new URL[0], parent);
        aspectOrAffectedMap = aspectOrAffectedList.stream()
                .collect(Collectors.toMap(AspectOrAffected::getClassName, Function.identity()));
        aspectOrAffectedList.stream()
                .filter(AspectOrAffected::isAspect)
                .map(AspectOrAffected::getDirectory)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(this::addURL);
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
                        Class aClass = this.defineClass(aspectOrAffected1.getClassName(), array,
                                new CodeSource(aspectOrAffected1.getPath(), (Certificate[]) null));
                        return aspectOrAffected1.getCallback().apply(aClass);
                    } catch (IOException e) {
                        return null;
                    }
                }));
        aspectsOrAffectedClass.putAll(classes);
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
        Class<?> aClass = aspectsOrAffectedClass.get(name);
        if (aClass != null) {
            return aClass;
        } else {
            return super.loadClass(name);
        }
    }
}
