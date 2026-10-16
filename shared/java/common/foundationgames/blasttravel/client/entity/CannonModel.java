package foundationgames.blasttravel.client.entity;

import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.PartPose;

/** Vanilla layer generated from the original JsonEM cannon model. */
public final class CannonModel {
    private CannonModel() {}

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition main = root.addOrReplaceChild("main", CubeListBuilder.create()
                .texOffs(36, 0).addBox(-6F, -5F, -1F, 12F, 2F, 2F, new CubeDeformation(0F))
                .texOffs(36, 54).addBox(-4F, -6F, -3F, 8F, 4F, 6F, new CubeDeformation(0F)), PartPose.offset(0F, -1F, 0F));
        PartDefinition left_wheel = main.addOrReplaceChild("left_wheel", CubeListBuilder.create()
                .texOffs(0, 22).addBox(6F, -1F, -1F, 2F, 2F, 2F, new CubeDeformation(0F))
                .texOffs(18, 21).addBox(7F, -3.5F, -3.5F, 0F, 7F, 7F, new CubeDeformation(0F))
                .texOffs(0, 0).addBox(6F, 3F, -2.05F, 2F, 2F, 4.125F, new CubeDeformation(0F)), PartPose.offset(0F, -4F, 0F));
        PartDefinition seg_1 = left_wheel.addOrReplaceChild("seg_1", CubeListBuilder.create()
                .texOffs(18, 21).addBox(7F, -3.5F, -3.5F, 0F, 7F, 7F, new CubeDeformation(0F))
                .texOffs(0, 0).addBox(6F, 3F, -2.05F, 2F, 2F, 4.125F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 0F, -0.7853982F, 0F, 0F));
        PartDefinition seg_2 = left_wheel.addOrReplaceChild("seg_2", CubeListBuilder.create()
                .texOffs(0, 0).addBox(6F, 3F, -2.05F, 2F, 2F, 4.125F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 0F, -1.5707963F, 0F, 0F));
        PartDefinition seg_3 = left_wheel.addOrReplaceChild("seg_3", CubeListBuilder.create()
                .texOffs(0, 0).addBox(6F, 3F, -2.05F, 2F, 2F, 4.125F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 0F, -2.3561945F, 0F, 0F));
        PartDefinition seg_4 = left_wheel.addOrReplaceChild("seg_4", CubeListBuilder.create()
                .texOffs(0, 0).addBox(6F, 3F, -2.05F, 2F, 2F, 4.125F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 0F, 3.1415927F, 0F, 0F));
        PartDefinition seg_5 = left_wheel.addOrReplaceChild("seg_5", CubeListBuilder.create()
                .texOffs(0, 0).addBox(6F, 3F, -2.05F, 2F, 2F, 4.125F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 0F, 2.3561945F, 0F, 0F));
        PartDefinition seg_6 = left_wheel.addOrReplaceChild("seg_6", CubeListBuilder.create()
                .texOffs(0, 0).addBox(6F, 3F, -2.05F, 2F, 2F, 4.125F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 0F, 1.5707963F, 0F, 0F));
        PartDefinition seg_7 = left_wheel.addOrReplaceChild("seg_7", CubeListBuilder.create()
                .texOffs(0, 0).addBox(6F, 3F, -2.05F, 2F, 2F, 4.125F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 0F, 0.7853982F, 0F, 0F));
        PartDefinition right_wheel = main.addOrReplaceChild("right_wheel", CubeListBuilder.create()
                .texOffs(0, 22).addBox(6F, -1F, -1F, 2F, 2F, 2F, new CubeDeformation(0F))
                .texOffs(18, 21).addBox(7F, -3.5F, -3.5F, 0F, 7F, 7F, new CubeDeformation(0F))
                .texOffs(0, 0).addBox(6F, 3F, -2.05F, 2F, 2F, 4.125F, new CubeDeformation(0F)), PartPose.offset(-14F, -4F, 0F));
        PartDefinition seg_8 = right_wheel.addOrReplaceChild("seg_8", CubeListBuilder.create()
                .texOffs(18, 21).addBox(7F, -3.5F, -3.5F, 0F, 7F, 7F, new CubeDeformation(0F))
                .texOffs(0, 0).addBox(6F, 3F, -2.05F, 2F, 2F, 4.125F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 0F, -0.7853982F, 0F, 0F));
        PartDefinition seg_9 = right_wheel.addOrReplaceChild("seg_9", CubeListBuilder.create()
                .texOffs(0, 0).addBox(6F, 3F, -2.05F, 2F, 2F, 4.125F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 0F, -1.5707963F, 0F, 0F));
        PartDefinition seg_10 = right_wheel.addOrReplaceChild("seg_10", CubeListBuilder.create()
                .texOffs(0, 0).addBox(6F, 3F, -2.05F, 2F, 2F, 4.125F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 0F, -2.3561945F, 0F, 0F));
        PartDefinition seg_11 = right_wheel.addOrReplaceChild("seg_11", CubeListBuilder.create()
                .texOffs(0, 0).addBox(6F, 3F, -2.05F, 2F, 2F, 4.125F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 0F, 3.1415927F, 0F, 0F));
        PartDefinition seg_12 = right_wheel.addOrReplaceChild("seg_12", CubeListBuilder.create()
                .texOffs(0, 0).addBox(6F, 3F, -2.05F, 2F, 2F, 4.125F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 0F, 2.3561945F, 0F, 0F));
        PartDefinition seg_13 = right_wheel.addOrReplaceChild("seg_13", CubeListBuilder.create()
                .texOffs(0, 0).addBox(6F, 3F, -2.05F, 2F, 2F, 4.125F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 0F, 1.5707963F, 0F, 0F));
        PartDefinition seg_14 = right_wheel.addOrReplaceChild("seg_14", CubeListBuilder.create()
                .texOffs(0, 0).addBox(6F, 3F, -2.05F, 2F, 2F, 4.125F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, 0F, 0F, 0.7853982F, 0F, 0F));
        PartDefinition fire = main.addOrReplaceChild("fire", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3F, -50F, -3F, 6F, 26F, 6F, new CubeDeformation(0F)), PartPose.offset(0F, -12F, 0F));
        PartDefinition player_head = main.addOrReplaceChild("player_head", CubeListBuilder.create(), PartPose.offset(0F, -12F, 0F));
        PartDefinition head = player_head.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4F, -4F, -4F, 8F, 8F, 8F, new CubeDeformation(-0.5F))
                .texOffs(32, 0).addBox(-4F, -4F, -4F, 8F, 8F, 8F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, -19F, 0F, -1.5707963F, 0F, 0F));
        PartDefinition cannon = main.addOrReplaceChild("cannon", CubeListBuilder.create()
                .texOffs(16, 4).addBox(-6F, -6F, -6F, 12F, 12F, 12F, new CubeDeformation(0F))
                .texOffs(24, 28).addBox(-5F, -21F, -5F, 10F, 15F, 10F, new CubeDeformation(0F))
                .texOffs(9, 42).addBox(-5.5F, -26F, -5.5F, 2F, 5F, 11F, new CubeDeformation(0F))
                .texOffs(0, 33).addBox(-3.5F, -26F, 3.5F, 7F, 5F, 2F, new CubeDeformation(0F))
                .texOffs(0, 26).addBox(-3.5F, -26F, -5.5F, 7F, 5F, 2F, new CubeDeformation(0F))
                .texOffs(2, 0).addBox(3.5F, -26F, -5.5F, 2F, 5F, 11F, new CubeDeformation(0F))
                .texOffs(54, 28).addBox(-1.5F, -2F, 6F, 3F, 3F, 1F, new CubeDeformation(0F)), PartPose.offset(0F, -11F, 0F));
        PartDefinition chains = cannon.addOrReplaceChild("chains", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition chain_1 = chains.addOrReplaceChild("chain_1", CubeListBuilder.create(), PartPose.offsetAndRotation(-6F, 6F, 0F, 0.0369039F, -0.0134281F, 0.348818F));
        PartDefinition chain_a1 = chain_1.addOrReplaceChild("chain_a1", CubeListBuilder.create()
                .texOffs(14, 40).addBox(-1.5F, -12F, 0F, 3F, 12F, 0F, new CubeDeformation(0F))
                .texOffs(14, 40).addBox(-1.5F, -24F, 0F, 3F, 12F, 0F, new CubeDeformation(0F))
                .texOffs(14, 40).addBox(-1.5F, -30F, 0F, 3F, 6F, 0F, new CubeDeformation(0F)), PartPose.offsetAndRotation(1.5F, 0F, 7F, 0F, 0.5235988F, 0F));
        PartDefinition chain_b1 = chain_1.addOrReplaceChild("chain_b1", CubeListBuilder.create()
                .texOffs(14, 40).addBox(-1.5F, -9F, 0F, 3F, 10F, 0F, new CubeDeformation(0F))
                .texOffs(14, 40).addBox(-1.5F, -21F, 0F, 3F, 12F, 0F, new CubeDeformation(0F))
                .texOffs(14, 40).addBox(-1.5F, -27F, 0F, 3F, 6F, 0F, new CubeDeformation(0F)), PartPose.offsetAndRotation(1.5F, 0F, 7F, 0F, -0.5235988F, 0F));
        PartDefinition chain_2 = chains.addOrReplaceChild("chain_2", CubeListBuilder.create(), PartPose.offsetAndRotation(3.1F, 6.9F, 0F, 0.0369039F, 0.0134286F, -0.348818F));
        PartDefinition chain_a2 = chain_2.addOrReplaceChild("chain_a2", CubeListBuilder.create()
                .texOffs(14, 40).addBox(-1.5F, -12F, 0F, 3F, 12F, 0F, new CubeDeformation(0F))
                .texOffs(14, 40).addBox(-1.5F, -24F, 0F, 3F, 12F, 0F, new CubeDeformation(0F))
                .texOffs(14, 40).addBox(-1.5F, -30F, 0F, 3F, 6F, 0F, new CubeDeformation(0F)), PartPose.offsetAndRotation(1.5F, 0F, 7F, 0F, -0.5235988F, 0F));
        PartDefinition chain_b2 = chain_2.addOrReplaceChild("chain_b2", CubeListBuilder.create()
                .texOffs(14, 40).addBox(-1.5F, -9F, 0F, 3F, 10F, 0F, new CubeDeformation(0F))
                .texOffs(14, 40).addBox(-1.5F, -21F, 0F, 3F, 12F, 0F, new CubeDeformation(0F))
                .texOffs(14, 40).addBox(-1.5F, -27F, 0F, 3F, 6F, 0F, new CubeDeformation(0F)), PartPose.offsetAndRotation(1.5F, 0F, 7F, 0F, 0.5235988F, 0F));
        PartDefinition chain_3 = chains.addOrReplaceChild("chain_3", CubeListBuilder.create(), PartPose.offsetAndRotation(-6F, 6F, -14F, -0.0369032F, -0.0134281F, 0.348818F));
        PartDefinition chain_a3 = chain_3.addOrReplaceChild("chain_a3", CubeListBuilder.create()
                .texOffs(14, 40).addBox(-1.5F, -12F, 0F, 3F, 12F, 0F, new CubeDeformation(0F))
                .texOffs(14, 40).addBox(-1.5F, -24F, 0F, 3F, 12F, 0F, new CubeDeformation(0F))
                .texOffs(14, 40).addBox(-1.5F, -30F, 0F, 3F, 6F, 0F, new CubeDeformation(0F)), PartPose.offsetAndRotation(1.5F, 0F, 7F, 0F, -0.5235988F, 0F));
        PartDefinition chain_b3 = chain_3.addOrReplaceChild("chain_b3", CubeListBuilder.create()
                .texOffs(14, 40).addBox(-1.5F, -9F, 0F, 3F, 10F, 0F, new CubeDeformation(0F))
                .texOffs(14, 40).addBox(-1.5F, -21F, 0F, 3F, 12F, 0F, new CubeDeformation(0F))
                .texOffs(14, 40).addBox(-1.5F, -27F, 0F, 3F, 6F, 0F, new CubeDeformation(0F)), PartPose.offsetAndRotation(1.5F, 0F, 7F, 0F, 0.5235988F, 0F));
        PartDefinition chain_4 = chains.addOrReplaceChild("chain_4", CubeListBuilder.create(), PartPose.offsetAndRotation(3.1F, 6.9F, -14F, -0.0369032F, 0.0134286F, -0.348818F));
        PartDefinition chain_a4 = chain_4.addOrReplaceChild("chain_a4", CubeListBuilder.create()
                .texOffs(14, 40).addBox(-1.5F, -12F, 0F, 3F, 12F, 0F, new CubeDeformation(0F))
                .texOffs(14, 40).addBox(-1.5F, -24F, 0F, 3F, 12F, 0F, new CubeDeformation(0F))
                .texOffs(14, 40).addBox(-1.5F, -30F, 0F, 3F, 6F, 0F, new CubeDeformation(0F)), PartPose.offsetAndRotation(1.5F, 0F, 7F, 0F, 0.5235988F, 0F));
        PartDefinition chain_b4 = chain_4.addOrReplaceChild("chain_b4", CubeListBuilder.create()
                .texOffs(14, 40).addBox(-1.5F, -9F, 0F, 3F, 10F, 0F, new CubeDeformation(0F))
                .texOffs(14, 40).addBox(-1.5F, -21F, 0F, 3F, 12F, 0F, new CubeDeformation(0F))
                .texOffs(14, 40).addBox(-1.5F, -27F, 0F, 3F, 6F, 0F, new CubeDeformation(0F)), PartPose.offsetAndRotation(1.5F, 0F, 7F, 0F, -0.5235988F, 0F));
        PartDefinition chain_5 = chains.addOrReplaceChild("chain_5", CubeListBuilder.create(), PartPose.offsetAndRotation(5.5F, -20F, 7F, 1.5707963F, 0F, 0F));
        PartDefinition chain_a5 = chain_5.addOrReplaceChild("chain_a5", CubeListBuilder.create()
                .texOffs(14, 40).addBox(-1.5F, -12F, 0F, 3F, 12F, 0F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0.5F, 0F, 0F, 0F, -1.0471976F, 0F));
        PartDefinition chain_b5 = chain_5.addOrReplaceChild("chain_b5", CubeListBuilder.create()
                .texOffs(14, 40).addBox(-1.5F, -9F, 0F, 3F, 8F, 0F, new CubeDeformation(0F))
                .texOffs(14, 42).addBox(-1.5F, -13F, 0F, 3F, 2F, 0F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0.5F, 0F, 0F, 0F, 1.0471976F, 0F));
        PartDefinition chain_6 = chains.addOrReplaceChild("chain_6", CubeListBuilder.create(), PartPose.offsetAndRotation(-6.5F, -20F, 7F, 1.5707963F, 0F, 0F));
        PartDefinition chain_a6 = chain_6.addOrReplaceChild("chain_a6", CubeListBuilder.create()
                .texOffs(14, 40).addBox(-1.5F, -12F, 0F, 3F, 12F, 0F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0.5F, 0F, 0F, 0F, -1.0471976F, 0F));
        PartDefinition chain_b6 = chain_6.addOrReplaceChild("chain_b6", CubeListBuilder.create()
                .texOffs(14, 40).addBox(-1.5F, -9F, 0F, 3F, 8F, 0F, new CubeDeformation(0F))
                .texOffs(14, 42).addBox(-1.5F, -13F, 0F, 3F, 2F, 0F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0.5F, 0F, 0F, 0F, 1.0471976F, 0F));
        PartDefinition chain_7 = chains.addOrReplaceChild("chain_7", CubeListBuilder.create(), PartPose.offsetAndRotation(-5.25F, 7F, 7F, 1.5707963F, 0F, 0F));
        PartDefinition chain_a7 = chain_7.addOrReplaceChild("chain_a7", CubeListBuilder.create()
                .texOffs(14, 40).addBox(-1.5F, -12F, 0F, 3F, 12F, 0F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0.5F, 0F, 0F, 0F, -0.5235988F, 0F));
        PartDefinition chain_b7 = chain_7.addOrReplaceChild("chain_b7", CubeListBuilder.create()
                .texOffs(14, 40).addBox(-1.5F, -9F, 0F, 3F, 9F, 0F, new CubeDeformation(0F))
                .texOffs(14, 42).addBox(-1.5F, -14F, 0F, 3F, 3F, 0F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0.5F, 0F, 0F, 0F, 0.5235988F, 0F));
        PartDefinition chain_8 = chains.addOrReplaceChild("chain_8", CubeListBuilder.create(), PartPose.offsetAndRotation(4.75F, 7F, 7F, 1.5707963F, 0F, 0F));
        PartDefinition chain_a8 = chain_8.addOrReplaceChild("chain_a8", CubeListBuilder.create()
                .texOffs(14, 40).addBox(-1.5F, -12F, 0F, 3F, 12F, 0F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0.5F, 0F, 0F, 0F, 0.5235988F, 0F));
        PartDefinition chain_b8 = chain_8.addOrReplaceChild("chain_b8", CubeListBuilder.create()
                .texOffs(14, 40).addBox(-1.5F, -9F, 0F, 3F, 9F, 0F, new CubeDeformation(0F))
                .texOffs(14, 42).addBox(-1.5F, -14F, 0F, 3F, 3F, 0F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0.5F, 0F, 0F, 0F, -0.5235988F, 0F));
        PartDefinition fuse = cannon.addOrReplaceChild("fuse", CubeListBuilder.create()
                .texOffs(54, 27).addBox(0F, -0.5F, 7F, 0F, 1F, 5F, new CubeDeformation(0F))
                .texOffs(49, 33).addBox(-0.5F, 0F, 7F, 1F, 0F, 5F, new CubeDeformation(0F)), PartPose.offsetAndRotation(0F, -0.5F, 0F, 0F, 0F, 0.7853982F));
        return LayerDefinition.create(mesh, 64, 64);
    }
}