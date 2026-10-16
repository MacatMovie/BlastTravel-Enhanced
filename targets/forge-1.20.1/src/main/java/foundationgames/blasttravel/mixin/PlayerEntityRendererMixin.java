package foundationgames.blasttravel.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import foundationgames.blasttravel.entity.CannonEntity;
import foundationgames.blasttravel.util.PlayerEntityDuck;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerEntityRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public PlayerEntityRendererMixin(EntityRendererProvider.Context context, PlayerModel<AbstractClientPlayer> model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"), cancellable = true)
    private void blasttravel$makePlayersInCannonsInvisible(AbstractClientPlayer player, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (player.getVehicle() instanceof CannonEntity) {
            ci.cancel();
        }
    }

    @Inject(method = "setupRotations(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFF)V", at = @At("HEAD"), cancellable = true)
    private void blasttravel$modifyPlayerAngles(AbstractClientPlayer player, PoseStack poseStack, float bob, float yBodyRot, float partialTick, CallbackInfo ci) {
        if (player instanceof PlayerEntityDuck duck && duck.blasttravel$inCannonFlight()) {
            var vel = duck.blasttravel$getVelocityLerped(partialTick);
            double horizontal = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
            super.setupRotations(player, poseStack, bob, 270 + ((float) Math.atan2(vel.z, vel.x) * Mth.RAD_TO_DEG), partialTick);
            poseStack.mulPose(Axis.XP.rotation((Mth.PI * 1.5F) + (float) Math.atan2(vel.y, horizontal)));
            ci.cancel();
        }
    }
}
