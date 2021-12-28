package plugin.chatreplyer;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChatReplyer extends JavaPlugin {
    ReplyCommand command;


    @Override
    public void onEnable() {
        // Plugin startup logic
        command = new ReplyCommand();

        getServer().getPluginManager().registerEvents(command, this);
        getCommand("reply").setExecutor(command);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        HandlerList.unregisterAll(command);

    }
}
