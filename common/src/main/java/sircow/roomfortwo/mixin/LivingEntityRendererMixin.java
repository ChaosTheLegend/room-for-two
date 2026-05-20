package sircow.roomfortwo.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.List;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {
    @Inject(method = "setupRotations", at = @At("TAIL"))
    private void roomfortwo$rotateSleepingEntities(LivingEntityRenderState state, PoseStack poseStack, float bodyRot, float entityScale, CallbackInfo ci) {
        if (!state.hasPose(Pose.SLEEPING)) return;
        if (!(state instanceof AvatarRenderState avatarState)) return;

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        AABB bedArea = new AABB(
                state.x - 1.5, state.y - 1.0, state.z - 1.5,
                state.x + 1.5, state.y + 1.0, state.z + 1.5
        );

        List<LivingEntity> occupants = level.getEntitiesOfClass(LivingEntity.class, bedArea, LivingEntity::isSleeping);
        occupants.sort(Comparator.comparingInt(LivingEntity::getId));

        int index = 0;
        for (int i = 0; i < occupants.size(); i++) {
            if (occupants.get(i).getId() == avatarState.id) {
                index = i;
                break;
            }
        }

        if ((index & 1) == 0) {
            poseStack.translate(-0.25F, 0.0F, -0.15F);
            poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        }
        else {
            poseStack.translate(0.25F, 0.0F, -0.15F);
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        }
    }
}