package club.asyncraft.memo;

import club.asyncraft.memo.util.Reference;
import club.asyncraft.memo.util.Utils;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
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
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

@Log
@Getter
public class Config {

    private Locale locale;
    private ScheduledTask task;

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

    public void unload() {
        if (this.task != null) {
            this.task.cancel();
        }
    }

    private void setBroadcast() {
        CommentedConfigurationNode broadcastNode = this.getRootNode("config.yml").orElseThrow().node("broadcast");
        int interval = broadcastNode.node("interval").getInt(300);

        if (broadcastNode.node("enable").getBoolean()) {
            Memo.instance.getLogger().info(MessageFormat.format(Utils.getTextComponentContent("memo.broadcast_is_enabled", this.locale), interval));
            ProxyServer proxyServer = Memo.instance.getProxyServer();
            this.task = proxyServer.getScheduler().buildTask(Memo.instance, () -> {
                proxyServer.getAllServers()
                        .forEach(server -> {
                            String serverName = server.getServerInfo().getName();
                            Optional<CommentedConfigurationNode> serverRootNodeOptional = this.getRootNode("server.yml");
                            if (serverRootNodeOptional.isEmpty()) {
                                return;
                            }

                            try {
                                String[] texts = serverRootNodeOptional.get().node(serverName, "broadcast").get(String[].class);
                                if (texts != null && texts.length > 0) {
                                    Collection<Player> players = server.getPlayersConnected();
                                    for (Player player : players) {
                                        for (String text : texts) {
                                            player.sendRichMessage(text);
                                        }
                                    }
                                    Memo.instance.getLogger().info(MessageFormat.format(Utils.getTextComponentContent("memo.broadcast", this.locale), serverName));
                                }
                            } catch (SerializationException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }).repeat(interval, TimeUnit.SECONDS).schedule();
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
