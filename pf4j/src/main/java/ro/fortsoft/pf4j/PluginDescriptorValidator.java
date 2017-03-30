package ro.fortsoft.pf4j;

/**
 * Validates plugin descriptor
 */
public interface PluginDescriptorValidator {
    /**
     * Validate that the content of the descriptor is sane
     * @param descriptor an instance of PluginDescriptor
     * @throws PluginException if validation fails
     */
    void validate(PluginDescriptor descriptor) throws PluginException;
}
