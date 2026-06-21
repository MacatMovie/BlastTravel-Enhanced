package foundationgames.blasttravel.entity.cannon;

import foundationgames.blasttravel.BlastTravel;
import foundationgames.blasttravel.entity.CannonEntity;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ConcretePowderBlock;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class ConcretePowderCannonBehavior extends CannonBehavior {
    private static final ResourceLocation TEXTURE = BlastTravel.id("textures/entity/cannon/head/powder.png");
    private static final Map<Item, Vector3f> COLORS = new HashMap<>();

    public ConcretePowderCannonBehavior() {
        super(Items.WHITE_CONCRETE_POWDER, stack -> stack.getItem() instanceof BlockItem item && item.getBlock() instanceof ConcretePowderBlock);
    }

    @Override
    public boolean displayHead(CannonEntity entity) {
        return true;
    }

    private Vector3f color(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof ConcretePowderBlock block) {
            int color = block.defaultMapColor().col;
            return COLORS.computeIfAbsent(item, ignored -> new Vector3f(
                    (float) ((color >> 16) & 0xFF) / 255,
                    (float) ((color >> 8) & 0xFF) / 255,
                    (float) (color & 0xFF) / 255));
        }
        return WHITE;
    }

    @Override
    public void onFired(CannonEntity cannon, ItemStack behaviorStack, net.minecraft.world.phys.Vec3 velocity) {
        if (cannon.level() instanceof ServerLevel world) {
            var rot = cannon.getLaunchDirection();
            var origin = cannon.position().add(0, 0.75, 0).add(rot.scale(1.8));
            world.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, behaviorStack), origin.x, origin.y, origin.z,
                    12 + world.random.nextInt(7), 0, 0, 0, 0.17);
        }
    }

    @Override
    public ResourceLocation headTexture(CannonEntity entity) {
        if (!entity.isVehicle()) {
            return TEXTURE;
        }
        return super.headTexture(entity);
    }

    @Override
    public Vector3f headColor(CannonEntity entity) {
        if (!entity.isVehicle()) {
            return this.color(entity.getBehaviorStack());
        }
        return super.headColor(entity);
    }

    @Override
    public @Nullable Vector3f fireColor(CannonEntity entity) {
        return this.color(entity.getBehaviorStack());
    }
}
