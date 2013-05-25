package org.fatecrafters.plugins;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PMTask implements Runnable {

	private final PermissionMessages plugin;

	public PMTask(PermissionMessages plugin) {
		this.plugin = plugin;
	}

	private Boolean cycle;
	private String name;
	private String perm;
	private String rmessage;

	@Override
	public void run() {
		for (Object perms : PMUtil.loops.keySet()) {
			if (PMUtil.loops.get(perms) > System.currentTimeMillis())
				continue;
			perm = perms.toString();
			if (PMUtil.methods.get(perm).equalsIgnoreCase("cycle") || PMUtil.methods.get(perm).equalsIgnoreCase("null")) {
				cycle = true;
			} else {
				cycle = false;
				rmessage = PMUtil.getRandomMessage(perm);
			}
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					for (Player p : Bukkit.getOnlinePlayers()) {
						name = p.getName();
						if (PMUtil.silences.get(name) != null && PMUtil.silences.get(name))
							continue;
						if (!p.hasPermission("permissionmessages."+perm))
							continue;
						if (PMUtil.disabledWorlds.get(perm).contains(p.getWorld().getName()))
							continue;
						if (cycle) {
							for (String message : PMUtil.getConfig().getStringList("PermissionMessages."+perm+".messages")) {
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("$player", name)));
							}
						} else {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', rmessage.replace("$player", name)));
						}
					}
				}
			});
			if (!PMUtil.getConfig().getString("PermissionMessages."+perm+".timer").contains("-")) {
				PMUtil.loops.put(perm, System.currentTimeMillis() + PMUtil.millis.get(perms));
			} else {
				PMUtil.loops.put(perm, System.currentTimeMillis() + PMUtil.getRandomTime(perm));
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
