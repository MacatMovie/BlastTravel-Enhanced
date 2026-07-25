package foundationgames.blasttravel.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class BTConfig {
    public static final String FILE_NAME = "blasttravel-common.toml";
    public static final ForgeConfigSpec COMMON_SPEC;
    private static final ForgeConfigSpec.DoubleValue LAUNCH_VELOCITY_FACTOR;
    private static final Logger LOG = LoggerFactory.getLogger("Blast Travel Config");

    private static final String DEFAULT_FILE_CONTENT = """
            # Controls the velocity applied to players and entities launched from cannons.
            # Changing this value may cause launched TNT or other entities to become lost or behave incorrectly.
            # Be careful with values above 2.0, as they may launch players into unloaded terrain or an unrecoverable void.
            # Default: 0.6
            # Range: 0.0 ~ 10.0
            launch_velocity_factor = 0.6
            """;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        LAUNCH_VELOCITY_FACTOR = builder
                .comment(
                        "Controls the velocity applied to players and entities launched from cannons.",
                        "Changing this value may cause launched TNT or other entities to become lost or behave incorrectly.",
                        "Be careful with values above 2.0, as they may launch players into unloaded terrain or an unrecoverable void."
                )
                .defineInRange("launch_velocity_factor", 0.6D, 0.0D, 10.0D);

        COMMON_SPEC = builder.build();
    }

    private BTConfig() {
    }

    /**
     * Ensures the config is visible immediately after startup, even before Forge
     * has had a chance to save a newly registered config spec itself.
     */
    public static Path ensureConfigFileExists() {
        Path configPath = FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);

        try {
            Files.createDirectories(configPath.getParent());
            if (Files.notExists(configPath)) {
                Files.writeString(
                        configPath,
                        DEFAULT_FILE_CONTENT,
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE_NEW,
                        StandardOpenOption.WRITE
                );
                LOG.info("Created Blast Travel config at {}", configPath.toAbsolutePath());
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create Blast Travel config at " + configPath.toAbsolutePath(), exception);
        }

        return configPath;
    }

    public static double launchVelocityFactor() {
        return LAUNCH_VELOCITY_FACTOR.get();
    }
}
