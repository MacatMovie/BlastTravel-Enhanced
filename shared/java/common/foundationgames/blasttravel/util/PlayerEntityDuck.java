package foundationgames.blasttravel.util;

import net.minecraft.world.phys.Vec3;

public interface PlayerEntityDuck {
    void blasttravel$setCannonFlight(boolean inFlight);

    default void blasttravel$setCannonFlightVelocity(Vec3 velocity) {
    }

    boolean blasttravel$inCannonFlight();

    Vec3 blasttravel$getVelocityLerped(float delta);
}
