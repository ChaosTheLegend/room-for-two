package sircow.roomfortwo.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import sircow.roomfortwo.Constants;

import java.util.HashMap;
import java.util.Map;

public record BedOccupancySyncPayload(Map<Integer, Integer> entitySlots) implements CustomPacketPayload {
    public static final ResourceLocation ID = Constants.id("bed_occupancy_sync");

    public BedOccupancySyncPayload(FriendlyByteBuf buf) {
        this(readMap(buf));
    }

    private static Map<Integer, Integer> readMap(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<Integer, Integer> map = new HashMap<>();

        for (int i = 0; i < size; i++) {
            map.put(buf.readVarInt(), buf.readVarInt());
        }
        return map;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.entitySlots.size());

        this.entitySlots.forEach((entityId, slot) -> {
            buf.writeVarInt(entityId);
            buf.writeVarInt(slot);
        });
    }

    @Override
    public @NotNull ResourceLocation id() {
        return ID;
    }
}
