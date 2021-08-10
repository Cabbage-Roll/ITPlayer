package cabbageroll.itplayer;

import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public class MCMain extends JavaPlugin implements CommandExecutor {

    public static MCMain plugin;

    public void onEnable() {
        getLogger().info("mcmain");

        getCommand("itplayer").setExecutor(Play.instance);

        plugin = this;
    }

}
