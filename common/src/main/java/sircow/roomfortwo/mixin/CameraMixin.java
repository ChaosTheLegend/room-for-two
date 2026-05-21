package sircow.roomfortwo.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.List;

// TODO: Currently doesn't rotate the camera in Forge, no Forge release until this is fixed

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow @Final private Quaternionf rotation;
    @Shadow private Entity entity;

    @Shadow protected abstract void move(float forwards, float up, float right);

    @Inject(method = "setup", at = @At("TAIL"))
    private void roomfortwo$adjustSleepCamera(BlockGetter blockGetter, Entity entity, boolean detached, boolean inverseView, float partialTick, CallbackInfo ci)  {
        if (!(this.entity instanceof LivingEntity livingEntity)) return;
        if (!livingEntity.isSleeping()) return;
        if (!Minecraft.getInstance().options.getCameraType().isFirstPerson()) return;

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

        if ((index & 1) == 0) {
            rotation.rotateY((float) Math.toRadians(180.0));
            rotation.rotateZ((float) Math.toRadians(-90.0));
            rotation.rotateX((float) Math.toRadians(-90.0));
        }
        else {
            rotation.rotateZ((float) Math.toRadians(90.0));
            rotation.rotateX((float) Math.toRadians(90.0));
        }
        move(-0.75F, 0.1F, 0.0F);
    }
}
