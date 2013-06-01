package org.fatecrafters.plugins;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PMTask implements Runnable {

	private final PermissionMessages plugin;

	public PMTask(PermissionMessages plugin) {
		this.plugin = plugin;
	}

	private Boolean cycle;
	private String rmessage;
	private List<String> messagesList = new ArrayList<String>();

	@Override
	public void run() {
		for (final String perm : PMUtil.configPerms) {
			if (PMUtil.loops.get(perm) > System.currentTimeMillis())
				continue;
			if (PMUtil.methods.get(perm).equalsIgnoreCase("cycle") || PMUtil.methods.get(perm).equalsIgnoreCase("null")) {
				cycle = true;
			} else {
				cycle = false;
				rmessage = PMUtil.getRandomMessage(perm);
			}
			messagesList.addAll(plugin.getConfig().getStringList("PermissionMessages."+perm+".messages"));
			
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					for (Player p : Bukkit.getOnlinePlayers()) {
						String name = p.getName();
						if (PMUtil.silences.get(name) != null && PMUtil.silences.get(name))
							continue;
						if (!p.hasPermission("permissionmessages."+perm))
							continue;
						if (PMUtil.disabledWorlds.get(perm).contains(p.getWorld().getName()))
							continue;
						if (cycle) {
							for (String message : messagesList) {
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("$player", name)));
							}
						} else {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', rmessage.replace("$player", name)));
						}
					}
				}
			}, 0L);
			
			if (!plugin.getConfig().getString("PermissionMessages."+perm+".timer").contains("-")) {
				PMUtil.loops.put(perm, System.currentTimeMillis() + PMUtil.millis.get(perm));
			} else {
				PMUtil.loops.put(perm, System.currentTimeMillis() + PMUtil.getRandomTime(perm));
			}
			messagesList.clear();
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}