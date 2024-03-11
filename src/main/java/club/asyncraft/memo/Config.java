package club.asyncraft.memo;

import club.asyncraft.memo.util.Reference;
import lombok.Getter;
import lombok.extern.java.Log;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;

@Log
@Getter
public class Config {

    private Locale locale;

    private final Path dataDir;
    private final Map<String, CommentedConfigurationNode> rootNodeMap;

    public Config(Path dataDir) {
        this.dataDir = dataDir;
        this.rootNodeMap = new HashMap<>();
    }

    public void init() {
        try {
            this.loadFile("config.yml");
            this.loadFile("server.yml");
            this.locale = Locale.forLanguageTag(this.getRootNode("config.yml").orElseThrow().node("lang").getString("en-US"));

            Memo.instance.getProxyServer().sendMessage(Component.text(this.getRootNode("config.yml").orElseThrow().node("lang").getString("en-US")));
            Memo.instance.getProxyServer().sendMessage(Component.text(this.locale.getDisplayName()));

            TranslationRegistry registry = TranslationRegistry.create(Key.key("memo"));
            GlobalTranslator.translator().removeSource(registry);

            for (Locale locale : Reference.locales) {
                registry.registerAll(locale, ResourceBundle.getBundle("club.asyncraft.memo.Bundle", locale, UTF8ResourceBundleControl.get()), true);
            }
            GlobalTranslator.translator().addSource(registry);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error", e);
        }
    }

    public Optional<CommentedConfigurationNode> getRootNode(String fileName) {
        try {
            return Optional.of(Optional.ofNullable(this.rootNodeMap.get(fileName)).orElseThrow());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private void loadFile(String fileName) throws ConfigurateException {
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

        this.rootNodeMap.put(fileName, YamlConfigurationLoader.builder().path(dataFile.toPath()).build().load());
    }
}
