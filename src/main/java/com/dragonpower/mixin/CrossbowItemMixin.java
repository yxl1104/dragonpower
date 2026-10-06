package com.dragonpower.mixin;

import com.dragonpower.DragonPowerMod;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin {
    @Inject(method = "shoot", at = @At("HEAD"), cancellable = true)
    private void fireDragonArrow(ItemStack stack, World world, PlayerEntity user, ItemStack projectile, float soundPitch, CallbackInfo ci) {
        if (!DragonPowerMod.dragonPowerEnabled) return;
        if (world.isClient) return;

        final double ARROW_SPEED_MULTIPLIER = 1.5;
        final double BASE_ARROW_SPEED = 3.0;

        ArrowEntity arrow = new ArrowEntity(EntityType.ARROW, world);
        arrow.setPosition(user.getEyePos());
        arrow.setVelocity(
                user.getRotationVector().x * BASE_ARROW_SPEED * ARROW_SPEED_MULTIPLIER,
                user.getRotationVector().y * BASE_ARROW_SPEED * ARROW_SPEED_MULTIPLIER,
                user.getRotationVector().z * BASE_ARROW_SPEED * ARROW_SPEED_MULTIPLIER
        );
        arrow.setDamage(0.0);
        arrow.setCritical(false);
        arrow.pickupType = ArrowEntity.PickupPermission.CREATIVE_ONLY;

        world.spawnEntity(arrow);

        world.playSound(
                null,
                user.getX(),
                user.getY(),
                user.getZ(),
                SoundEvents.ENTITY_ENDER_DRAGON_SHOOT,
                SoundCategory.PLAYERS,
                1.0F,
                1.0F
        );
        ci.cancel();
    }

    @Inject(method = "loadProjectile", at = @At("HEAD"), cancellable = true)
    private void loadWithoutAmmo(World world, PlayerEntity user, ItemStack crossbow, ItemStack projectile, boolean creative, CallbackInfo ci) {
        if (!DragonPowerMod.dragonPowerEnabled) return;
        CrossbowItem.setCharged(crossbow, true);
        ci.cancel();
    }
}
