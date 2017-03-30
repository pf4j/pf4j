package ro.fortsoft.pf4j;

import ro.fortsoft.pf4j.util.StringUtils;

/**
 * Checks that there is an id, version and class
 */
public class DefaultPluginDescriptorValidator implements PluginDescriptorValidator {
    @Override
    public void validate(PluginDescriptor descriptor) throws PluginException {
        if (StringUtils.isEmpty(descriptor.getPluginId())) {
            throw new PluginException("id cannot be empty");
        }
        if (StringUtils.isEmpty(descriptor.getPluginClass())) {
            throw new PluginException("class cannot be empty");
        }
        if (descriptor.getVersion() == null) {
            throw new PluginException("version cannot be empty");
        }
    }
}
