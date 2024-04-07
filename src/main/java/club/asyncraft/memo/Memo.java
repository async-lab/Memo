package club.asyncraft.memo;

import club.asyncraft.memo.util.Utils;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerSettingsChangedEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import lombok.extern.java.Log;
import org.slf4j.Logger;

import java.nio.file.Path;

@Log
@Getter
@Plugin(
        id = BuildConstants.PLUGIN_ID,
        name = BuildConstants.PLUGIN_NAME,
        version = BuildConstants.PLUGIN_VERSION,
        description = BuildConstants.PLUGIN_DESCRIPTION,
        url = BuildConstants.PLUGIN_URL,
        authors = {BuildConstants.PLUGIN_AUTHORS}
)
public class Memo {
    public static Memo instance;

    private final ProxyServer proxyServer;
    private final Path dataDir;
    private Config config;
    private final Logger logger;

    @Inject
    public Memo(ProxyServer server, @DataDirectory Path dataDir, Logger logger) {
        this.proxyServer = server;
        this.dataDir = dataDir;
        this.logger = logger;
        Memo.instance = this;
    }

    public void init() {
        Commands.init();
        this.config.unload();
        this.config = new Config(this.dataDir);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.init();
        logger.info(Utils.getTextComponent("memo.loaded").content());
    }

    @Subscribe
    public void onPlayerSettingsChangedEvent(PlayerSettingsChangedEvent event) {
        event.getPlayerSettings().getLocale();
        //TODO
    }

    private void register(Object x) {
        // 将对象注册到事件管理器，使其可以使用@Subscribe注解
        this.proxyServer.getEventManager().register(this, x);
    }

}