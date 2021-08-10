package cabbageroll.itplayer;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Play implements CommandExecutor {

    public static final Play instance = new Play();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            player.sendMessage(args[0]);
            File file = new File(MCMain.plugin.getDataFolder() + "/" + args[0]);
            try {
                FileInputStream fis = new FileInputStream(file);
                Song song = new Song(fis);
                SongPlayer sp = new SongPlayer(song);
                sp.addPlayer(player);
                sp.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

}
