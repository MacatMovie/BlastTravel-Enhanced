package foundationgames.blasttravel.entity.cannon;

import foundationgames.blasttravel.entity.CannonEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

public class EntityCannonBehavior extends CannonBehavior {
    private final EntityFactory entityFactory;

    public EntityCannonBehavior(Item item, ResourceLocation texture, EntityFactory entityFactory) {
        this(item, stack -> stack.is(item), texture, entityFactory);
    }

    public EntityCannonBehavior(Item icon, Predicate<ItemStack> filter, ResourceLocation texture, EntityFactory entityFactory) {
        super(icon, filter, texture);
        this.entityFactory = entityFactory;
    }

    @Override
    public boolean occupiesCannon(ItemStack behaviorStack) {
        return true;
    }

    @Override
    public void onFired(CannonEntity cannon, ItemStack behaviorStack, Vec3 velocity) {
        var pos = cannon.position().add(0, 0.75, 0).add(cannon.getLaunchDirection().scale(1.8));
        var entity = entityFactory.create(cannon.level(), pos, behaviorStack);
        entity.setDeltaMovement(velocity);
        entity.hasImpulse = true;
        behaviorStack.shrink(1);
        cannon.level().addFreshEntity(entity);
    }

    public static Entity tntFactory(Level world, Vec3 pos, ItemStack from) {
        return new PrimedTnt(world, pos.x, pos.y - 0.5, pos.z, null);
    }

    public static Entity fallingBlockFactory(Level world, Vec3 pos, ItemStack from) {
        var state = from.getItem() instanceof BlockItem block ? block.getBlock().defaultBlockState() : Blocks.AIR.defaultBlockState();
        return new FallingBlockEntity(world, pos.x, pos.y - 0.5, pos.z, state);
    }

    @FunctionalInterface
    public interface EntityFactory {
        Entity create(Level world, Vec3 pos, ItemStack spawnedFrom);
    }
}
