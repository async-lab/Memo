package club.asyncraft.commandshortcut.util;

import lombok.Getter;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
public class Config {

    private final Path dataDir;
    private final Map<String, YamlConfigurationLoader> loaderMap;

    public Config(Path dataDir) {
        this.dataDir = dataDir;
        this.loaderMap = new HashMap<>();
        this.loadFile("config.yml");
        this.loadFile("server.yml");
    }

    public Optional<CommentedConfigurationNode> getRootNode(String fileName) {
        try {
            return Optional.of(Optional.ofNullable(this.loaderMap.get(fileName)).orElseThrow().load());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private void loadFile(String fileName) {
        File dataDirFile = dataDir.toFile();
        if (!dataDirFile.exists()) {
            dataDirFile.mkdir();
        }

        File dataFile = new File(dataDirFile, fileName);
        if (!dataFile.exists()) {
            try {
                InputStream in = this.getClass().getResourceAsStream("/" + fileName);
                if (in != null) {
                    Files.copy(in, dataFile.toPath());
                }
            } catch (IOException e) {
                throw new RuntimeException("ERROR: Can't write default configuration file (permissions/filesystem error?)");
            }
        }

        this.loaderMap.put(fileName, YamlConfigurationLoader.builder().path(dataFile.toPath()).build());
    }
}
