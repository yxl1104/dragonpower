package com.dragonpower.mixin;

import com.dragonpower.DragonPowerMod;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArrowEntity.class)
public abstract class ArrowEntityMixin {

    @Inject(method = "onHit", at = @At("HEAD"))
    private void arrowHitSpawnDragonFireball(HitResult hitResult, CallbackInfo ci) {
        ArrowEntity arrow = (ArrowEntity) (Object) this;
        World world = arrow.world;

        if (!DragonPowerMod.dragonPowerEnabled || world.isClient()) {
            return;
        }

        if (hitResult.getType() == HitResult.Type.ENTITY || hitResult.getType() == HitResult.Type.BLOCK) {
            DragonFireballEntity fireball = new DragonFireballEntity(world, null, 0, 0, 0);
            fireball.setPosition(hitResult.getPos());
            fireball.explosionPower = 2;
            world.spawnEntity(fireball);
            arrow.discard();
        }
    }
}

