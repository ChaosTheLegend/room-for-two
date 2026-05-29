package sircow.roomfortwo.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import sircow.roomfortwo.platform.Services;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class BedOccupancyTracker {
    private static final Map<Integer, Integer> clientSlotCache = new ConcurrentHashMap<>();

    private static final Map<BlockPos, List<Integer>> serverBedOrders = new ConcurrentHashMap<>();

    private BedOccupancyTracker() {}

    public static int getSlot(int entityId) {
        return clientSlotCache.getOrDefault(entityId, 0);
    }

    public static void updateClientCache(Map<Integer, Integer> map) {
        clientSlotCache.putAll(map);
    }

    public static void cleanClientEntity(int entityId) {
        clientSlotCache.remove(entityId);
    }

    public static void updateBedOccupancy(ServerLevel level, BlockPos bedPos, int leavingEntityId) {
        if (bedPos == null) return;

        List<LivingEntity> sleepers = level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(bedPos).inflate(4.0),
                entity -> entity.isSleeping()
                        && entity.getId() != leavingEntityId
                        && bedPos.equals(Objects.requireNonNull(entity.getSleepingPos().orElse(null)))
        );

        Set<Integer> currentSleeperIds = new HashSet<>();
        for (LivingEntity sleeper : sleepers) {
            currentSleeperIds.add(sleeper.getId());
        }

        List<Integer> bedOrder = serverBedOrders.computeIfAbsent(bedPos, k -> new ArrayList<>());
        bedOrder.removeIf(id -> id == leavingEntityId || !currentSleeperIds.contains(id));

        for (LivingEntity sleeper : sleepers) {
            int id = sleeper.getId();
            if (!bedOrder.contains(id)) {
                bedOrder.add(id);
            }
        }

        if (bedOrder.isEmpty()) {
            serverBedOrders.remove(bedPos);
            return;
        }

        Map<Integer, Integer> syncMap = new HashMap<>();
        for (int i = 0; i < bedOrder.size(); i++) {
            syncMap.put(bedOrder.get(i), i);
        }

        Services.NETWORK.broadcastBedOccupancy(level, bedPos, syncMap);
    }
}
