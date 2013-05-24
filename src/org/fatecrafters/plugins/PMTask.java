package org.fatecrafters.plugins;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PMTask implements Runnable {

	@Override
	public void run() {
		for (Object perms : PermissionMessages.loops.keySet()) {
			if (PermissionMessages.loops.get(perms) < System.currentTimeMillis()) {
				String perm = perms.toString();
				for (Player p : Bukkit.getOnlinePlayers()) {
					String name = p.getName();
					Boolean silenced = PermissionMessages.silences.get(name);
					if (!silenced) {
						if (p.hasPermission("permissionmessages."+perm)) {
							for (String message : PMUtil.getConfig().getStringList("PermissionMessages."+perm+".messages")) {
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("$player", name)));
							}
						}
					}
				}
				if (!PMUtil.getConfig().getString("PermissionMessages."+perm+".timer").contains("-")) {
					PermissionMessages.loops.put(perm, System.currentTimeMillis() + PermissionMessages.millis.get(perms));
				} else {
					PermissionMessages.loops.put(perm, System.currentTimeMillis() + PMUtil.getRandomTime(perm));
				}
			}
		}
	}

}
