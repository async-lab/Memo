package club.asyncraft.memo;

import club.asyncraft.memo.util.Commands;
import club.asyncraft.memo.util.Config;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;

import java.nio.file.Path;

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

    @Inject
    public Memo(ProxyServer server, @DataDirectory Path dataDir) {
        this.proxyServer = server;
        this.dataDir = dataDir;
        Memo.instance = this;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        Commands.init();
        this.config = new Config(this.dataDir);
    }

    private void register(Object x) {
        // 将对象注册到事件管理器，使其可以使用@Subscribe注解
        this.proxyServer.getEventManager().register(this, x);
    }

}