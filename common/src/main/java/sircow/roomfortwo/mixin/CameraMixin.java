package sircow.roomfortwo.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;
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

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow @Final private Quaternionf rotation;
    @Shadow private Entity entity;

    @Shadow protected abstract void move(float forwards, float up, float right);

    @Inject(method = "alignWithEntity", at = @At("TAIL"))
    private void applySleepCameraRotationAndTranslation(float partialTicks, CallbackInfo ci) {
        if (this.entity instanceof LivingEntity livingEntity && livingEntity.isSleeping()) {
            if (!Minecraft.getInstance().options.getCameraType().isFirstPerson()) return;

            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) return;

            double x = livingEntity.getX();
            double y = livingEntity.getY();
            double z = livingEntity.getZ();

            AABB bedArea = new AABB(x - 1.5, y - 1.0, z - 1.5, x + 1.5, y + 1.0, z + 1.5);
            List<LivingEntity> occupants = level.getEntitiesOfClass(LivingEntity.class, bedArea, LivingEntity::isSleeping);
            occupants.sort(Comparator.comparingInt(LivingEntity::getId));

            int index = 0;
            for (int i = 0; i < occupants.size(); i++) {
                if (occupants.get(i).getId() == livingEntity.getId()) {
                    index = i;
                    break;
                }
            }

            boolean hasVillager = occupants.stream().anyMatch(e -> e instanceof Villager);

            if (index > 0 || hasVillager) {
                this.rotation.rotateZ((float) Math.toRadians(90.0));
                this.rotation.rotateX((float) Math.toRadians(90.0));
                this.move(-0.75F, 0.1F, 0.0F);
            }
            else {
                this.rotation.rotateY((float) Math.toRadians(180.0));
                this.rotation.rotateZ((float) Math.toRadians(-90.0));
                this.rotation.rotateX((float) Math.toRadians(-90.0));
                this.move(-0.75F, 0.1F, 0.0F);
            }
        }
    }
}