package foundationgames.blasttravel.entity.cannon;

import foundationgames.blasttravel.BlastTravel;
import foundationgames.blasttravel.entity.CannonEntity;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class CannonBehavior {
    protected static final Vector3f WHITE = new Vector3f(1, 1, 1);
    private static final List<CannonBehavior> ID_TO_BEHAVIOR = new ArrayList<>();
    private static final Object2IntMap<Predicate<ItemStack>> FILTER_TO_BEHAVIOR_ID = new Object2IntOpenHashMap<>();

    public final Item icon;
    public final Predicate<ItemStack> filter;
    private final ResourceLocation texture;

    public CannonBehavior(Item item, ResourceLocation texture) {
        this(item, stack -> stack.is(item), texture);
    }

    public CannonBehavior(Item icon, Predicate<ItemStack> filter) {
        this(icon, filter, BlastTravel.id("textures/entity/cannon/regular.png"));
    }

    public CannonBehavior(Item icon, Predicate<ItemStack> filter, ResourceLocation texture) {
        this.icon = icon;
        this.filter = filter;
        this.texture = texture;
    }

    public CannonBehavior register() {
        FILTER_TO_BEHAVIOR_ID.put(this.filter, ID_TO_BEHAVIOR.size());
        ID_TO_BEHAVIOR.add(this);
        return this;
    }

    public boolean displayHead(CannonEntity entity) {
        return entity.getClientPlayer() != null;
    }

    public boolean occupiesCannon(ItemStack behaviorStack) {
        return false;
    }

    public void onFired(CannonEntity cannon, ItemStack behaviorStack, net.minecraft.world.phys.Vec3 velocity) {
    }

    public static CannonBehavior byId(int id) {
        if (id < 0 || id >= ID_TO_BEHAVIOR.size()) return ID_TO_BEHAVIOR.get(0);
        return ID_TO_BEHAVIOR.get(id);
    }

    public static int idForStack(ItemStack stack) {
        for (var entry : FILTER_TO_BEHAVIOR_ID.object2IntEntrySet()) {
            if (entry.getKey().test(stack)) {
                return entry.getIntValue();
            }
        }
        return 0;
    }

    public static boolean isValidBehaviorStack(ItemStack stack) {
        return FILTER_TO_BEHAVIOR_ID.object2IntEntrySet().stream().anyMatch(entry -> entry.getKey().test(stack));
    }

    public static Collection<CannonBehavior> allBehaviors() {
        return ID_TO_BEHAVIOR;
    }

    public ResourceLocation texture(ItemStack stack) {
        return texture;
    }

    public ResourceLocation headTexture(CannonEntity entity) {
        Player player = entity.getClientPlayer();
        if (player == null) {
            return ResourceLocation.fromNamespaceAndPath("minecraft", "missingno");
        }
        return ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/player/wide/steve.png");
    }

    public Vector3f headColor(CannonEntity entity) {
        return WHITE;
    }

    public @Nullable Vector3f fireColor(CannonEntity entity) {
        return null;
    }
}
