package org.fatecrafters.plugins;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PMTask implements Runnable {

	private final PermissionMessages plugin;

	public PMTask(PermissionMessages plugin) {
		this.plugin = plugin;
	}


	@Override
	public void run() {
		for (String perms : plugin.getConfig().getConfigurationSection("PermissionMessages").getKeys(false)) {
			if (PermissionMessages.loops.get(perms) < System.currentTimeMillis()) {
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (p.hasPermission("permissionmessages."+perms)) {
						for (String message : plugin.getConfig().getStringList("PermissionMessages."+perms+".messages")) {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("$player", p.getName())));
						}
					}
				}
				PermissionMessages.loops.put(perms, System.currentTimeMillis() + PermissionMessages.millis.get(perms));
			}
		}
	}

}
