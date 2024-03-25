package net.mehvahdjukaar.supplementaries.reg.fabric;

import net.mehvahdjukaar.supplementaries.common.fluids.FiniteFluid;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluids;

public class ModFluidsImpl {
    public static BucketItem createLumiseneBucket() {
        return new BucketItem(Fluids.WATER, new Item.Properties());
    }

    public static FiniteFluid createLumisene() {
        return new LumiseneFluid();
    }

    public static class LumiseneFluid extends FiniteFluid {
        public LumiseneFluid() {
            super(16, null, null);
        }

    }

}
