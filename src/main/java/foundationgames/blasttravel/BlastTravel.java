package foundationgames.blasttravel;

import foundationgames.blasttravel.config.BTConfig;
import foundationgames.blasttravel.entity.CannonEntity;
import foundationgames.blasttravel.item.CannonItem;
import foundationgames.blasttravel.screen.CannonScreenHandler;
import foundationgames.blasttravel.util.BTNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

@Mod(BlastTravel.MOD_ID)
public class BlastTravel {
    public static final String MOD_ID = "blasttravel";
    public static final Logger LOG = LoggerFactory.getLogger("Blast Travel");

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, MOD_ID);
    public static final DeferredRegister<net.minecraft.core.particles.ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, MOD_ID);

    public static final Supplier<EntityType<CannonEntity>> CANNON = ENTITY_TYPES.register("cannon", () ->
            EntityType.Builder.<CannonEntity>of(CannonEntity::new, MobCategory.MISC)
                    .sized(1.0F, 0.8F)
                    .clientTrackingRange(10)
                    .updateInterval(3)
                    .build("cannon"));

    public static final Supplier<MenuType<CannonScreenHandler>> CANNON_SCREEN_HANDLER = MENU_TYPES.register("cannon", () ->
            new MenuType<>(CannonScreenHandler::new, FeatureFlags.DEFAULT_FLAGS));

    public static final Supplier<Item> CANNON_ITEM = ITEMS.register("cannon", () -> new CannonItem(new Item.Properties()));

    public static final Supplier<SimpleParticleType> CANNON_BLAST = PARTICLE_TYPES.register("cannon_blast", () -> new SimpleParticleType(true));

    public BlastTravel(IEventBus modBus, ModContainer modContainer) {
        ENTITY_TYPES.register(modBus);
        MENU_TYPES.register(modBus);
        ITEMS.register(modBus);
        PARTICLE_TYPES.register(modBus);

        BTConfig.ensureConfigFileExists();
        modContainer.registerConfig(ModConfig.Type.COMMON, BTConfig.COMMON_SPEC, BTConfig.FILE_NAME);
        LOG.info("Registered Blast Travel config as config/{}", BTConfig.FILE_NAME);

        modBus.addListener(BTNetworking::registerPayloads);
        modBus.addListener(this::addCreativeTabs);
    }

    private void addCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(CANNON_ITEM.get());
        }
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static Component translatable(String key) {
        return Component.translatable(key);
    }
}
