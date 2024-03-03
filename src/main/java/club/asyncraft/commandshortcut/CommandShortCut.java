package club.asyncraft.commandshortcut;

import club.asyncraft.commandshortcut.util.Commands;
import club.asyncraft.commandshortcut.util.Config;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;

import java.nio.file.Path;
import java.util.logging.Logger;

@Getter
@Plugin(
        id = BuildConstatns.PLUGIN_ID,
        name = BuildConstatns.PLUGIN_NAME,
        version = BuildConstatns.PLUGIN_VERSION,
        description = BuildConstatns.PLUGIN_DESCRIPTION,
        url = BuildConstatns.PLUGIN_URL,
        authors = {BuildConstatns.PLUGIN_AUTHORS}
)
public class CommandShortCut {
    private final ProxyServer proxyServer;
    private final Logger logger;
    private final Path dataDir;
    private Config config;

    @Inject
    public CommandShortCut(ProxyServer server, Logger logger, @DataDirectory Path dataDir) {
        this.proxyServer = server;
        this.logger = logger;
        this.dataDir = dataDir;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        Commands.init(this);
        this.config = new Config(this.dataDir);
    }

    private void register(Object x) {
        // 将对象注册到事件管理器，使其可以使用@Subscribe注解
        this.proxyServer.getEventManager().register(this, x);
    }

}