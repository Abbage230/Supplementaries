package net.mehvahdjukaar.supplementaries.mixins.forge;

import net.mehvahdjukaar.moonlight.api.misc.OptionalMixin;
import net.mehvahdjukaar.supplementaries.integration.FarmersDelightCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.common.block.TomatoVineBlock;

@Pseudo
@Mixin(TomatoVineBlock.class)
public abstract class CompatFarmersDelightTomatoMixin extends Block {

    protected CompatFarmersDelightTomatoMixin(Properties arg) {
        super(arg);
    }

    @Inject(method = "attemptRopeClimb", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z",
            shift = At.Shift.BEFORE), cancellable = true, require = 0, remap = false)
    public void suppRopeCompat(ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (FarmersDelightCompat.tryTomatoLogging(level, pos.above())) ci.cancel();
    }
}
