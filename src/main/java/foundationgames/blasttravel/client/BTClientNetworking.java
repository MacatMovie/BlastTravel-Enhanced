package foundationgames.blasttravel.client;

import foundationgames.blasttravel.entity.CannonEntity;
import foundationgames.blasttravel.util.BTNetworking;
import foundationgames.blasttravel.util.PlayerEntityDuck;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class BTClientNetworking {
    private BTClientNetworking() {}


    public static void handleFireCannonClient(BTNetworking.FireCannonPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            var mc = Minecraft.getInstance();
            if (mc.level == null) return;
            Vec3 velocity = new Vec3(payload.x(), payload.y(), payload.z());
            Vec3 launchPos = new Vec3(payload.launchX(), payload.launchY(), payload.launchZ());
            CannonEntity cannon = null;
            var cannonEntity = mc.level.getEntity(payload.cannonId());
            if (cannonEntity instanceof CannonEntity foundCannon) {
                cannon = foundCannon;
            }

            if (payload.playerId() >= 0) {
                var entity = mc.level.getEntity(payload.playerId());
                if (entity instanceof Player player) {
                    applyLaunch(player, cannon, launchPos, velocity);
                }
            }

            if (cannon != null) {
                cannon.fireClient();
            }
        });
    }

    private static void applyLaunch(Player player, CannonEntity cannon, Vec3 launchPos, Vec3 velocity) {
        player.getAbilities().flying = false;
        if (player.getVehicle() instanceof CannonEntity vehicle && (cannon == null || vehicle.getId() == cannon.getId())) {
            player.stopRiding();
        }

        player.setPos(launchPos.x, launchPos.y, launchPos.z);
        player.setDeltaMovement(velocity);
        player.hasImpulse = true;
        player.fallDistance = 0.0F;
        ((PlayerEntityDuck) player).blasttravel$setCannonFlightVelocity(velocity);
        ((PlayerEntityDuck) player).blasttravel$setCannonFlight(true);
    }

    public static void handleStopCannonFlightClient(BTNetworking.StopCannonFlightClientPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            var mc = Minecraft.getInstance();
            if (mc.level == null) return;
            var entity = mc.level.getEntity(payload.playerId());
            if (entity instanceof PlayerEntityDuck player && entity != mc.player) {
                player.blasttravel$setCannonFlight(false);
            }
        });
    }
}
