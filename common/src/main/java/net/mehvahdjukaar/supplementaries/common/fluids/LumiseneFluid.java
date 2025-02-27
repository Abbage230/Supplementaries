package net.mehvahdjukaar.supplementaries.common.fluids;

import net.mehvahdjukaar.supplementaries.reg.ModFluids;
import net.mehvahdjukaar.supplementaries.reg.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import static net.mehvahdjukaar.supplementaries.reg.ModFluids.LUMISENE_MAX_LAYERS;

public class LumiseneFluid extends FiniteFluid {
    public LumiseneFluid() {
        super(LUMISENE_MAX_LAYERS, ModFluids.LUMISENE_BLOCK, ModFluids.LUMISENE_BUCKET);
    }

    @Override
    protected void animateTick(Level level, BlockPos pos, FluidState state, RandomSource random) {
        super.animateTick(level, pos, state, random);
        if (random.nextInt(12) == 0) {
            BlockState above = level.getBlockState(pos.above());
            if (!above.getFluidState().isEmpty()) return;
            BlockState blockState = level.getBlockState(pos);
            if (blockState.getBlock() instanceof FlammableLiquidBlock fb &&
                    !fb.isLitUp(blockState, level, pos)) {
                level.addParticle(ModParticles.SPARKLE_PARTICLE.get(),
                        pos.getX() + random.nextDouble(),
                        pos.getY() + getOwnHeight(state) + 1 / 32f,
                        pos.getZ() + random.nextDouble(),
                        0, 0, 0);
            }
        }
    }
}
