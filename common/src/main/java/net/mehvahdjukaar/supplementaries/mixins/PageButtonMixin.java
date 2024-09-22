package net.mehvahdjukaar.supplementaries.mixins;

import net.mehvahdjukaar.supplementaries.common.block.IAntiquable;
import net.mehvahdjukaar.supplementaries.reg.ModTextures;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PageButton.class)
public abstract class PageButtonMixin implements IAntiquable {

    @Unique
    private boolean supplementaries$antiqueInk;

    @Override
    public boolean isAntique() {
        return supplementaries$antiqueInk;
    }

    @Override
    public void setAntique(boolean hasInk) {
        this.supplementaries$antiqueInk = hasInk;
    }

}
