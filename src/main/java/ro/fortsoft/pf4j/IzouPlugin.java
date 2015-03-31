package ro.fortsoft.pf4j;

/**
 * This class should be extended by all ZipFileManagers in Izou. It serves as a way of getting information from
 * the izou addOn.
 *
 * @author Intellimate
 * @version 2.0
 */
public class IzouPlugin extends Plugin {
    private String sdkVersion;

    /**
     * Constructor to be used by plugin manager for plugin instantiation.
     * Your plugins have to provide constructor with this exact signature to
     * be successfully loaded by manager.
     *
     * @param wrapper the PluginWrapper to assign the ZipFileManager to
     * @param sdkVersion the version of the izou sdk this plugin is using
     */
    public IzouPlugin(PluginWrapper wrapper, String sdkVersion) {
        super(wrapper);
        this.sdkVersion = sdkVersion;
    }

    public String getSDKVersion() {
        return sdkVersion;
    }
}
