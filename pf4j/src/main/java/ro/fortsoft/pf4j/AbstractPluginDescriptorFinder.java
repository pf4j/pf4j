package ro.fortsoft.pf4j;

/**
 * Base class implementing validate
 */
public abstract class AbstractPluginDescriptorFinder implements PluginDescriptorFinder {
    private PluginDescriptorValidator validator = new DefaultPluginDescriptorValidator();

    void validate(PluginDescriptor descriptor) throws PluginException {
        validator.validate(descriptor);
    }

    public PluginDescriptorValidator getValidator() {
        return validator;
    }

    public void setValidator(PluginDescriptorValidator validator) {
        this.validator = validator;
    }
}
