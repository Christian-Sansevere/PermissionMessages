package org.fatecrafters.plugins;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public class PMUtil {

	private static PermissionMessages plugin;
	public static void setPlugin(PermissionMessages plugin) {
		PMUtil.plugin = plugin;
	}
	public static FileConfiguration getConfig() {
		return plugin.getConfig();
	}

	public static Long getMilliTime(String configloc) {
		String configtime = plugin.getConfig().getString("PermissionMessages."+configloc);
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

	public static int getRandomTime(String perm) {
		String timespan = getConfig().getString("PermissionMessages."+perm+".timer");
		String[] split = timespan.split("-");
		Random r;
		r = new Random();
		int low, high;
		if(split[0].contains("s")) {
			String slow = split[0].replace("s", "");
			low = Integer.parseInt(slow)*1000;
			if (split[1].contains("s")) {
				String shigh = split[1].replace("s", "");
				high = Integer.parseInt(shigh)*1000;
				return r.nextInt(high - low + 1) + low;
			} else if (split[1].contains("m")) {
				String shigh = split[1].replace("m", "");
				high = Integer.parseInt(shigh)*60000;
				return r.nextInt(high - low + 1) + low;
			} else if (split[1].contains("h")) {
				String shigh = split[1].replace("h", "");
				high = Integer.parseInt(shigh)*3600000;
				return r.nextInt(high - low + 1) + low;
			}
		} else if(split[0].contains("m")) {
			String slow = split[0].replace("m", "");
			low = Integer.parseInt(slow)*60000;
			if (split[1].contains("s")) {
				String shigh = split[1].replace("s", "");
				high = Integer.parseInt(shigh)*1000;
				return r.nextInt(high - low + 1) + low;
			} else if (split[1].contains("m")) {
				String shigh = split[1].replace("m", "");
				high = Integer.parseInt(shigh)*60000;
				return r.nextInt(high - low + 1) + low;
			} else if (split[1].contains("h")) {
				String shigh = split[1].replace("h", "");
				high = Integer.parseInt(shigh)*3600000;
				return r.nextInt(high - low + 1) + low;
			}
		} else if(split[0].contains("h")) {
			String slow = split[0].replace("m", "");
			low = Integer.parseInt(slow)*3600000;
			if (split[1].contains("s")) {
				split[1].replace("s", "");
				high = Integer.parseInt(split[1])*1000;
				return r.nextInt(high - low + 1) + low;
			} else if (split[1].contains("m")) {
				split[1].replace("m", "");
				high = Integer.parseInt(split[1])*60000;
				return r.nextInt(high - low + 1) + low;
			} else if (split[1].contains("h")) {
				split[1].replace("h", "");
				high = Integer.parseInt(split[1])*3600000;
				return r.nextInt(high - low + 1) + low;
			}
		}
		plugin.getServer().getLogger().severe("[PermissionMessages] Error in random timer configuration!");
		return 0;	
	}

	public static void addToHashmap() {
		for (String perms : getConfig().getConfigurationSection("PermissionMessages").getKeys(false)) {
			if (!getConfig().getString("PermissionMessages."+perms+".timer").contains("-")) {
				Long milli = getMilliTime(perms+".timer");
				PermissionMessages.loops.put(perms, System.currentTimeMillis()+milli);
				PermissionMessages.millis.put(perms, milli);
			} else {
				Long milli = (long) getRandomTime(perms);
				PermissionMessages.loops.put(perms, System.currentTimeMillis()+milli);
			}
		}
	}

	public static void reload() {
		plugin.reloadConfig();
		addToHashmap();
		for (Object key : PermissionMessages.loops.keySet()) {
			Permission perm = new Permission("permissionmessages."+key.toString());
			perm.setDefault(PermissionDefault.FALSE);
			if (plugin.getServer().getPluginManager().getPermission("permissionmessages."+key.toString()) == null) {
				plugin.getServer().getPluginManager().addPermission(perm);
			}
		}
	}

	public static void addToConfig(String perm, String timer, String message) {
		List<String> list = Arrays.asList(message);
		getConfig().set("PermissionMessages."+perm+".timer", timer);
		getConfig().set("PermissionMessages."+perm+".messages", list);
		plugin.saveConfig();
	}

	public static boolean addToExistingPerm(String perm, String message) {
		for (String perms : getConfig().getConfigurationSection("PermissionMessages").getKeys(false)) {
			if (perms.equalsIgnoreCase(perm)) {
				List<String> list = getConfig().getStringList("PermissionMessages."+perms+".messages");
				list.add(message);
				getConfig().set("PermissionMessages."+perms+".messages", list);
				plugin.saveConfig();
				return true;
			}
		}
		return false;
	}

	public static boolean checkIfArgIsTimer(String arg) {
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
