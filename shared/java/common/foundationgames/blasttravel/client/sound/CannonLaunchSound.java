package foundationgames.blasttravel.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * A one-shot cannon blast for the player being launched.
 *
 * The audio stays centered (independent of camera facing), but its loudness is
 * calculated every sound tick from the player's distance to the ORIGINAL cannon.
 * It does not follow the player at a fixed volume. Other players still hear the
 * server's normal positional cannon blast.
 */
public final class CannonLaunchSound extends AbstractTickableSoundInstance {
    // Match the distance falloff of a normal 1.0-volume Minecraft world sound.
    private static final double AUDIBLE_RADIUS = 16.0D;
    private static final int MAX_LIFETIME_TICKS = 80;

    private final Vec3 cannonSoundPosition;
    private int age;

    public CannonLaunchSound(SoundEvent explosion, Vec3 cannonSoundPosition) {
        super(explosion, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.cannonSoundPosition = cannonSoundPosition;
        this.relative = true;
        this.attenuation = SoundInstance.Attenuation.NONE;
        this.looping = false;
        this.pitch = 1.0F;
        // With a relative sound at the origin OpenAL has no front/back panning.
        // Volume is applied manually using the fixed cannon world coordinates.
        this.x = 0.0D;
        this.y = 0.0D;
        this.z = 0.0D;
        this.updateDistanceVolume();
    }

    @Override
    public void tick() {
        if (++age > MAX_LIFETIME_TICKS) {
            this.stop();
            return;
        }
        this.updateDistanceVolume();
    }

    private void updateDistanceVolume() {
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            this.stop();
            return;
        }

        double distance = mc.player.position().distanceTo(cannonSoundPosition);
        this.volume = Mth.clamp((float) (1.0D - distance / AUDIBLE_RADIUS), 0.0F, 1.0F);
    }
}
