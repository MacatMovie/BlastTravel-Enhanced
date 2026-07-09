package foundationgames.blasttravel.util;

import foundationgames.blasttravel.BlastTravel;
import foundationgames.blasttravel.entity.CannonEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public enum BTNetworking {;
    private static final String PROTOCOL_VERSION = "2";

    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(BlastTravel.id("main"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

    public static void c2sRequestFire(CannonEntity entity) {
        CHANNEL.sendToServer(new RequestFirePayload(entity.getId(), entity.getYRot(), entity.getXRot()));
    }

    public static void c2sStopCannonFlight(boolean thud) {
        CHANNEL.sendToServer(new StopCannonFlightPayload(thud));
    }

    public static void s2cFireCannon(ServerPlayer to, CannonEntity cannon, @Nullable Player launched, Vec3 velocity, Vec3 launchPos) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> to), new FireCannonPayload(cannon.getId(), velocity.x, velocity.y, velocity.z,
                launched == null ? -1 : launched.getId(), launchPos.x, launchPos.y, launchPos.z));
    }

    public static void s2cStopCannonFlight(ServerPlayer to, Player flying) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> to), new StopCannonFlightClientPayload(flying.getId()));
    }

    public static void registerMessages(FMLCommonSetupEvent event) {
        int id = 0;
        CHANNEL.messageBuilder(RequestFirePayload.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(RequestFirePayload::encode)
                .decoder(RequestFirePayload::decode)
                .consumerMainThread(BTNetworking::handleRequestFire)
                .add();
        CHANNEL.messageBuilder(StopCannonFlightPayload.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(StopCannonFlightPayload::encode)
                .decoder(StopCannonFlightPayload::decode)
                .consumerMainThread(BTNetworking::handleStopCannonFlight)
                .add();
        CHANNEL.messageBuilder(FireCannonPayload.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(FireCannonPayload::encode)
                .decoder(FireCannonPayload::decode)
                .consumerMainThread(BTNetworking::handleFireCannonClient)
                .add();
        CHANNEL.messageBuilder(StopCannonFlightClientPayload.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(StopCannonFlightClientPayload::encode)
                .decoder(StopCannonFlightClientPayload::decode)
                .consumerMainThread(BTNetworking::handleStopCannonFlightClient)
                .add();
    }

    private static void handleFireCannonClient(FireCannonPayload payload, Supplier<NetworkEvent.Context> context) {
        foundationgames.blasttravel.client.BTClientNetworking.handleFireCannonClient(payload);
    }

    private static void handleStopCannonFlightClient(StopCannonFlightClientPayload payload, Supplier<NetworkEvent.Context> context) {
        foundationgames.blasttravel.client.BTClientNetworking.handleStopCannonFlightClient(payload);
    }

    private static void handleRequestFire(RequestFirePayload payload, Supplier<NetworkEvent.Context> context) {
        var player = context.get().getSender();
        if (player == null) {
            return;
        }
        var entity = player.level().getEntity(payload.entityId());
        if (entity instanceof CannonEntity cannon) {
            cannon.applyClientAim(payload.yaw(), payload.pitch(), player);
            cannon.fireServer(player);
        }
    }

    private static void handleStopCannonFlight(StopCannonFlightPayload payload, Supplier<NetworkEvent.Context> context) {
        var launched = context.get().getSender();
        if (launched == null) {
            return;
        }
        ((PlayerEntityDuck) launched).blasttravel$setCannonFlight(false);
        if (launched.level() instanceof ServerLevel world) {
            world.players().forEach(player -> s2cStopCannonFlight(player, launched));
        }

        if (payload.thud()) {
            launched.level().playSound(null, launched.getX(), launched.getY(), launched.getZ(), SoundEvents.GENERIC_SMALL_FALL, SoundSource.PLAYERS, 1, 0.78F);
        }
    }

    private static ResourceLocation payloadId(String path) {
        return BlastTravel.id(path);
    }

    public record RequestFirePayload(int entityId, float yaw, float pitch) {
        public static void encode(RequestFirePayload payload, FriendlyByteBuf buf) {
            buf.writeVarInt(payload.entityId);
            buf.writeFloat(payload.yaw);
            buf.writeFloat(payload.pitch);
        }

        public static RequestFirePayload decode(FriendlyByteBuf buf) {
            return new RequestFirePayload(buf.readVarInt(), buf.readFloat(), buf.readFloat());
        }
    }

    public record StopCannonFlightPayload(boolean thud) {
        public static void encode(StopCannonFlightPayload payload, FriendlyByteBuf buf) {
            buf.writeBoolean(payload.thud);
        }

        public static StopCannonFlightPayload decode(FriendlyByteBuf buf) {
            return new StopCannonFlightPayload(buf.readBoolean());
        }
    }

    public record FireCannonPayload(int cannonId, double x, double y, double z, int playerId, double launchX, double launchY, double launchZ) {
        public static void encode(FireCannonPayload payload, FriendlyByteBuf buf) {
            buf.writeVarInt(payload.cannonId);
            buf.writeDouble(payload.x);
            buf.writeDouble(payload.y);
            buf.writeDouble(payload.z);
            buf.writeVarInt(payload.playerId);
            buf.writeDouble(payload.launchX);
            buf.writeDouble(payload.launchY);
            buf.writeDouble(payload.launchZ);
        }

        public static FireCannonPayload decode(FriendlyByteBuf buf) {
            return new FireCannonPayload(buf.readVarInt(), buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readVarInt(), buf.readDouble(), buf.readDouble(), buf.readDouble());
        }
    }

    public record StopCannonFlightClientPayload(int playerId) {
        public static void encode(StopCannonFlightClientPayload payload, FriendlyByteBuf buf) {
            buf.writeVarInt(payload.playerId);
        }

        public static StopCannonFlightClientPayload decode(FriendlyByteBuf buf) {
            return new StopCannonFlightClientPayload(buf.readVarInt());
        }
    }
}
