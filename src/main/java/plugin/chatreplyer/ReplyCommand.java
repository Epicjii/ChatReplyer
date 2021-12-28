package plugin.chatreplyer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class ReplyCommand implements CommandExecutor, Listener {
    HashMap<Player, Player> replierToTargetmap = new HashMap<>();

    @Override
    public boolean onCommand
            (@NotNull CommandSender sender,
             @NotNull Command command,
             @NotNull String label,
             @NotNull String[] args) {

        String message = String.join(" ", args);
        for (Player replier : replierToTargetmap.keySet()) {
            if (replier == sender) {
                replierToTargetmap.get(replier).sendMessage(
                        Component.text(sender.getName() + " replies: " + message)
                                .decoration(TextDecoration.ITALIC, true).color(TextColor.fromHexString("#AAAAAA")));
            }
        }
        return true;
    }

    @EventHandler
    public void messageTracker(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (!(event.getMessage().split(" ")[0].equals("/msg") || event.getMessage().split(" ")[0].equals("/w"))) {
            return;
        }
        for (Player recipients : event.getRecipients()) {
            replierToTargetmap.put(recipients, event.getPlayer());
        }
    }


}
