package plugin.chatreplyer;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("unused")
public final class ChatReplyer extends JavaPlugin {
    ReplyCommand command;


    @Override
    public void onEnable() {
        // Plugin startup logic
        var replyCommand = getCommand("reply");

        if (replyCommand != null) {
            command = new ReplyCommand(replyCommand);
            replyCommand.setExecutor(command);
            getServer().getPluginManager().registerEvents(command, this);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        HandlerList.unregisterAll(command);
    }
}
