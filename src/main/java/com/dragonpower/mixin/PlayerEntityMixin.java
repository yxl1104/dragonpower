package com.dragonpower.mixin;

import com.dragonpower.DragonPowerClient;
import com.dragonpower.DragonPowerMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.boss.dragon.EndCrystalEntity;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.damage.DamageSource;
import net.minecraft.world.damage.DamageTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    @Shadow public net.minecraft.entity.player.PlayerAbilities abilities;
    private boolean isDashing;
    private int crystalBonusHealth = 0;
    private int dragonSoundTimer;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "getMaxHealth", at = @At("RETURN"), cancellable = true)
    private void modifyMaxHealth(CallbackInfoReturnable<Float> cir) {
        if(!DragonPowerMod.dragonPowerEnabled) return;
        cir.setReturnValue(cir.getReturnValue() + 20 + crystalBonusHealth);
    }

    @Inject(method = "getArmor", at = @At("RETURN"), cancellable = true)
    private void addArmor(CallbackInfoReturnable<Integer> cir) {
        if(!DragonPowerMod.dragonPowerEnabled) return;
        cir.setReturnValue(cir.getReturnValue() + 8);
    }

    @Inject(method = "attack", at = @At("HEAD"))
    private void swordDamageBoost(Entity target, CallbackInfo ci) {
        if(!DragonPowerMod.dragonPowerEnabled) return;
        PlayerEntity self = (PlayerEntity)(Object)this;
        if(self.getStackInHand(Hand.MAIN_HAND).getItem() instanceof net.minecraft.item.SwordItem){
            target.damage(self.getWorld().getDamageSources().playerAttack(self), 6.0f);
        }
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if(DragonPowerMod.dragonPowerEnabled && source.isOf(DamageTypes.EXPLOSION)){
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void immunityTick(CallbackInfo ci) {
        if (!DragonPowerMod.dragonPowerEnabled) return;
        PlayerEntity self = (PlayerEntity)(Object)this;
        self.getStatusEffects().forEach(effect -> {
            if (!effect.getEffectType().isBeneficial()) {
                self.removeStatusEffect(effect.getEffectType());
            }
        });
        self.setAir(self.getMaxAir());
        self.fireTicks = 0;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void dragonTick(CallbackInfo ci){
        PlayerEntity self = (PlayerEntity)(Object)this;
        if(!DragonPowerMod.dragonPowerEnabled){
            abilities.allowFlying = false;
            abilities.flying = false;
            crystalBonusHealth = 0;
            isDashing = false;
            dragonSoundTimer = 0;
            return;
        }

        abilities.allowFlying = true;
        abilities.flying = true;

        if(self.world.isClient()){
            isDashing = DragonPowerClient.dashKey.isPressed();

            if(isDashing){
                self.playSound(SoundEvents.ITEM_ELYTRA_FLYING, 0.8F, 1.0F);
                dragonSoundTimer = 0;
            }else{
                dragonSoundTimer++;
                if(dragonSoundTimer >= 80){
                    self.playSound(SoundEvents.ENTITY_ENDER_DRAGON_AMBIENT, 0.9F, 1.0F);
                    dragonSoundTimer = 0;
                }
            }

            if(isDashing){
                for(int i = 0; i < 8; i++){
                    self.world.addParticle(
                            new DustParticleEffect(0.6f,0.2f,0.9f,1.0f),
                            self.getX() + (self.getRandom().nextDouble()-0.5)*0.5,
                            self.getY() + (self.getRandom().nextDouble()-0.5)*0.5,
                            self.getZ() + (self.getRandom().nextDouble()-0.5)*0.5,
                            -self.getVelocity().x*0.2,
                            -self.getVelocity().y*0.2,
                            -self.getVelocity().z*0.2
                    );
                }
            }else if(abilities.flying){
                for(int i = 0; i < 2; i++){
                    self.world.addParticle(
                            new DustParticleEffect(0.6f,0.2f,0.9f,1.0f),
                            self.getX() + (self.getRandom().nextDouble()-0.5)*0.3,
                            self.getY() + (self.getRandom().nextDouble()-0.5)*0.3,
                            self.getZ() + (self.getRandom().nextDouble()-0.5)*0.3,
                            0,0,0
                    );
                }
            }

            var crystalList = self.world.getEntitiesByClass(EndCrystalEntity.class, self.getBoundingBox().expand(10), e->true);
            for(EndCrystalEntity crystal : crystalList){
                Vec3d start = crystal.getPos().add(0,1,0);
                Vec3d end = self.getPos().add(0, self.getHeight()*0.5,0);
                Vec3d delta = end.subtract(start);
                double distance = delta.length();
                int particleCount = (int) Math.ceil(distance * 2);
                for(int p=0;p<particleCount;p++){
                    double t = (double)p / particleCount;
                    Vec3d pos = start.add(delta.multiply(t));
                    self.world.addParticle(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 0,0,0);
                }
            }

            while(DragonPowerClient.fireDragonBallKey.wasPressed()){
                DragonFireballEntity fireball = new DragonFireballEntity(self.world, self, self.getRotationVector().x, self.getRotationVector().y, self.getRotationVector().z);
                fireball.setPosition(self.getEyePos());
                fireball.explosionPower = 2;
                self.world.spawnEntity(fireball);
                self.world.playSound(null, self.getPos(), SoundEvents.ENTITY_ENDER_DRAGON_SHOOT, net.minecraft.sound.SoundCategory.PLAYERS,1.0F,1.0F);
            }
        }

        if(!self.world.isClient()){
            final double DASH_SPEED_MULTIPLIER = 1.2;
            final double BASE_ELYTRA_FIREWORK_SPEED = 1.25;

            if(isDashing){
                Vec3d dir = self.getRotationVector().multiply(BASE_ELYTRA_FIREWORK_SPEED * DASH_SPEED_MULTIPLIER);
                self.addVelocity(dir.x, dir.y, dir.z);
                self.setVelocity(self.getVelocity().add(0, -self.getGravity(), 0));

                Box hitBox = self.getBoundingBox().expand(5);
                self.world.getBlockStates(hitBox).forEach(pos -> {
                    if(self.world.canBreak(pos, self)){
                        // 保留破碎粒子+音效，不生成方块掉落物
                        self.world.breakBlock(pos, false, self);
                    }
                });
                self.world.getEntitiesByClass(LivingEntity.class, hitBox, e->e != self).forEach(e->{
                    e.damage(self.world.getDamageSources().playerAttack(self),20);
                });
            }

            var crystalList = self.world.getEntitiesByClass(EndCrystalEntity.class, self.getBoundingBox().expand(10), e->true);
            long crystalCount = crystalList.size();
            if(crystalCount > 0){
                crystalBonusHealth = (int)(crystalCount * 4);
                if(self.getHealth() < self.getMaxHealth()){
                    self.heal(0.05f);
                }
            }else{
                crystalBonusHealth = 0;
            }
        }
    }
}
