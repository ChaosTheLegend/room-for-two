package sircow.roomfortwo.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class ForgeCameraMixin {
    @Shadow private BlockGetter level;
    @Shadow private Entity entity;
    @Shadow private boolean detached, initialized;
    @Shadow private float partialTickTime, eyeHeight;

    @Shadow protected abstract void setPosition(double x, double y, double z);

    @Inject(method = "setup", at = @At("HEAD"), cancellable = true)
    private void roomfortwo$forceStaticSleepCamera(BlockGetter blockGetter, Entity entity, boolean detached, boolean inverseView, float partialTick, CallbackInfo ci) {
        if (entity instanceof LivingEntity livingEntity && livingEntity.isSleeping()) {
            if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                this.initialized = true;
                this.level = blockGetter;
                this.entity = entity;
                this.detached = detached;
                this.partialTickTime = partialTick;

                this.setPosition(livingEntity.getX(), livingEntity.getY() + (double) this.eyeHeight, livingEntity.getZ());
                ci.cancel();
            }
        }
    }
}
