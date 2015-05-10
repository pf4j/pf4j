package ro.fortsoft.pf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The IzouRegistrarService registers addOns at load time with different components of Izou.
 * <p>
 *     For example, here addOns are registered with the {@link org.intellimate.izou.security.SocketPermissionModule}
 *     if they are allowed to use sockets.
 * </p>
 */
public class IzouRegistrarService {
    private final Logger log = LoggerFactory.getLogger(IzouPluginClassLoader.class);
    private org.intellimate.izou.security.SecurityManager izouSecurityManager;

    /**
     * Creates a new registrar service
     */
    public IzouRegistrarService() {
        // Get system security manager
        java.lang.SecurityManager javaSecurityManager = System.getSecurityManager();
        if (javaSecurityManager instanceof org.intellimate.izou.security.SecurityManager) {
            izouSecurityManager = (org.intellimate.izou.security.SecurityManager) javaSecurityManager;
        }

        if (izouSecurityManager == null) {
            throw new NullPointerException("System.getSecurityManager() did not return a SecurityManager from Izou");
        }
    }

    /**
     * Registers the addOn with all services it can be registered with
     *
     * @param descriptor the current PluginDescriptor of the addOn bein loaded
     */
    public void register(PluginDescriptor descriptor) {
        registerSocket(descriptor);
    }

    private void registerSocket(PluginDescriptor descriptor) throws NullPointerException {
        boolean canConnect = false;
        try {
            canConnect = descriptor.getAddOnProperties().get("socket_connection").equals("true")
                    && !descriptor.getAddOnProperties().get("socket_usage_descripton").equals("null");
        } catch (NullPointerException e) {
            // Do nothing that is fine, it will just skip ahead and not register
        }

        if (canConnect) {
            izouSecurityManager.getPermissionManager().getSocketPermissionModule()
                    .registerAddOn(descriptor.getPluginId());
        }
    }
}
