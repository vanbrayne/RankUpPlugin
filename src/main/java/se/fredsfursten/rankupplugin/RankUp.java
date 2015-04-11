package se.fredsfursten.rankupplugin;

import java.util.List;
import java.util.Set;

import me.botsko.oracle.Oracle;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.tyrannyofheaven.bukkit.zPermissions.ZPermissionsService;

import se.fredsfursten.plugintools.ConfigurableFormat;

public class RankUp {
	private static RankUp singleton = null;
	private static ConfigurableFormat giveCommandFormat;
	private static ConfigurableFormat playTimeMessage;
	private static ConfigurableFormat nextRankMessage;

	private JavaPlugin plugin = null;
	private ZPermissionsService permissionService = null;
	private Plugin oraclePlugin;
	private String[] rankGroups;
	private Integer[] afterHours;

	private RankUp() {
		giveCommandFormat = new ConfigurableFormat("GiveCommand", 3,
				"give %s %s %d");
		playTimeMessage = new ConfigurableFormat("PlayTimeMessage", 1,
				"You have played %d hours.");
		nextRankMessage = new ConfigurableFormat("NextRankMessage", 2,
				"You have %d hours left to rank %s.");
		List<String> stringList = RankUpPlugin.getPluginConfig().getStringList("RankGroups");
		if (stringList == null) this.rankGroups = new String[0];
		else this.rankGroups = stringList.toArray(new String[0]);
		Bukkit.getLogger().info(String.format("RankGroups: %s", arrayToString(this.rankGroups)));
		List<Integer> integerList = RankUpPlugin.getPluginConfig().getIntegerList("AfterHours");
		if (integerList == null) this.afterHours = new Integer[0];
		else this.afterHours = integerList.toArray(new Integer[0]);
		Bukkit.getLogger().info(String.format("RankHours: %s", arrayToString(this.afterHours)));
	}

	private String arrayToString(String[] stringList) {
		String s = "[";
		for (String string : stringList) {
			if (!s.equalsIgnoreCase("[")) s += ", ";
			s += string;
		}
		s +="]";
		return s;
	}

	private String arrayToString(Integer[] integerList) {
		String s = "[";
		for (Integer integer : integerList) {
			if (!s.equalsIgnoreCase("[")) s += ", ";
			s += integer.toString();
		}
		s +="]";
		return s;
	}

	static RankUp get()
	{
		if (singleton == null) {
			singleton = new RankUp();
		}
		return singleton;
	}

	void enable(JavaPlugin plugin){
		this.plugin = plugin;
		connectToPermissionService();
		connectToOracle(plugin);
	}

	private void connectToOracle(JavaPlugin plugin) {
		this.oraclePlugin = plugin.getServer().getPluginManager().getPlugin("Oracle");
		if (this.oraclePlugin != null && this.oraclePlugin.isEnabled()) {
			plugin.getLogger().info("Succesfully hooked into the Oracle plugin!");
		} else {
			this.oraclePlugin = null;
			Bukkit.getLogger().warning("RankUp doesn't work without the Oracle plugin");			
		}
	}

	private void connectToPermissionService() {
		try {
			this.permissionService = Bukkit.getServicesManager().load(ZPermissionsService.class);
			// this.oracleService = Bukkit.getServicesManager().load(Oracle.class); 
		}
		catch (NoClassDefFoundError e) {
			// Eh...
		}
		if (this.permissionService == null) {
			Bukkit.getLogger().warning("RankUp doesn't work without the zPermissions plugin");
		}
	}

	void disable() {
	}

	public void rankup(Player player) {
		int playTimeHours = 0;
		if (this.oraclePlugin == null) {
			player.sendMessage("RankUp doesn't work without the Oracle plugin");
		} else {
			playTimeHours = reportPlayTime(player, playTimeHours);
		}
		if (this.permissionService == null) {
			player.sendMessage("RankUp doesn't work without the zPermissions plugin");			
		} else {
			reportGroupMembership(player);
		}
		addRelevantGroups(player, playTimeHours);
		reportNextGroup(player, playTimeHours);
	}

	private int reportPlayTime(Player player, int playTimeHours) {
		Integer playTime = Oracle.playtimeHours.get(player.getUniqueId());
		if (playTime == null) {
			player.sendMessage(String.format("Could not find any playtime information for player %s.", player.getName()));
		} else {
			playTimeHours = playTime.intValue();
			RankUp.playTimeMessage.sendMessage(player, playTimeHours);
		}
		return playTimeHours;
	}

	private void reportGroupMembership(Player player) {
		Set<String> groups = this.permissionService.getPlayerGroups(player.getUniqueId());
		if ((groups == null) || (groups.size() == 0)) {
			player.sendMessage("You are not member of any groups");
		} else {
			for (String groupName : groups) {
				player.sendMessage(String.format("You are a member of group %s.", groupName));
			}
		}
	}

	private String reportNextGroup(Player player, int playTimeHours) {
		String groupName = null;
		for (int i = 0; i < this.afterHours.length; i++) {
			int rankHour = this.afterHours[i].intValue();
			groupName = this.rankGroups[i];
			if (rankHour > playTimeHours) {
				RankUp.nextRankMessage.sendMessage(player, rankHour - playTimeHours, groupName);			
				return groupName;
			}
		}
		player.sendMessage(String.format("You have reached the highest rank, %s.", groupName));					
		return null;
	}

	private void addRelevantGroups(Player player, int playTimeHours) {
		for (int i = 0; i < this.afterHours.length; i++) {
			int rankHour = this.afterHours[i].intValue();
			String groupName = this.rankGroups[i];
			if (rankHour > playTimeHours) {
				break;
			}
			// Increase rank
			player.sendMessage(String.format("Added you to group %s.", groupName));
		}
	}
}