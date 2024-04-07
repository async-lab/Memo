package club.asyncraft.memo;

import club.asyncraft.memo.util.Reference;
import club.asyncraft.memo.util.Utils;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import lombok.Getter;
import lombok.extern.java.Log;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
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
        try {
            this.loadFile("config.yml");
            this.loadFile("server.yml");
            this.locale = Locale.forLanguageTag(this.getRootNode("config.yml").orElseThrow().node("lang").getString("en-US"));

            TranslationRegistry registry = TranslationRegistry.create(Key.key("memo"));
            GlobalTranslator.translator().removeSource(registry);

            for (Locale locale : Reference.locales) {
                registry.registerAll(locale, ResourceBundle.getBundle("club.asyncraft.memo.Bundle", locale, UTF8ResourceBundleControl.get()), true);
            }
            GlobalTranslator.translator().addSource(registry);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error", e);
        }
        this.setBroadcast();
    }

    private void setBroadcast() {
        CommentedConfigurationNode broadcastNode = this.getRootNode("config.yml").orElseThrow().node("broadcast");
        if (broadcastNode.node("enable").getBoolean()) {
            ProxyServer proxyServer = Memo.instance.getProxyServer();
            proxyServer.getScheduler().buildTask(Memo.instance, () -> {
                proxyServer.getAllPlayers()
                        .forEach(player -> {
                            try {
                                Optional<ServerConnection> serverConnectionOptional = player.getCurrentServer();
                                if (serverConnectionOptional.isPresent()) {
                                    String serverName = serverConnectionOptional.get().getServerInfo().getName();
                                    Optional<CommentedConfigurationNode> serverRootNodeOptional = this.getRootNode("server.yml");
                                    if (serverRootNodeOptional.isPresent()) {
                                        CommentedConfigurationNode serverRootNode = serverRootNodeOptional.get();
                                        String[] texts = serverRootNode.node(serverName, "broadcast").get(String[].class);
                                        if (texts != null) {
                                            for (String text : texts) {
                                                player.sendRichMessage(text);
                                            }
                                            Memo.instance.getLogger().info(Utils.getTextComponentContent("memo.broadcast").formatted(serverName));
                                        }
                                    }
                                }
                            } catch (SerializationException e) {
                                throw new RuntimeException(e);
                            }
                            player.sendMessage(Utils.getTextComponent("broadcast"));
                        });
            }).repeat(broadcastNode.node("interval").getInt(), TimeUnit.SECONDS).schedule();
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
            if (!dataDirFile.mkdir()) {
                throw new RuntimeException("ERROR: Can't create data directory (permissions/filesystem error?)");
            }
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
