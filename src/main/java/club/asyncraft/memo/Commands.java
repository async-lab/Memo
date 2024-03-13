package club.asyncraft.memo;

import club.asyncraft.memo.util.Utils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.extern.java.Log;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.util.Optional;
import java.util.logging.Level;

@Log
public class Commands {
    public static void init() {
        ProxyServer proxyServer = Memo.instance.getProxyServer();
        CommandManager commandManager = proxyServer.getCommandManager();

        LiteralCommandNode<CommandSource> memoRootNode = BrigadierCommand.literalArgumentBuilder("memo")
                .executes(Commands::memoExecutor)
                .then(
                        BrigadierCommand.requiredArgumentBuilder("sub", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    if (ctx.getSource().hasPermission("memo.reload")) {
                                        builder.suggest("reload");
                                    }
                                    builder.suggest("show");
                                    return builder.buildFuture();
                                })
                                .executes(Commands::memoExecutor)
                                .then(
                                        BrigadierCommand.requiredArgumentBuilder("arg", StringArgumentType.word())
                                                .executes(Commands::memoExecutor)
                                )
                )

                .build();

        commandManager.register(commandManager.metaBuilder("memo").plugin(Memo.instance).build(), new BrigadierCommand(memoRootNode));
    }

    private static int memoExecutor(CommandContext<CommandSource> context) {
        try {
            String sub;
            try {
                sub = context.getArgument("sub", String.class);
            } catch (Exception e) {
                sub = "show";
            }

            CommandSource source = context.getSource();

            switch (sub) {
                case "reload" -> {
                    if (!source.hasPermission("memo.reload")) {
                        source.sendMessage(Utils.getTextComponent("memo.no_permission"));
                        return Command.SINGLE_SUCCESS;
                    }
                    Memo.instance.init();
                    source.sendMessage(Utils.getTextComponent("memo.reloaded"));
                }
                case "show" -> showExecutor(context);
                default -> source.sendMessage(Utils.getTextComponent("memo.wrong_usage"));
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error", e);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int showExecutor(CommandContext<CommandSource> context) {
        try {
            CommandSource source = context.getSource();
            if (!(source instanceof Player player)) {
                source.sendMessage(Utils.getTextComponent("memo.only_player"));
                return Command.SINGLE_SUCCESS;
            }

            CommentedConfigurationNode rootNode = Memo.instance.getConfig().getRootNode("server.yml").orElseThrow();

            String serverName = Optional.ofNullable(player.getCurrentServer().orElseThrow().getServerInfo()).orElseThrow().getName();

            String arg;
            try {
                arg = context.getArgument("arg", String.class);
            } catch (Exception e) {
                arg = "main";
            }

            String[] texts = Optional.ofNullable(rootNode.node(serverName, arg).get(String[].class)).orElseThrow();

            for (String text : texts) {
                player.sendRichMessage(text);
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error", e);
        }

        return Command.SINGLE_SUCCESS;
    }
}
