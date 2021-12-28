package plugin.chatreplyer;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ReplyCommand implements CommandExecutor, TabCompleter, Listener {
    private static final HashMap<CommandSender, CommandSender> replierToTargetMap = new HashMap<>();

    private static final Set<String> whisperCommands = Collections.unmodifiableSet(
            Sets.newHashSet("w", "msg", "tell")
    );
    private final Set<String> aliases;
    private final PluginCommand replyCommand;

    public ReplyCommand(@NotNull PluginCommand replyCommand) {
        this.replyCommand = replyCommand;
        aliases = Sets.newHashSet(replyCommand.getAliases());
    }

    @Override
    public boolean onCommand
            (@NotNull CommandSender sender,
             @NotNull Command command,
             @NotNull String label,
             @NotNull String[] args) {
        String message = String.join(" ", args);

        var replyTarget = replierToTargetMap.get(sender);

        if (replyTarget == null) {
            sender.sendMessage(
                    Component.text("Nobody has messaged you yet.")
            );
            return true;
        } else {
            replyTarget.sendMessage(
                    Component.text(sender.getName() + " replies: " + message)
                            .decoration(TextDecoration.ITALIC, true)
                            .color(TextColor.fromHexString("#AAAAAA"))
            );
        }

        return true;
    }

    @EventHandler
    public void messageTracker(@NotNull PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) {
            return;
        }

        var sender = event.getPlayer();
        var splitCommand = event.getMessage().split(" ");

        var command = splitCommand[0];

        if (command.charAt(0) == '/') {
            command = command.substring(1);
        }

        if (commandIsNotValid(sender, command, splitCommand)) {
            return;
        }

        for (var recipient : whisperCommandRecipients(sender, command, splitCommand)) {
            replierToTargetMap.put(recipient, event.getPlayer());
        }
    }

    private boolean commandIsNotValid(Permissible sender, String command, @NotNull String[] splitCommand) {
        if (commandLengthCorrect(command, splitCommand)) {
            if (aliases.contains(command)) {
                var permission = replyCommand.getPermission();

                return permission != null && !sender.hasPermission(permission);
            }

            if (whisperCommands.contains(command)) {
                return senderNotPermittedToMessageRecipient(sender, splitCommand[1]);
            }
        }

        return true;
    }

    private boolean commandLengthCorrect(String command, String[] splitCommand) {
        if (aliases.contains(command)) {
            return splitCommand.length >= 2;
        }

        return splitCommand.length >= 3;
    }

    private boolean senderNotPermittedToMessageRecipient(Permissible sender, @NotNull String recipient) {
        return recipient.charAt(0) == '@' && !senderPermittedToUseSelector(sender);
    }

    private boolean senderPermittedToUseSelector(@NotNull Permissible sender) {
        System.out.println(sender.hasPermission(PermissionDefault.OP.toString()));
        return sender.hasPermission(PermissionDefault.OP.toString());
    }

    private @NotNull List<? extends CommandSender> whisperCommandRecipients(CommandSender sender, String command, String @NotNull [] splitCommand) {
        if (aliases.contains(command)) {
            if (replierToTargetMap.containsKey(sender)) {
                return Lists.newArrayList(replierToTargetMap.get(sender));
            }

            return Collections.emptyList();
        }

        var recipient = splitCommand[1];

        return Bukkit.getServer().selectEntities(sender, recipient);
    }

    @EventHandler
    public void messageTracker(@NotNull ServerCommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        var sender = event.getSender();
        var splitCommand = event.getCommand().split(" ");
        var command = splitCommand[0];

        if (commandIsNotValid(sender, command, splitCommand)) {
            return;
        }

        for (var recipient : whisperCommandRecipients(sender, command, splitCommand)) {
            replierToTargetMap.put(recipient, sender);
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
