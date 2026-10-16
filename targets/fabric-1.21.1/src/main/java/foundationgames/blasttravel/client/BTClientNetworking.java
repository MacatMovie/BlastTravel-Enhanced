package foundationgames.blasttravel.client;

import foundationgames.blasttravel.client.sound.CannonLaunchSound;
import foundationgames.blasttravel.entity.CannonEntity;
import foundationgames.blasttravel.util.BTNetworking;
import foundationgames.blasttravel.util.PlayerEntityDuck;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class BTClientNetworking {
    private BTClientNetworking() {}

    // Fabric 1.21.1's typed CustomPacketPayload API.
    public static void sendRequestFire(CannonEntity cannon) {
        ClientPlayNetworking.send(new BTNetworking.RequestFirePayload(cannon.getId(), cannon.getYRot(), cannon.getXRot()));
    }

    public static void sendStopCannonFlight(boolean thud) {
        ClientPlayNetworking.send(new BTNetworking.StopCannonFlightPayload(thud));
    }

    public static void registerReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(BTNetworking.FireCannonPayload.TYPE,
                (payload, context) -> context.client().execute(() -> handleFireCannonClient(payload)));
        ClientPlayNetworking.registerGlobalReceiver(BTNetworking.StopCannonFlightClientPayload.TYPE,
                (payload, context) -> context.client().execute(() -> handleStopCannonFlightClient(payload)));
    }

    public static void handleFireCannonClient(BTNetworking.FireCannonPayload payload) {
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
                if (player == mc.player) {
                    // Use the cannon's fixed world position, NOT the launched player's position.
                    playCenteredLaunchSound(cannon != null
                            ? Vec3.atCenterOf(cannon.blockPosition()) : launchPos);
                }
            }
        }

        if (cannon != null) {
            cannon.fireClient();
        }
    }

    /** Centered output for the rider, fading with distance from the fixed cannon location. */
    private static void playCenteredLaunchSound(Vec3 cannonPosition) {
        Minecraft.getInstance().getSoundManager().play(new CannonLaunchSound(
                SoundEvents.GENERIC_EXPLODE.value(),
                cannonPosition
        ));
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

    public static void handleStopCannonFlightClient(BTNetworking.StopCannonFlightClientPayload payload) {
        var mc = Minecraft.getInstance();
        if (mc.level == null) return;
        var entity = mc.level.getEntity(payload.playerId());
        if (entity instanceof PlayerEntityDuck player && entity != mc.player) {
            player.blasttravel$setCannonFlight(false);
        }
    }
}
