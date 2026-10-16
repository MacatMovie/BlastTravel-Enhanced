package foundationgames.blasttravel.mixin;

import foundationgames.blasttravel.util.BTNetworking;
import foundationgames.blasttravel.util.PlayerEntityDuck;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerEntityMixin implements PlayerEntityDuck {
    private static final EntityDimensions CANNON_FLIGHT_DIMENSIONS = EntityDimensions.scalable(0.6F, 0.6F);

    private Vec3 blasttravel$vel = Vec3.ZERO;
    private Vec3 blasttravel$trackingVel = Vec3.ZERO;
    private Vec3 blasttravel$prevVel = Vec3.ZERO;
    private Vec3 blasttravel$flightVelocity = Vec3.ZERO;
    private boolean blasttravel$inCannonFlight = false;
    private boolean blasttravel$cancelFallDamage = false;
    private int blasttravel$ticksFlying = 0;

    @Inject(method = "tick", at = @At("HEAD"))
    private void blasttravel$beginTick(CallbackInfo ci) {
        var self = (Player) (Object) this;
        this.blasttravel$prevVel = self.isLocalPlayer() ? blasttravel$vel : blasttravel$trackingVel;

        if (this.blasttravel$inCannonFlight() && !self.isPassenger()) {
            self.setDeltaMovement(this.blasttravel$flightVelocity);
            self.hasImpulse = true;
            self.setDiscardFriction(true);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void blasttravel$endTick(CallbackInfo ci) {
        var self = (Player) (Object) this;
        this.blasttravel$vel = self.position().subtract(self.xo, self.yo, self.zo);

        if (this.blasttravel$inCannonFlight()) {
            if (!self.level().isClientSide) {
                var vel = self.getDeltaMovement();
                var frontBox = self.getBoundingBox().inflate(0.2, 0.2, 0.2);
                for (var entity : self.level().getEntitiesOfClass(LivingEntity.class, frontBox, entity -> entity != self)) {
                    if (!entity.isInvulnerable()) {
                        entity.hurt(self.damageSources().playerAttack(self), (float) (vel.length() * 4));
                    }
                }
            }

            if (self.isLocalPlayer() && this.blasttravel$ticksFlying > 12 &&
                    (self.onGround() || self.isFallFlying() || self.getAbilities().flying || self.isInWater())) {
                this.blasttravel$setCannonFlight(false);
                BTNetworking.c2sStopCannonFlight(self.onGround());
            }

            this.blasttravel$flightVelocity = self.getDeltaMovement();
            this.blasttravel$ticksFlying++;
        } else {
            this.blasttravel$ticksFlying = 0;
        }

        if (!self.isLocalPlayer()) {
            this.blasttravel$trackingVel = this.blasttravel$trackingVel.add(this.blasttravel$vel.subtract(this.blasttravel$trackingVel).scale(1F / self.getType().updateInterval()));
        }
    }

    @Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
    private void blasttravel$cancelFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if (this.blasttravel$cancelFallDamage) {
            this.blasttravel$cancelFallDamage = false;
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getDimensions", at = @At("HEAD"), cancellable = true)
    private void blasttravel$setFlyingPose(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        if (this.blasttravel$inCannonFlight()) {
            cir.setReturnValue(CANNON_FLIGHT_DIMENSIONS);
        }
    }

    @Override
    public void blasttravel$setCannonFlight(boolean inFlight) {
        var self = (Player) (Object) this;
        if (inFlight && !this.blasttravel$inCannonFlight) {
            this.blasttravel$vel = this.blasttravel$trackingVel = this.blasttravel$prevVel = self.getDeltaMovement();
            if (self.getDeltaMovement().lengthSqr() > 1.0E-7) {
                this.blasttravel$flightVelocity = self.getDeltaMovement();
            }
            this.blasttravel$cancelFallDamage = true;
            self.setJumping(false);
        }

        this.blasttravel$inCannonFlight = inFlight;
        if (!inFlight) {
            this.blasttravel$flightVelocity = Vec3.ZERO;
        }
        self.setDiscardFriction(inFlight);
        self.refreshDimensions();
    }

    @Override
    public void blasttravel$setCannonFlightVelocity(Vec3 velocity) {
        this.blasttravel$flightVelocity = velocity;
        this.blasttravel$vel = this.blasttravel$trackingVel = this.blasttravel$prevVel = velocity;
    }

    @Override
    public boolean blasttravel$inCannonFlight() {
        return this.blasttravel$inCannonFlight;
    }

    @Override
    public Vec3 blasttravel$getVelocityLerped(float delta) {
        var vel = ((Player) (Object) this).isLocalPlayer() ? blasttravel$vel : blasttravel$trackingVel;
        return new Vec3(Mth.lerp(delta, blasttravel$prevVel.x, vel.x), Mth.lerp(delta, blasttravel$prevVel.y, vel.y), Mth.lerp(delta, blasttravel$prevVel.z, vel.z));
    }
}
