package foundationgames.blasttravel.client;

import foundationgames.blasttravel.BlastTravel;
import foundationgames.blasttravel.client.entity.CannonEntityRenderer;
import foundationgames.blasttravel.client.entity.CannonModel;
import foundationgames.blasttravel.client.particle.CannonBlastParticle;
import foundationgames.blasttravel.client.screen.CannonScreen;
import foundationgames.blasttravel.entity.CannonEntity;
import foundationgames.blasttravel.util.PlayerEntityDuck;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;

public final class BlastTravelClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(BlastTravel.CANNON_SCREEN_HANDLER.get(), CannonScreen::new);
        EntityRendererRegistry.register(BlastTravel.CANNON.get(), CannonEntityRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(CannonEntityRenderer.MODEL, CannonModel::createBodyLayer);
        ParticleFactoryRegistry.getInstance().register(BlastTravel.CANNON_BLAST.get(), CannonBlastParticle.Factory::new);
        BTClientNetworking.registerReceivers();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            var mc = Minecraft.getInstance();
            if (mc.player != null) {
                if (mc.player.getVehicle() instanceof CannonEntity cannon) {
                    cannon.handleInput(mc.options.keyJump.isDown());
                }
                if (mc.player instanceof PlayerEntityDuck duck && duck.blasttravel$inCannonFlight()) {
                    mc.player.xxa = 0;
                    mc.player.zza = 0;
                }
            }
        });
    }
}
