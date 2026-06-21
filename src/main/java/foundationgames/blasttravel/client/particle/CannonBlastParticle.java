package foundationgames.blasttravel.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class CannonBlastParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    public CannonBlastParticle(ClientLevel world, double x, double y, double z, double vx, double vy, double vz, SpriteSet sprites) {
        super(world, x, y, z, vx, vy, vz);
        this.sprites = sprites;
        this.age = world.random.nextInt(2);
        this.lifetime = 16;
        this.friction = 0.76F;
        this.scale(1.5F + world.random.nextFloat() * 0.2F);
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Factory(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel world, double x, double y, double z, double vx, double vy, double vz) {
            return new CannonBlastParticle(world, x, y, z, vx, vy, vz, this.sprites);
        }
    }
}
