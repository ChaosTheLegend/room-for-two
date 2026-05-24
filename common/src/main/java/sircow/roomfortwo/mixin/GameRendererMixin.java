package sircow.roomfortwo.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.List;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;prepareCullFrustum(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;Lorg/joml/Matrix4f;)V", shift = At.Shift.AFTER))
    private void roomfortwo$adjustSleepCamera(float partialTick, long nanoTime, PoseStack poseStack, CallbackInfo ci) {
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

        poseStack.mulPose(Axis.YP.rotationDegrees(direction.toYRot() - 180.0F));

        float xRotateL, yRotateL, zRotateL, xRotateR, yRotateR, zRotateR;
        switch (direction) {
            case NORTH -> {
                xRotateR = 90.0F;
                yRotateR = 0.0F;
                zRotateR = -90.0F;
                xRotateL = xRotateR;
                yRotateL = wrapYaw(yRotateR);
                zRotateL = -zRotateR;
            }
            case SOUTH -> {
                xRotateR = -90.0F;
                yRotateR = 0.0F;
                zRotateR = -90.0F;
                xRotateL = xRotateR;
                yRotateL = wrapYaw(yRotateR);
                zRotateL = -zRotateR;
            }
            case WEST -> {
                xRotateR = 0.0F;
                yRotateR = 180.0F;
                zRotateR = -90.0F;
                xRotateL = xRotateR + 180.0F;
                yRotateL = wrapYaw(yRotateR);
                zRotateL = -zRotateR;
            }
            case EAST -> {
                xRotateR = 0.0F;
                yRotateR = 0.0F;
                zRotateR = 90.0F;
                xRotateL = xRotateR + 180.0F;
                yRotateL = wrapYaw(yRotateR);
                zRotateL = -zRotateR;
            }
            default -> {
                xRotateL = xRotateR = 0.0F;
                yRotateL = yRotateR = 0.0F;
                zRotateL = zRotateR = 0.0F;
            }
        }

        if ((index & 1) == 0) {
            poseStack.mulPose(Axis.XP.rotationDegrees(xRotateR));
            poseStack.mulPose(Axis.YP.rotationDegrees(yRotateR));
            poseStack.mulPose(Axis.ZP.rotationDegrees(zRotateR));
            switch (direction) {
                case NORTH -> poseStack.translate(-0.5D, -0.1D, 0.0D);
                case SOUTH -> poseStack.translate(0.5D, -0.1D, 0.0D);
                case WEST -> poseStack.translate(0.0D, -0.1D, 0.5D);
                case EAST -> poseStack.translate(0.0D, -0.1D, -0.5D);
            }
        }
        else {
            poseStack.mulPose(Axis.XP.rotationDegrees(xRotateL));
            poseStack.mulPose(Axis.YP.rotationDegrees(yRotateL));
            poseStack.mulPose(Axis.ZP.rotationDegrees(zRotateL));
            switch (direction) {
                case NORTH -> poseStack.translate(0.75D, -0.1D, 0.0D);
                case SOUTH -> poseStack.translate(-0.75D, -0.1D, 0.0D);
                case WEST -> poseStack.translate(0.0D, -0.1D, -0.75D);
                case EAST -> poseStack.translate(0.0D, -0.1D, 0.75D);
            }
        }
    }

    @Unique
    private static float wrapYaw(float yaw) {
        yaw %= 360.0F;
        if (yaw > 180.0F) yaw -= 360.0F;
        if (yaw <= -180.0F) yaw += 360.0F;
        return yaw;
    }
}
