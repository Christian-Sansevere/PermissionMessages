package org.fatecrafters.plugins;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

public class PermissionMessages extends JavaPlugin {

	public static HashMap<String, Long> timers = new HashMap<String, Long>();
	public static HashMap<String, Long> millis = new HashMap<String, Long>();
	public static HashMap<String, Long> loops = new HashMap<String, Long>();

	@Override
	public void onEnable() { 
		saveDefaultConfig();
		addToHashmap();
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new PMTask(this), 0L, 50L);
		for (Object key : PermissionMessages.timers.keySet()) {
			Permission perm = new Permission("permissionmessages."+key.toString());
			perm.setDefault(PermissionDefault.FALSE);
			getServer().getPluginManager().addPermission(perm);
			getLogger().info("[PermissionMessages] Task loaded for "+key.toString()+". Timer: "+PermissionMessages.timers.get(key)+" ticks");
		}
		getLogger().info("[PermissionMessages] PermissionMessages has been enabled.");
	}

	@Override
	public void onDisable() {
		getServer().getScheduler().cancelAllTasks();
		getServer().getPluginManager().disablePlugin(this);
		getLogger().info("[PermissionMessages] PermissionMessages has been disabled.");
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equalsIgnoreCase("permissionmessages")) {
			if (args.length >= 1) {
				if (args[0].equalsIgnoreCase("reload")) {
					if (!sender.hasPermission("permissionmessages.reload")) {
						sender.sendMessage(ChatColor.GRAY + "You do not have permission.");
						return false;
					}
					Long first = System.currentTimeMillis();
					reload();
					sender.sendMessage(ChatColor.GRAY + "Config and Timers reloaded.");
					sender.sendMessage(ChatColor.GRAY + "Took " + ChatColor.YELLOW + (System.currentTimeMillis() - first) + "ms" + ChatColor.GRAY + ".");
					return true;
				} else if (args[0].equalsIgnoreCase("add")) {
					if (!sender.hasPermission("permissionmessages.add")) {
						sender.sendMessage(ChatColor.GRAY + "You do not have permission.");
						return false;
					}
					String perm = args[1];
					StringBuilder stringbuild = new StringBuilder();
					for(int i = 3; i < args.length; i++) {
						if (i > 3) stringbuild.append(" ");
						stringbuild.append(args[i]);
					}
					String message = stringbuild.toString();
					if (addToExistingPerm(perm, message)) {
						sender.sendMessage(ChatColor.GRAY+"Message has been added to "+ChatColor.LIGHT_PURPLE+perm);
						reload();
						return true;
					} else {
						sender.sendMessage(ChatColor.GRAY + "The permission does not exist, please use the create method.");
						return false;
					}
				} else if (args[0].equalsIgnoreCase("remove")) {
					if (!sender.hasPermission("permissionmessages.remove")) {
						sender.sendMessage(ChatColor.GRAY + "You do not have permission.");
						return false;
					}
					if (args.length == 2) { 
						String perm = args[1];
						getConfig().set("PermissionMessages."+perm, null);
						saveConfig();
						reload();
						sender.sendMessage(ChatColor.LIGHT_PURPLE+perm+ChatColor.GRAY+" was removed from the config.");
						return true;
					} else {
						sender.sendMessage(ChatColor.GRAY+"Please use "+ChatColor.LIGHT_PURPLE+"/permissionmsgs remove <thePermissionInConfig>");
						return false;
					}
				} else if (args[0].equalsIgnoreCase("create")) {
					if (!sender.hasPermission("permissionmessages.create")) {
						sender.sendMessage(ChatColor.GRAY + "You do not have permission.");
						return false;
					} else if (!checkIfArgIsTimer(args[2])) {
						sender.sendMessage(ChatColor.GRAY + "You did not correctly enter a time. Please use a number with <s/m/h>");
						return false;
					}
					String perm = args[1];
					String time = args[2];
					StringBuilder stringbuild = new StringBuilder();
					for(int i = 3; i < args.length; i++) {
						if (i > 3) stringbuild.append(" ");
						stringbuild.append(args[i]);
					}
					String message = stringbuild.toString();
					addToConfig(perm, time, message);
					sender.sendMessage(ChatColor.LIGHT_PURPLE+perm+ChatColor.GRAY+" was added to the config.");
					reload();
					return true;
				}
			}
		}
		return false;
	}
	
	private Long getTickTime(String configloc) {
		String configtime = getConfig().getString("PermissionMessages."+configloc);
		if (configtime.contains("s")) {
			configtime = configtime.replace("s", "");
			return (Long.parseLong(configtime)*1000)/50;
		} else if (configtime.contains("m")) {
			configtime = configtime.replace("m", "");
			return (Long.parseLong(configtime)*60000)/50;
		} else if (configtime.contains("h")) {
			configtime = configtime.replace("h", "");
			return (Long.parseLong(configtime)*3600000)/50;
		} else {
			return Long.parseLong(configtime);
		}
	}
	
	private Long getMilliTime(String configloc) {
		String configtime = getConfig().getString("PermissionMessages."+configloc);
		if (configtime.contains("s")) {
			configtime = configtime.replace("s", "");
			return (Long.parseLong(configtime)*1000);
		} else if (configtime.contains("m")) {
			configtime = configtime.replace("m", "");
			return (Long.parseLong(configtime)*60000);
		} else if (configtime.contains("h")) {
			configtime = configtime.replace("h", "");
			return (Long.parseLong(configtime)*3600000);
		} else {
			return Long.parseLong(configtime);
		}
	}

	private void addToHashmap() {
		for (String perms : getConfig().getConfigurationSection("PermissionMessages").getKeys(false)) {
			Long timer = getTickTime(perms+".timer");
			Long milli = getMilliTime(perms+".timer");
			timers.put(perms, timer);
			loops.put(perms, System.currentTimeMillis()+milli);
			millis.put(perms, milli);
		}
	}

	private void reload() {
		reloadConfig();
		PermissionMessages.timers.clear();
		addToHashmap();
		for (Object key : PermissionMessages.timers.keySet()) {
			Permission perm = new Permission("permissionmessages."+key.toString());
			perm.setDefault(PermissionDefault.FALSE);
			if (getServer().getPluginManager().getPermission("permissionmessages."+key.toString()) == null) {
				getServer().getPluginManager().addPermission(perm);
			}
		}
	}

	private void addToConfig(String perm, String timer, String message) {
		List<String> list = Arrays.asList(message);
		getConfig().set("PermissionMessages."+perm+".timer", timer);
		getConfig().set("PermissionMessages."+perm+".messages", list);
		saveConfig();
	}
	
	private boolean addToExistingPerm(String perm, String message) {
		for (String perms : getConfig().getConfigurationSection("PermissionMessages").getKeys(false)) {
			if (perms.equalsIgnoreCase(perm)) {
				List<String> list = getConfig().getStringList("PermissionMessages."+perms+".messages");
				list.add(message);
				getConfig().set("PermissionMessages."+perms+".messages", list);
				saveConfig();
				return true;
			}
		}
		return false;
	}
	
	private boolean checkIfArgIsTimer(String arg) {
		if (arg.contains("s")) {
			return true;
		} else if (arg.contains("m")) {
			return true;
		} else if (arg.contains("h")) {
			return true;
		}
		return false;
	}

}
