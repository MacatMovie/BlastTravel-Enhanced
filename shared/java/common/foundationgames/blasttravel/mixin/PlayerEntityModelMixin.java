package foundationgames.blasttravel.mixin;

import foundationgames.blasttravel.util.PlayerEntityDuck;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public abstract class PlayerEntityModelMixin<T extends LivingEntity> extends HumanoidModel<T> {
    @Unique private LivingEntity blasttravel$cached;

    public PlayerEntityModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(method = "setupAnim", at = @At("HEAD"))
    private void blasttravel$cacheEntity(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        this.blasttravel$cached = entity;
    }

    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void blasttravel$setHeadAngle(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (entity instanceof PlayerEntityDuck duck && duck.blasttravel$inCannonFlight()) {
            this.head.xRot = -0.5F * Mth.PI;
            this.head.yRot = 0;
            this.head.zRot = 0;
            this.hat.copyFrom(this.head);
        }
    }

    @ModifyVariable(method = "setupAnim", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private float blasttravel$overrideLimbAngles(float old) {
        if (this.blasttravel$cached instanceof PlayerEntityDuck duck && duck.blasttravel$inCannonFlight()) {
            this.blasttravel$cached = null;
            return 0.75F * Mth.PI;
        }
        return old;
    }
}
