package sircow.roomfortwo;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class RoomForTwo {
    public RoomForTwo(IEventBus eventBus) {
        CommonClass.init();
    }
}
