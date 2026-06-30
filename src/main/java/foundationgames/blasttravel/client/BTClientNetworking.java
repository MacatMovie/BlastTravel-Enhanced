package foundationgames.blasttravel.client;

import foundationgames.blasttravel.entity.CannonEntity;
import foundationgames.blasttravel.util.BTNetworking;
import foundationgames.blasttravel.util.PlayerEntityDuck;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class BTClientNetworking {
    private BTClientNetworking() {}

    public static void handleFireCannonClient(BTNetworking.FireCannonPayload payload) {
        var mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Vec3 velocity = new Vec3(payload.x(), payload.y(), payload.z());
        CannonEntity cannon = null;
        var cannonEntity = mc.level.getEntity(payload.cannonId());
        if (cannonEntity instanceof CannonEntity foundCannon) {
            cannon = foundCannon;
        }

        if (payload.playerId() >= 0) {
            var entity = mc.level.getEntity(payload.playerId());
            if (entity instanceof Player player) {
                player.getAbilities().flying = false;
                if (cannon != null) {
                    var launchPos = cannon.getLaunchPosition();
                    player.setPos(launchPos.x, launchPos.y, launchPos.z);
                }
                player.setDeltaMovement(velocity);
                player.hasImpulse = true;
                ((PlayerEntityDuck) player).blasttravel$setCannonFlightVelocity(velocity);
                ((PlayerEntityDuck) player).blasttravel$setCannonFlight(true);
            }
        }

        if (cannon != null) {
            cannon.fireClient();
        }
    }

    public static void handleStopCannonFlightClient(BTNetworking.StopCannonFlightClientPayload payload) {
        var mc = Minecraft.getInstance();
        if (mc.level == null) return;
        var entity = mc.level.getEntity(payload.playerId());
        if (entity instanceof PlayerEntityDuck player && entity != mc.player) {
            player.blasttravel$setCannonFlight(false);
        }
    }
}
