package foundationgames.blasttravel.config;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BTConfig {
    public static final String FILE_NAME = "blasttravel-common.toml";
    private static final Logger LOG = LoggerFactory.getLogger("Blast Travel Config");
    private static final Pattern VALUE_PATTERN = Pattern.compile("(?m)^\\s*launch_velocity_factor\\s*=\\s*([0-9]+(?:\\.[0-9]+)?)\\s*$");

    private static final String DEFAULT_FILE_CONTENT = """
            # Controls the velocity applied to players and entities launched from cannons.
            # Changing this value may cause launched TNT or other entities to become lost or behave incorrectly.
            # Be careful with values above 2.0, as they may launch players into unloaded terrain or an unrecoverable void.
            # Default: 0.6
            # Range: 0.0 ~ 10.0
            launch_velocity_factor = 0.6
            """;

    private static double launchVelocityFactor = 0.6D;

    private BTConfig() {
    }

    public static Path ensureConfigFileExists() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);

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
            load(configPath);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not create or load Blast Travel config at " + configPath.toAbsolutePath(), exception);
        }

        return configPath;
    }

    private static void load(Path configPath) throws IOException {
        String text = Files.readString(configPath, StandardCharsets.UTF_8);
        Matcher matcher = VALUE_PATTERN.matcher(text);
        if (matcher.find()) {
            try {
                double parsed = Double.parseDouble(matcher.group(1));
                launchVelocityFactor = Math.max(0.0D, Math.min(10.0D, parsed));
            } catch (NumberFormatException ignored) {
                launchVelocityFactor = 0.6D;
            }
        } else {
            launchVelocityFactor = 0.6D;
        }
    }

    public static double launchVelocityFactor() {
        return launchVelocityFactor;
    }
}
