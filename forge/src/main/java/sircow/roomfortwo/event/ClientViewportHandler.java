package sircow.roomfortwo.event;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;

@Mod.EventBusSubscriber(modid = "roomfortwo", value = Dist.CLIENT)
public class ClientViewportHandler {
    private static final Method MOVE_CAMERA_METHOD = ObfuscationReflectionHelper.findMethod(
            Camera.class, "m_90568_", double.class, double.class, double.class
    );

    @SubscribeEvent
    public static void onComputeCameraAngles(EntityViewRenderEvent.CameraSetup event) {
        Camera camera = event.getCamera();

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return;

        LivingEntity livingEntity = minecraft.player;
        if (!livingEntity.isSleeping()) return;
        if (!minecraft.options.getCameraType().isFirstPerson()) return;

        ClientLevel level = minecraft.level;
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
        if (direction == null) return;

        float baseYaw = direction.toYRot() - 180.0F;
        float xRotateL, yRotateL, zRotateL, xRotateR, yRotateR, zRotateR, targetPitch, targetYaw, targetRoll;

        xRotateR = 0.0F;
        yRotateR = 90.0F;
        zRotateR = 90.0F;
        xRotateL = -xRotateR;
        yRotateL = -wrapYaw(yRotateR);
        zRotateL = -zRotateR;

        if ((index & 1) == 0) {
            targetPitch = xRotateR;
            targetYaw = baseYaw + yRotateR;
            targetRoll = zRotateR;
            moveCameraFree(camera, 0.0, -0.1, 0.5);
        }
        else {
            targetPitch = xRotateL;
            targetYaw = baseYaw + yRotateL;
            targetRoll = zRotateL;
            moveCameraFree(camera, 0.0, -0.1, -0.5);
        }
        event.setPitch(targetPitch);
        event.setYaw(targetYaw);
        event.setRoll(targetRoll);
    }

    private static float wrapYaw(float yaw) {
        yaw %= 360.0F;
        if (yaw > 180.0F) yaw -= 360.0F;
        if (yaw <= -180.0F) yaw += 360.0F;
        return yaw;
    }

    private static void moveCameraFree(Camera camera, double x, double y, double z) {
        try {
            MOVE_CAMERA_METHOD.invoke(camera, x, y, z);
        }
        catch (Exception ignored) {}
    }
}