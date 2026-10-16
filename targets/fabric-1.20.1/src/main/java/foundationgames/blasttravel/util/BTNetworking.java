package foundationgames.blasttravel.util;

import foundationgames.blasttravel.BlastTravel;
import foundationgames.blasttravel.entity.CannonEntity;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public enum BTNetworking {;
    public static final ResourceLocation REQUEST_FIRE_ID = payloadId("request_fire");
    public static final ResourceLocation STOP_CANNON_FLIGHT_ID = payloadId("stop_cannon_flight");
    public static final ResourceLocation FIRE_CANNON_ID = payloadId("fire_cannon");
    public static final ResourceLocation STOP_CANNON_FLIGHT_CLIENT_ID = payloadId("stop_cannon_flight_client");

    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(REQUEST_FIRE_ID, (server, player, handler, buf, responseSender) -> {
            var payload = RequestFirePayload.decode(buf);
            server.execute(() -> {
                var entity = player.level().getEntity(payload.entityId());
                if (entity instanceof CannonEntity cannon) {
                    cannon.applyClientAim(payload.yaw(), payload.pitch(), player);
                    cannon.fireServer(player);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(STOP_CANNON_FLIGHT_ID, (server, player, handler, buf, responseSender) -> {
            var payload = StopCannonFlightPayload.decode(buf);
            server.execute(() -> {
                ((PlayerEntityDuck) player).blasttravel$setCannonFlight(false);
                if (player.level() instanceof ServerLevel world) {
                    world.players().forEach(other -> s2cStopCannonFlight(other, player));
                }
                if (payload.thud()) {
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_SMALL_FALL, SoundSource.PLAYERS, 1, 0.78F);
                }
            });
        });
    }

    public static void c2sRequestFire(CannonEntity entity) {
        foundationgames.blasttravel.client.BTClientNetworking.sendRequestFire(entity);
    }

    public static void c2sStopCannonFlight(boolean thud) {
        foundationgames.blasttravel.client.BTClientNetworking.sendStopCannonFlight(thud);
    }

    public static void s2cFireCannon(ServerPlayer to, CannonEntity cannon, @Nullable Player launched, Vec3 velocity, Vec3 launchPos) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        new FireCannonPayload(cannon.getId(), velocity.x, velocity.y, velocity.z, launched == null ? -1 : launched.getId(), launchPos.x, launchPos.y, launchPos.z).encode(buf);
        ServerPlayNetworking.send(to, FIRE_CANNON_ID, buf);
    }

    public static void s2cStopCannonFlight(ServerPlayer to, Player flying) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        new StopCannonFlightClientPayload(flying.getId()).encode(buf);
        ServerPlayNetworking.send(to, STOP_CANNON_FLIGHT_CLIENT_ID, buf);
    }

    private static ResourceLocation payloadId(String path) {
        return BlastTravel.id(path);
    }

    public record RequestFirePayload(int entityId, float yaw, float pitch) {
        public void encode(FriendlyByteBuf buf) {
            buf.writeVarInt(this.entityId);
            buf.writeFloat(this.yaw);
            buf.writeFloat(this.pitch);
        }

        public static RequestFirePayload decode(FriendlyByteBuf buf) {
            return new RequestFirePayload(buf.readVarInt(), buf.readFloat(), buf.readFloat());
        }
    }

    public record StopCannonFlightPayload(boolean thud) {
        public void encode(FriendlyByteBuf buf) {
            buf.writeBoolean(this.thud);
        }

        public static StopCannonFlightPayload decode(FriendlyByteBuf buf) {
            return new StopCannonFlightPayload(buf.readBoolean());
        }
    }

    public record FireCannonPayload(int cannonId, double x, double y, double z, int playerId, double launchX, double launchY, double launchZ) {
        public void encode(FriendlyByteBuf buf) {
            buf.writeVarInt(this.cannonId);
            buf.writeDouble(this.x);
            buf.writeDouble(this.y);
            buf.writeDouble(this.z);
            buf.writeVarInt(this.playerId);
            buf.writeDouble(this.launchX);
            buf.writeDouble(this.launchY);
            buf.writeDouble(this.launchZ);
        }

        public static FireCannonPayload decode(FriendlyByteBuf buf) {
            return new FireCannonPayload(buf.readVarInt(), buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readVarInt(), buf.readDouble(), buf.readDouble(), buf.readDouble());
        }
    }

    public record StopCannonFlightClientPayload(int playerId) {
        public void encode(FriendlyByteBuf buf) {
            buf.writeVarInt(this.playerId);
        }

        public static StopCannonFlightClientPayload decode(FriendlyByteBuf buf) {
            return new StopCannonFlightClientPayload(buf.readVarInt());
        }
    }
}
