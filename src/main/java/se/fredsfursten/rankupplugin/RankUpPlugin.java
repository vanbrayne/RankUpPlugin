package se.fredsfursten.rankupplugin;

import java.io.File;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import se.fredsfursten.plugintools.Misc;
import se.fredsfursten.plugintools.PluginConfig;

public final class RankUpPlugin extends JavaPlugin implements Listener {
	private static File RankUpStorageFile;

	@Override
	public void onEnable() {	
		Misc.enable(this);
		PluginConfig.get(this);
		RankUpStorageFile = new File(getDataFolder(), "RankUp.bin");
		getServer().getPluginManager().registerEvents(this, this);	
		Commands.get().enable(this);
		RankUp.get().enable(this);
	}

	@Override
	public void onDisable() {
		Commands.get().disable();
		RankUp.get().disable();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 0) {
			sender.sendMessage("Incomplete command...");
			return false;
		}
		
		if (!(sender instanceof Player)) {
			sender.sendMessage("You must be a player to use this command.");
			return false;
		}

		//String command = args[0].toLowerCase();
		Commands.get().rankupCommand((Player) sender, args);
		return true;
	}



	public static File getStorageFile()
	{
		return RankUpStorageFile;
	}
}
