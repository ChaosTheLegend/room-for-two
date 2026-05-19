package sircow.roomfortwo.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BedBlock.class)
public class BedBlockMixin {
    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void allowInfiniteBedSharing(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (level.isClientSide()) {
            cir.setReturnValue(InteractionResult.SUCCESS_SERVER);
            return;
        }

        if (state.getValue(BlockStateProperties.BED_PART) != BedPart.HEAD) {
            pos = pos.relative(state.getValue(BlockStateProperties.HORIZONTAL_FACING));
            state = level.getBlockState(pos);
            if (!state.is((BedBlock) (Object) this)) {
                cir.setReturnValue(InteractionResult.CONSUME);
                return;
            }
        }

        player.startSleepInBed(pos).ifLeft(problem -> {
            if (problem.message() != null) player.sendOverlayMessage(problem.message());
        });

        if (state.getValue(BlockStateProperties.OCCUPIED)) {
            level.setBlock(pos, state.setValue(BlockStateProperties.OCCUPIED, false), 3);
        }

        cir.setReturnValue(InteractionResult.SUCCESS_SERVER);
    }
}
