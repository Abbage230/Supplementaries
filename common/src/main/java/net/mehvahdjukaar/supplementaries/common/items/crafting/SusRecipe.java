package net.mehvahdjukaar.supplementaries.common.items.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.supplementaries.reg.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class SusRecipe extends CustomRecipe {
    private final Ingredient ingredient;
    private final ItemStack result;


    public SusRecipe(CraftingBookCategory craftingBookCategory, Ingredient ingredient, ItemStack itemStack) {
        super(craftingBookCategory);
        this.ingredient = ingredient;
        this.result = itemStack;
    }

    @Override
    public boolean matches(CraftingInput inv, Level level) {
        ItemStack gravel = null;
        ItemStack something = null;

        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (gravel == null && ingredient.test(stack)) {
                gravel = stack;
            } else if (!stack.isEmpty()) {
                if (something != null) {
                    return false;
                }
                something = stack;
            }
        }
        return gravel != null && something != null;
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider provider) {
        ItemStack gravel = null;
        ItemStack something = null;

        for (int i = 0; i < inv.size(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (gravel == null && ingredient.test(stack)) {
                gravel = stack;
            } else if (!stack.isEmpty()) {
                something = stack;
            }
        }
        ItemStack result = this.result.copy();
        result.applyComponents(gravel.getComponentsPatch());

        CompoundTag tag = new CompoundTag();
        tag.put("item", something.save(provider, new CompoundTag()));
        result.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(tag));

        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.SUS_CRAFTING.get();
    }

    public static class Serializer implements RecipeSerializer<SusRecipe> {

        public static final MapCodec<SusRecipe> CODEC = RecordCodecBuilder.mapCodec((i) -> i.group(
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC)
                        .forGetter(CustomRecipe::category),
                Ingredient.CODEC.fieldOf("ingredient").forGetter((arg) -> arg.ingredient),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter((arg) -> arg.result)
        ).apply(i, SusRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, SusRecipe> STREAM_CODEC = StreamCodec.composite(
                CraftingBookCategory.STREAM_CODEC, CustomRecipe::category,
                Ingredient.CONTENTS_STREAM_CODEC, i -> i.ingredient,
                ItemStack.STREAM_CODEC, i -> i.result,
                SusRecipe::new
        );

        @Override
        public MapCodec<SusRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SusRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
