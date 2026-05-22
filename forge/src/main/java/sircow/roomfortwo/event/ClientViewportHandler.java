package sircow.roomfortwo.event;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;

@Mod.EventBusSubscriber(modid = "roomfortwo", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientViewportHandler {
    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Camera camera = event.getCamera();

        if (camera.getEntity() instanceof LivingEntity livingEntity && livingEntity.isSleeping()) {
            if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                ClientLevel level = Minecraft.getInstance().level;
                if (level == null) return;

                AABB bedArea = new AABB(
                        livingEntity.getX() - 1.5, livingEntity.getY() - 1.0, livingEntity.getZ() - 1.5,
                        livingEntity.getX() + 1.5, livingEntity.getY() + 1.0, livingEntity.getZ() + 1.5
                );

                List<LivingEntity> occupants = level.getEntitiesOfClass(LivingEntity.class, bedArea, LivingEntity::isSleeping);
                occupants.sort(Comparator.comparingInt(LivingEntity::getId));

                int index = 0;
                for (int i = 0; i < occupants.size(); i++) {
                    if (occupants.get(i).getId() == livingEntity.getId()) {
                        index = i;
                        break;
                    }
                }

                Direction direction = livingEntity.getBedOrientation();
                float baseYaw = (direction != null) ? direction.toYRot() : 0.0F;
                float targetYaw, targetPitch, targetRoll;

                if ((index & 1) == 0) {
                    targetYaw = baseYaw - 90.0F;
                    targetPitch = 0.0F;
                    targetRoll = -90.0F;
                }
                else {
                    targetYaw = baseYaw + 90.0F;
                    targetPitch = 0.0F;
                    targetRoll = 90.0F;
                }

                event.setYaw(targetYaw);
                event.setPitch(targetPitch);
                event.setRoll(targetRoll);
                moveCameraFree(camera);
            }
        }
    }

    private static void moveCameraFree(Camera camera) {
        try {
            Method moveMethodDev = Camera.class.getDeclaredMethod("move", float.class, float.class, float.class);
            moveMethodDev.setAccessible(true);
            moveMethodDev.invoke(camera, (float) -0.5, (float) 0.1, (float) 0.0);
        }
        catch (Exception ignored) {}
    }
}
