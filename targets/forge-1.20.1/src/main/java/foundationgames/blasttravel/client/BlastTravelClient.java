package foundationgames.blasttravel.client;

import foundationgames.blasttravel.BlastTravel;
import foundationgames.blasttravel.client.entity.CannonEntityRenderer;
import foundationgames.blasttravel.client.entity.CannonModel;
import foundationgames.blasttravel.client.particle.CannonBlastParticle;
import foundationgames.blasttravel.client.screen.CannonScreen;
import foundationgames.blasttravel.entity.CannonEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = BlastTravel.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BlastTravelClient {
    private BlastTravelClient() {}

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(BlastTravel.CANNON_SCREEN_HANDLER.get(), CannonScreen::new));
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(BlastTravel.CANNON.get(), CannonEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(CannonEntityRenderer.MODEL, CannonModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(BlastTravel.CANNON_BLAST.get(), CannonBlastParticle.Factory::new);
    }

    @EventBusSubscriber(modid = BlastTravel.MOD_ID, value = Dist.CLIENT)
    public static final class GameEvents {
        @SubscribeEvent
        public static void clientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }

            var mc = Minecraft.getInstance();
            if (mc.player != null) {
                if (mc.player.getVehicle() instanceof CannonEntity cannon) {
                    cannon.handleInput(mc.options.keyJump.isDown());
                }

                if (mc.player instanceof foundationgames.blasttravel.util.PlayerEntityDuck duck && duck.blasttravel$inCannonFlight()) {
                    mc.player.xxa = 0;
                    mc.player.zza = 0;
                }
            }
        }
    }
}
