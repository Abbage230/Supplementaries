package net.mehvahdjukaar.supplementaries.common.block.dispenser;

import net.mehvahdjukaar.moonlight.api.util.DispenserHelper;
import net.mehvahdjukaar.supplementaries.common.block.tiles.JarBlockTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;

class FishBucketJarBehavior extends DispenserHelper.AdditionalDispenserBehavior {

    protected FishBucketJarBehavior(Item item) {
        super(item);
    }

    @Override
    protected InteractionResultHolder<ItemStack> customBehavior(BlockSource source, ItemStack stack) {
        //this.setSuccessful(false);
        ServerLevel world = source.getLevel();
        BlockPos blockpos = source.getPos().relative(source.getBlockState().getValue(DispenserBlock.FACING));
        if (world.getBlockEntity(blockpos) instanceof JarBlockTile tile) {
            //TODO: add fish buckets
            if (tile.fluidHolder.isEmpty() && tile.isEmpty()) {
                if (tile.mobContainer.interactWithBucket(stack, world, blockpos, null, null)) {
                    tile.setChanged();
                    return InteractionResultHolder.success(new ItemStack(Items.BUCKET));
                }
            }
            return InteractionResultHolder.fail(stack);
        }
        return InteractionResultHolder.pass(stack);
    }
}

