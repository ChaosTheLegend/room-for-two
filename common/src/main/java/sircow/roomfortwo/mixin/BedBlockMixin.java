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
    private void roomfortwo$use(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (level.isClientSide()) {
            cir.setReturnValue(InteractionResult.SUCCESS_SERVER);
            return;
        }

        BlockPos bedPos = pos;
        BlockState bedState = state;

        if (bedState.getValue(BlockStateProperties.BED_PART) != BedPart.HEAD) {
            bedPos = bedPos.relative(bedState.getValue(BlockStateProperties.HORIZONTAL_FACING));
            bedState = level.getBlockState(bedPos);

            if (!bedState.is((BedBlock) (Object) this)) {
                cir.setReturnValue(InteractionResult.CONSUME);
                return;
            }
        }

        player.startSleepInBed(bedPos).ifLeft(problem -> {
            if (problem.message() != null) player.sendOverlayMessage(problem.message());
        });

        cir.setReturnValue(InteractionResult.SUCCESS_SERVER);
    }
}
