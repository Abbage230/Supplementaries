package net.mehvahdjukaar.supplementaries.common.items;

import com.mojang.blaze3d.vertex.PoseStack;
import net.mehvahdjukaar.moonlight.api.item.IFirstPersonAnimationProvider;
import net.mehvahdjukaar.moonlight.api.item.IThirdPersonAnimationProvider;
import net.mehvahdjukaar.moonlight.api.item.additional_placements.AdditionalItemPlacementsAPI;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.supplementaries.common.entities.SlingshotProjectileEntity;
import net.mehvahdjukaar.supplementaries.common.events.overrides.InteractEventsHandler;
import net.mehvahdjukaar.supplementaries.configs.CommonConfigs;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.mehvahdjukaar.supplementaries.reg.ModSounds;
import net.mehvahdjukaar.supplementaries.reg.ModTags;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class SlingshotItem extends ProjectileWeaponItem implements IFirstPersonAnimationProvider, IThirdPersonAnimationProvider {

    public SlingshotItem(Properties properties) {
        super(properties);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player) {

            ItemStack projectileStack = player.getProjectile(stack);

            if (!projectileStack.isEmpty() && this.getAllSupportedProjectiles().test(projectileStack)) {

                float power = getPowerForTime(entity, stack, timeLeft);
                if ((power >= 0.085D)) {

                    int maxProjectiles = 0;//

                    List<ItemStack> projectiles = new ArrayList<>();

                    for (int p = 0; p < maxProjectiles; p++) {
                        if (!this.getAllSupportedProjectiles().test(projectileStack)) {
                            break;
                        }
                        projectiles.add(projectileStack.copy());
                        if (!player.getAbilities().instabuild) {
                            projectileStack.shrink(1);
                            if (projectileStack.isEmpty()) {
                                player.getInventory().removeItem(projectileStack);
                            }
                        }
                        projectileStack = player.getProjectile(stack);
                    }
                    if (!world.isClientSide) {
                        float[] pitches = getShotPitches(world.getRandom());
                        int count = projectiles.size();
                        float angle = 10;
                        for (int j = 0; j < count; j++) {

                            boolean stasis = false;
                            InteractionHand hand = player.getUsedItemHand();
                            power *= (float) ((CommonConfigs.Tools.SLINGSHOT_RANGE.get() + (stasis ? 0.5 : 0)) * 1.1);
                            shootProjectile(world, entity, hand, stack, projectiles.get(j), count == 1 ? 1 : pitches[j], power, 1,
                                    angle * (j - (count - 1) / 2f));
                        }
                    }
                    player.awardStat(Stats.ITEM_USED.get(this));
                }
            }
        }
    }

    private static void shootProjectile(Level level, LivingEntity entity, InteractionHand hand, ItemStack stack, ItemStack projectileStack,
                                        float soundPitch, float power, float accuracy, float yaw) {

        projectileStack.setCount(1);
        SlingshotProjectileEntity projectile = new SlingshotProjectileEntity(level, projectileStack, stack, entity);

        Vec3 vector3d1 = entity.getUpVector(1.0F);
        Quaternionf quaternionf = new Quaternionf().setAngleAxis(yaw * 0.017453292F, vector3d1.x(), vector3d1.y(), vector3d1.z());

        Vector3f vector3f = entity.getViewVector(1.0F).toVector3f();
        vector3f.rotate(quaternionf);
        projectile.shoot(vector3f.x(), vector3f.y(), vector3f.z(), power, accuracy);

        level.addFreshEntity(projectile);

        level.playSound(null, entity, ModSounds.SLINGSHOT_SHOOT.get(), SoundSource.PLAYERS, 1.0F,
                soundPitch * (1.0F / (level.random.nextFloat() * 0.3F + 0.9F) + power * 0.6F));

    }

    //shoot pitches for multi shot
    protected float[] getShotPitches(RandomSource random) {
        boolean flag = random.nextBoolean();
        return new float[]{getRandomShotPitch(random, flag), 1.0F, getRandomShotPitch(random, !flag)};
    }

    protected float getRandomShotPitch(RandomSource random, boolean left) {
        float f = left ? 0.63F : 0.43F;
        return 1.0F / (random.nextFloat() * 0.5F + 1.8F) + f;
    }

    public float getPowerForTime(LivingEntity e, ItemStack stack, float timeLeft) {
        float useTime = this.getUseDuration(stack, e) - timeLeft;
        float f = useTime / getChargeDuration(stack);
        //parabolic power increase
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }

    //this is max use time

    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
        return 67777;
    }

    //actual use duration
    public static int getChargeDuration(ItemStack stack) {
        int i = 0;
        int maxCharge = CommonConfigs.Tools.SLINGSHOT_CHARGE.get();
        return i == 0 ? maxCharge : maxCharge - (maxCharge / 4) * i;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        boolean flag = this.getAllSupportedProjectiles().test(player.getProjectile(itemstack));

        if (!flag) {
            return InteractionResultHolder.fail(itemstack);
        } else {
            player.startUsingItem(hand);
            player.level().playSound(player, player,
                    getChargeSound(itemstack), SoundSource.PLAYERS, 1.0F,
                    1 * (1.0F / (world.random.nextFloat() * 0.3F + 0.9F)));
            return InteractionResultHolder.consume(itemstack);
        }
    }

    public SoundEvent getChargeSound(ItemStack stack) {
        int i =0;
        return switch (i) {
            case 0 -> ModSounds.SLINGSHOT_CHARGE_0.get();
            case 1 -> ModSounds.SLINGSHOT_CHARGE_1.get();
            case 2 -> ModSounds.SLINGSHOT_CHARGE_2.get();
            default -> ModSounds.SLINGSHOT_CHARGE_3.get();
        };
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return s -> {
            Item i = s.getItem();
            //no buckets
            if (i instanceof ThrowablePotionItem) {
                return CommonConfigs.Tools.SLINGSHOT_POTIONS.get();
            }
            if (i instanceof BombItem) {
                return CommonConfigs.Tools.SLINGSHOT_BOMBS.get();
            }
            if (i instanceof SnowballItem) {
                return CommonConfigs.Tools.SLINGSHOT_SNOWBALL.get();
            }
            if (i instanceof EnderpearlItem) {
                return CommonConfigs.Tools.SLINGSHOT_ENDERPEARLS.get();
            }
            if (i instanceof FireChargeItem) {
                return CommonConfigs.Tools.SLINGSHOT_FIRECHARGE.get();
            }
            if (i instanceof DispensibleContainerItem && i.hasCraftingRemainingItem()) {
                return CommonConfigs.Tools.SLINGSHOT_BUCKETS.get();
            }
            if (s.is(ModTags.SLINGSHOT_DAMAGEABLE)) {
                return true;
            }
            return !(i instanceof DispensibleContainerItem || s.is(ModTags.SLINGSHOT_BLACKLIST)) &&
                    i instanceof BlockItem ||
                    AdditionalItemPlacementsAPI.hasBehavior(i) ||
                    InteractEventsHandler.hasBlockPlacementAssociated(i);
        };
    }

    @Override
    public int getDefaultProjectileRange() {
        return 10;
    }

    @Override
    protected void shootProjectile(LivingEntity shooter, Projectile projectile, int index, float velocity, float inaccuracy, float angle, @Nullable LivingEntity target) {

    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        //need to use NONE for custom one
        return UseAnim.NONE;
    }

    @Override
    public <T extends LivingEntity> boolean poseLeftArm(ItemStack stack, HumanoidModel<T> model, T entity, HumanoidArm mainHand) {
        if (entity.getUseItemRemainingTicks() > 0 && entity.getUseItem().getItem() == this) {
            model.leftArm.yRot = MthUtils.wrapRad(0.1F + model.head.yRot);
            model.leftArm.xRot = MthUtils.wrapRad((-(float) Math.PI / 2F) + model.head.xRot);
            return true;
        }
        return false;
    }

    //TODO: finish this
    @Override
    public <T extends LivingEntity> boolean poseRightArm(ItemStack stack, HumanoidModel<T> model, T entity, HumanoidArm mainHand) {
        if (entity.getUseItemRemainingTicks() > 0 && entity.getUseItem().getItem() == this) {
            model.rightArm.yRot = MthUtils.wrapRad(-0.1F + model.head.yRot);
            //model.leftArm.yRot = 0.1F + model.head.yRot + 0.4F;
            model.rightArm.xRot = MthUtils.wrapRad((-(float) Math.PI / 2F) + model.head.xRot);
            //model.leftArm.xRot = (-(float) Math.PI / 2F) + model.head.xRot;

            /*
            model.leftArm.xRot = model.rightArm.xRot;
            float f = (float) SlingshotItem.getChargeDuration(entity.getUseItem());
            float f1 = MathHelper.clamp((float) entity.getTicksUsingItem(), 0.0F, f);
            float f2 = f1 / f;

            model.leftArm.yRot = (float) (0.1F + model.head.yRot + MathHelper.lerp(f2, ClientConfigs.general.TEST1.get(), ClientConfigs.general.TEST2.get()) * (float) (true ? 1 : -1));
            */
            //if(ClientConfigs.general.TEST3.get()<0)
            // model.leftArm.xRot = (float) (1f*ClientConfigs.general.TEST3.get());//MathHelper.lerp(f2, model.leftArm.xRot, (-(float) Math.PI / 2F));

            //animateCrossbowCharge(model.leftArm, model.leftArm, entity, mainHand == HandSide.RIGHT);
            return true;
        }
        return false;
    }


    @Override
    public void animateItemFirstPerson(Player player, ItemStack itemStack, InteractionHand interactionHand, HumanoidArm humanoidArm, PoseStack poseStack, float v, float v1, float v2, float v3) {

    }
}
