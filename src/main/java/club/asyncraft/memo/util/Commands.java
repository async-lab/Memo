package club.asyncraft.memo.util;

import club.asyncraft.memo.Memo;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.util.Optional;
import java.util.logging.Level;

public class Commands {
    public static void init(Memo plugin) {
        ProxyServer proxyServer = plugin.getProxyServer();
        CommandManager commandManager = proxyServer.getCommandManager();

        LiteralCommandNode<CommandSource> memoRootNode = BrigadierCommand.literalArgumentBuilder("memo")
                .executes(context -> {
                    try {
                        CommandSource source = context.getSource();
                        if (!(source instanceof Player player)) {
                            source.sendMessage(Component.text("This command can only be executed by players"));
                            return Command.SINGLE_SUCCESS;
                        }

                        CommentedConfigurationNode rootNode = plugin.getConfig().getRootNode("server.yml").orElseThrow();

                        String serverName = Optional.ofNullable(player.getCurrentServer().orElseThrow().getServerInfo()).orElseThrow().getName();

                        String[] texts = Optional.ofNullable(rootNode.node(serverName).get(String[].class)).orElseThrow();

                        for (String text : texts) {
                            player.sendRichMessage(text);
                        }
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.SEVERE, "Error", e);
                    }

                    return Command.SINGLE_SUCCESS;
                })
                .build();

        commandManager.register(commandManager.metaBuilder("memo").plugin(plugin).build(), new BrigadierCommand(memoRootNode));
    }
}
