package foundationgames.blasttravel;

import foundationgames.blasttravel.config.BTConfig;
import foundationgames.blasttravel.entity.CannonEntity;
import foundationgames.blasttravel.item.CannonItem;
import foundationgames.blasttravel.screen.CannonScreenHandler;
import foundationgames.blasttravel.util.BTNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class BlastTravel implements ModInitializer {
    public static final String MOD_ID = "blasttravel";
    public static final Logger LOG = LoggerFactory.getLogger("Blast Travel");

    public static final EntityType<CannonEntity> CANNON_TYPE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            id("cannon"),
            FabricEntityTypeBuilder.<CannonEntity>create(MobCategory.MISC, CannonEntity::new)
                    .dimensions(EntityDimensions.fixed(1.0F, 0.8F))
                    .trackRangeChunks(10)
                    .trackedUpdateRate(3)
                    .build()
    );
    public static final Supplier<EntityType<CannonEntity>> CANNON = () -> CANNON_TYPE;

    public static final MenuType<CannonScreenHandler> CANNON_SCREEN_HANDLER_TYPE = Registry.register(
            BuiltInRegistries.MENU,
            id("cannon"),
            new MenuType<>(CannonScreenHandler::new, FeatureFlags.DEFAULT_FLAGS)
    );
    public static final Supplier<MenuType<CannonScreenHandler>> CANNON_SCREEN_HANDLER = () -> CANNON_SCREEN_HANDLER_TYPE;

    public static final Item CANNON_ITEM_TYPE = Registry.register(
            BuiltInRegistries.ITEM,
            id("cannon"),
            new CannonItem(new Item.Properties())
    );
    public static final Supplier<Item> CANNON_ITEM = () -> CANNON_ITEM_TYPE;

    public static final SimpleParticleType CANNON_BLAST_TYPE = Registry.register(
            BuiltInRegistries.PARTICLE_TYPE,
            id("cannon_blast"),
            FabricParticleTypes.simple(true)
    );
    public static final Supplier<SimpleParticleType> CANNON_BLAST = () -> CANNON_BLAST_TYPE;

    @Override
    public void onInitialize() {
        BTConfig.ensureConfigFileExists();
        LOG.info("Registered Blast Travel config as config/{}", BTConfig.FILE_NAME);
        BTNetworking.registerServerReceivers();
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> entries.accept(CANNON_ITEM.get()));
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public static Component translatable(String key) {
        return Component.translatable(key);
    }
}
