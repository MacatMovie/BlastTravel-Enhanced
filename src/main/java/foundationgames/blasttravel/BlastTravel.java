package foundationgames.blasttravel;

import foundationgames.blasttravel.entity.CannonEntity;
import foundationgames.blasttravel.item.CannonItem;
import foundationgames.blasttravel.screen.CannonScreenHandler;
import foundationgames.blasttravel.util.BTNetworking;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

@Mod(BlastTravel.MOD_ID)
public class BlastTravel {
    public static final String MOD_ID = "blasttravel";
    public static final Logger LOG = LoggerFactory.getLogger("Blast Travel");

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<net.minecraft.core.particles.ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, MOD_ID);

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

    public BlastTravel() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ENTITY_TYPES.register(modBus);
        MENU_TYPES.register(modBus);
        ITEMS.register(modBus);
        PARTICLE_TYPES.register(modBus);

        modBus.addListener(BTNetworking::registerMessages);
        modBus.addListener(this::addCreativeTabs);
    }

    private void addCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(CANNON_ITEM.get());
        }
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public static Component translatable(String key) {
        return Component.translatable(key);
    }
}
