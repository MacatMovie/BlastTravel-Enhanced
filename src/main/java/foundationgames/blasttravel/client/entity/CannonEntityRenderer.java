package foundationgames.blasttravel.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import foundationgames.blasttravel.BlastTravel;
import foundationgames.blasttravel.entity.CannonEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import com.mojang.math.Axis;

public class CannonEntityRenderer extends EntityRenderer<CannonEntity> {
    public static final ModelLayerLocation MODEL = new ModelLayerLocation(BlastTravel.id("cannon"), "main");

    private static final ResourceLocation[] FIRE_TEXTURES = {
            BlastTravel.id("textures/entity/cannon_fire/frame_0.png"),
            BlastTravel.id("textures/entity/cannon_fire/frame_1.png"),
            BlastTravel.id("textures/entity/cannon_fire/frame_2.png"),
            BlastTravel.id("textures/entity/cannon_fire/frame_3.png"),
            BlastTravel.id("textures/entity/cannon_fire/frame_4.png")
    };

    private static final ResourceLocation[] DYED_FIRE_TEXTURES = {
            BlastTravel.id("textures/entity/cannon_fire/dyed_frame_0.png"),
            BlastTravel.id("textures/entity/cannon_fire/dyed_frame_1.png"),
            BlastTravel.id("textures/entity/cannon_fire/dyed_frame_2.png"),
            BlastTravel.id("textures/entity/cannon_fire/dyed_frame_3.png"),
            BlastTravel.id("textures/entity/cannon_fire/dyed_frame_4.png")
    };

    private static final ResourceLocation[] SMOKE_TEXTURES = {
            BlastTravel.id("textures/entity/cannon_smoke/frame_0.png"),
            BlastTravel.id("textures/entity/cannon_smoke/frame_1.png"),
            BlastTravel.id("textures/entity/cannon_smoke/frame_2.png"),
            BlastTravel.id("textures/entity/cannon_smoke/frame_3.png"),
            BlastTravel.id("textures/entity/cannon_smoke/frame_4.png")
    };

    private final ModelPart root;
    private final ModelPart leftWheel;
    private final ModelPart rightWheel;
    private final ModelPart cannon;
    private final ModelPart chains;
    private final ModelPart fuse;
    private final ModelPart playerHead;
    private final ModelPart fire;

    public CannonEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.root = context.bakeLayer(MODEL).getChild("main");
        this.leftWheel = this.root.getChild("left_wheel");
        this.rightWheel = this.root.getChild("right_wheel");
        this.cannon = this.root.getChild("cannon");
        this.playerHead = this.root.getChild("player_head");
        this.fire = this.root.getChild("fire");
        this.chains = this.cannon.getChild("chains");
        this.fuse = this.cannon.getChild("fuse");
        this.resetModel();
    }

    private void resetModel() {
        this.leftWheel.xRot = this.rightWheel.xRot = 0;
        this.cannon.xRot = 0;
        this.cannon.visible = true;
        this.playerHead.visible = false;
        this.fuse.visible = true;
        this.chains.visible = false;
        this.fire.visible = false;
    }

    private static int argb(float alpha, float red, float green, float blue) {
        int a = Mth.clamp((int) (alpha * 255.0F), 0, 255);
        int r = Mth.clamp((int) (red * 255.0F), 0, 255);
        int g = Mth.clamp((int) (green * 255.0F), 0, 255);
        int b = Mth.clamp((int) (blue * 255.0F), 0, 255);
        return a << 24 | r << 16 | g << 8 | b;
    }

    @Override
    public ResourceLocation getTextureLocation(CannonEntity entity) {
        return entity.getBehavior().texture(entity.getBehaviorStack());
    }

    @Override
    public void render(CannonEntity entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
        poseStack.pushPose();
        var behavior = entity.getBehavior();

        // For the local rider, render directly from the player's interpolated view
        // rotation instead of the cannon entity's network/tick rotation. This keeps
        // third-person aiming smooth and avoids wrap/correction jumps from looking
        // like sudden spins. The entity rotation remains authoritative for gameplay.
        float renderYaw = yaw;
        float renderPitch = entity.getViewXRot(partialTick);
        if (!entity.hasChains() && entity.getClientPlayer() instanceof AbstractClientPlayer player && player.isLocalPlayer()) {
            renderYaw = player.getViewYRot(partialTick);
            renderPitch = Math.min(18.0F, player.getViewXRot(partialTick));
        }

        float yawRadians = (180 + renderYaw) * Mth.DEG_TO_RAD;
        float wheelAngle = yawRadians * 1.2F;

        this.leftWheel.xRot = wheelAngle;
        this.rightWheel.xRot = -wheelAngle;
        this.cannon.xRot = (renderPitch + 90) * Mth.DEG_TO_RAD;

        boolean renderCannon = entity.getFirstPassenger() != Minecraft.getInstance().player || !Minecraft.getInstance().options.getCameraType().isFirstPerson();
        this.cannon.visible = renderCannon;
        this.chains.visible = entity.hasChains();
        this.fuse.visible = entity.hasFuse();

        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        poseStack.mulPose(Axis.YP.rotation(yawRadians));

        float anim = entity.getAnimation(partialTick);
        poseStack.mulPose(Axis.XP.rotationDegrees(-5 * (-2 * (anim * anim * anim * anim * anim * anim * anim * anim) + 2 * (anim * anim))));

        this.root.render(poseStack, buffer.getBuffer(RenderType.entityCutout(this.getTextureLocation(entity))), packedLight, OverlayTexture.NO_OVERLAY);

        var color = behavior.fireColor(entity);
        int tick = Math.max(0, (CannonEntity.MAX_ANIMATION - entity.getAnimationTick()) - 3);
        if (tick <= 7) {
            this.fire.visible = true;
            this.fire.xRot = this.cannon.xRot;
        }
        if (tick <= 4) {
            if (color != null) {
                this.fire.render(poseStack, buffer.getBuffer(RenderType.eyes(DYED_FIRE_TEXTURES[tick])), packedLight, OverlayTexture.NO_OVERLAY, argb(1.0F, color.x(), color.y(), color.z()));
            } else {
                this.fire.render(poseStack, buffer.getBuffer(RenderType.eyes(FIRE_TEXTURES[tick])), packedLight, OverlayTexture.NO_OVERLAY);
            }
        }
        tick -= 3;
        if (tick >= 0 && tick <= 4) {
            this.fire.render(poseStack, buffer.getBuffer(RenderType.entityTranslucent(SMOKE_TEXTURES[tick])), packedLight, OverlayTexture.NO_OVERLAY, argb(0.9F - (0.07F * tick), 1.0F, 1.0F, 1.0F));
        }

        if (renderCannon && behavior.displayHead(entity)) {
            this.playerHead.visible = true;
            this.playerHead.xRot = this.cannon.xRot;
            color = behavior.headColor(entity);

            ResourceLocation headTexture = behavior.headTexture(entity);
            if (entity.getClientPlayer() instanceof AbstractClientPlayer player) {
                headTexture = player.getSkin().texture();
            }

            this.playerHead.render(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(headTexture)), packedLight, OverlayTexture.NO_OVERLAY, argb(1.0F, color.x(), color.y(), color.z()));
        }

        this.resetModel();
        poseStack.popPose();
    }
}
