package sircow.roomfortwo;

import sircow.roomfortwo.platform.Services;

public class CommonClass {
    public static void init() {
        if (Services.PLATFORM.isModLoaded("roomfortwo")) {
            Constants.LOG.info("Initialising {}", Constants.MOD_NAME);
        }
    }
}
