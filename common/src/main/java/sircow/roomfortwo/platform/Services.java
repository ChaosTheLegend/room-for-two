package sircow.roomfortwo.platform;

import sircow.roomfortwo.Constants;
import sircow.roomfortwo.platform.services.IPlatformHelper;
import sircow.roomfortwo.platform.services.IPlatformNetwork;

import java.util.ServiceLoader;

public class Services {
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);
    public static final IPlatformNetwork NETWORK = ServiceLoader.load(IPlatformNetwork.class)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Failed to load IPlatformNetwork service"));

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        Constants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}