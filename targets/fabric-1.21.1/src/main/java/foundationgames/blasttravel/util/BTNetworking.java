package foundationgames.blasttravel.util;

import foundationgames.blasttravel.BlastTravel;
import foundationgames.blasttravel.entity.CannonEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.jetbrains.annotations.Nullable;

public enum BTNetworking {;
    public static void c2sRequestFire(CannonEntity entity) {
        foundationgames.blasttravel.client.BTClientNetworking.sendRequestFire(entity);
    }

    public static void c2sStopCannonFlight(boolean thud) {
        foundationgames.blasttravel.client.BTClientNetworking.sendStopCannonFlight(thud);
    }

    public static void s2cFireCannon(ServerPlayer to, CannonEntity cannon, @Nullable Player launched, Vec3 velocity, Vec3 launchPos) {
        ServerPlayNetworking.send(to, new FireCannonPayload(cannon.getId(), velocity.x, velocity.y, velocity.z,
                launched == null ? -1 : launched.getId(), launchPos.x, launchPos.y, launchPos.z));
    }

    public static void s2cStopCannonFlight(ServerPlayer to, Player flying) {
        ServerPlayNetworking.send(to, new StopCannonFlightClientPayload(flying.getId()));
    }

    public static void registerServerReceivers() {
        PayloadTypeRegistry.playC2S().register(RequestFirePayload.TYPE, RequestFirePayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(StopCannonFlightPayload.TYPE, StopCannonFlightPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(FireCannonPayload.TYPE, FireCannonPayload.STREAM_CODEC);
        PayloadTypeRegistry.playS2C().register(StopCannonFlightClientPayload.TYPE, StopCannonFlightClientPayload.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RequestFirePayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                var entity = player.level().getEntity(payload.entityId());
                if (entity instanceof CannonEntity cannon) {
                    cannon.applyClientAim(payload.yaw(), payload.pitch(), player);
                    cannon.fireServer(player);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(StopCannonFlightPayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
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

    private static ResourceLocation payloadId(String path) {
        return BlastTravel.id(path);
    }

    public record RequestFirePayload(int entityId, float yaw, float pitch) implements CustomPacketPayload {
        public static final Type<RequestFirePayload> TYPE = new Type<>(payloadId("request_fire"));
        public static final StreamCodec<RegistryFriendlyByteBuf, RequestFirePayload> STREAM_CODEC = StreamCodec.of(
                (buf, payload) -> {
                    buf.writeVarInt(payload.entityId);
                    buf.writeFloat(payload.yaw);
                    buf.writeFloat(payload.pitch);
                },
                buf -> new RequestFirePayload(buf.readVarInt(), buf.readFloat(), buf.readFloat())
        );

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record StopCannonFlightPayload(boolean thud) implements CustomPacketPayload {
        public static final Type<StopCannonFlightPayload> TYPE = new Type<>(payloadId("stop_cannon_flight"));
        public static final StreamCodec<RegistryFriendlyByteBuf, StopCannonFlightPayload> STREAM_CODEC = StreamCodec.of(
                (buf, payload) -> buf.writeBoolean(payload.thud),
                buf -> new StopCannonFlightPayload(buf.readBoolean())
        );

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record FireCannonPayload(int cannonId, double x, double y, double z, int playerId, double launchX, double launchY, double launchZ) implements CustomPacketPayload {
        public static final Type<FireCannonPayload> TYPE = new Type<>(payloadId("fire_cannon"));
        public static final StreamCodec<RegistryFriendlyByteBuf, FireCannonPayload> STREAM_CODEC = StreamCodec.of(
                (buf, payload) -> {
                    buf.writeVarInt(payload.cannonId);
                    buf.writeDouble(payload.x);
                    buf.writeDouble(payload.y);
                    buf.writeDouble(payload.z);
                    buf.writeVarInt(payload.playerId);
                    buf.writeDouble(payload.launchX);
                    buf.writeDouble(payload.launchY);
                    buf.writeDouble(payload.launchZ);
                },
                buf -> new FireCannonPayload(buf.readVarInt(), buf.readDouble(), buf.readDouble(), buf.readDouble(),
                        buf.readVarInt(), buf.readDouble(), buf.readDouble(), buf.readDouble())
        );

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public record StopCannonFlightClientPayload(int playerId) implements CustomPacketPayload {
        public static final Type<StopCannonFlightClientPayload> TYPE = new Type<>(payloadId("stop_cannon_flight_client"));
        public static final StreamCodec<RegistryFriendlyByteBuf, StopCannonFlightClientPayload> STREAM_CODEC = StreamCodec.of(
                (buf, payload) -> buf.writeVarInt(payload.playerId),
                buf -> new StopCannonFlightClientPayload(buf.readVarInt())
        );

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
}
