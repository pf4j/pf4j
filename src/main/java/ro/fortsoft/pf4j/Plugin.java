package ro.fortsoft.pf4j;

/**
 * <p>
 * </p>
 *
 * @author Julian Brendl
 * @version 1.0
 */

@AddonAccessible
public interface Plugin {
    /**
     * Retrieves the wrapper of this plug-in.
     */
    PluginWrapper getWrapper();

    /**
     * Start method is called by the application when the plugin is loaded.
     */
    void start() throws PluginException;

    /**
     * Stop method is called by the application when the plugin is unloaded.
     */
    void stop() throws PluginException;
}
