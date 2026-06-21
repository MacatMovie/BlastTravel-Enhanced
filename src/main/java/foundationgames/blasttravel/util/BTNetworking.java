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
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.Nullable;

public enum BTNetworking {;
    public static void c2sRequestFire(CannonEntity entity) {
        PacketDistributor.sendToServer(new RequestFirePayload(entity.getId(), entity.getYRot(), entity.getXRot()));
    }

    public static void c2sStopCannonFlight(boolean thud) {
        PacketDistributor.sendToServer(new StopCannonFlightPayload(thud));
    }

    public static void s2cFireCannon(ServerPlayer to, CannonEntity cannon, @Nullable Player launched, Vec3 velocity) {
        PacketDistributor.sendToPlayer(to, new FireCannonPayload(cannon.getId(), velocity.x, velocity.y, velocity.z, launched == null ? -1 : launched.getId()));
    }

    public static void s2cStopCannonFlight(ServerPlayer to, Player flying) {
        PacketDistributor.sendToPlayer(to, new StopCannonFlightClientPayload(flying.getId()));
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1").optional();
        registrar.playToServer(RequestFirePayload.TYPE, RequestFirePayload.STREAM_CODEC, BTNetworking::handleRequestFire);
        registrar.playToServer(StopCannonFlightPayload.TYPE, StopCannonFlightPayload.STREAM_CODEC, BTNetworking::handleStopCannonFlight);
        registrar.playToClient(FireCannonPayload.TYPE, FireCannonPayload.STREAM_CODEC, BTNetworking::handleFireCannonClient);
        registrar.playToClient(StopCannonFlightClientPayload.TYPE, StopCannonFlightClientPayload.STREAM_CODEC, BTNetworking::handleStopCannonFlightClient);
    }

    private static void handleFireCannonClient(FireCannonPayload payload, IPayloadContext context) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            foundationgames.blasttravel.client.BTClientNetworking.handleFireCannonClient(payload, context);
        }
    }

    private static void handleStopCannonFlightClient(StopCannonFlightClientPayload payload, IPayloadContext context) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            foundationgames.blasttravel.client.BTClientNetworking.handleStopCannonFlightClient(payload, context);
        }
    }

    private static void handleRequestFire(RequestFirePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            var entity = player.level().getEntity(payload.entityId());
            if (entity instanceof CannonEntity cannon) {
                cannon.applyClientAim(payload.yaw(), payload.pitch(), player);
                cannon.fireServer(player);
            }
        });
    }

    private static void handleStopCannonFlight(StopCannonFlightPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            var launched = context.player();
            ((PlayerEntityDuck) launched).blasttravel$setCannonFlight(false);
            if (launched.level() instanceof ServerLevel world) {
                world.players().forEach(player -> s2cStopCannonFlight(player, launched));
            }

            if (payload.thud()) {
                launched.level().playSound(null, launched.getX(), launched.getY(), launched.getZ(), SoundEvents.GENERIC_SMALL_FALL, SoundSource.PLAYERS, 1, 0.78F);
            }
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

    public record FireCannonPayload(int cannonId, double x, double y, double z, int playerId) implements CustomPacketPayload {
        public static final Type<FireCannonPayload> TYPE = new Type<>(payloadId("fire_cannon"));
        public static final StreamCodec<RegistryFriendlyByteBuf, FireCannonPayload> STREAM_CODEC = StreamCodec.of(
                (buf, payload) -> {
                    buf.writeVarInt(payload.cannonId);
                    buf.writeDouble(payload.x);
                    buf.writeDouble(payload.y);
                    buf.writeDouble(payload.z);
                    buf.writeVarInt(payload.playerId);
                },
                buf -> new FireCannonPayload(buf.readVarInt(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readVarInt())
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
