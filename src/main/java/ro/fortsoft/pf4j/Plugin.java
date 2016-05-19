package ro.fortsoft.pf4j;

/**
 * <p>
 * </p>
 *
 * @author Julian Brendl
 * @version 1.0
 */
public abstract class Plugin {
    /**
     * Wrapper of the plugin.
     */
    private PluginWrapper wrapper;

    /**
     * Constructor to be used by plugin manager for plugin instantiation.
     * Your plugins have to provide constructor with this exact signature to
     * be successfully loaded by manager.
     *
     * @param wrapper the PluginWrapper to assign the ZipFileManager to
     */
    public Plugin(PluginWrapper wrapper) {
        if (wrapper == null) {
            throw new IllegalArgumentException("Wrapper cannot be null");
        }

        this.wrapper = wrapper;
    }

    /**
     * Retrieves the wrapper of this plug-in.
     */
    public final PluginWrapper getWrapper() {
        return wrapper;
    }

    /**
     * Start method is called by the application when the plugin is loaded.
     */
    public void start() throws PluginException {
    }

    /**
     * Stop method is called by the application when the plugin is unloaded.
     */
    public void stop() throws PluginException {
    }
}
