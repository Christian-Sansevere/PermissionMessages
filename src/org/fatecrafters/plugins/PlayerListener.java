package org.fatecrafters.plugins;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent e) {
		String name = e.getPlayer().getName();
		if (PermissionMessages.silences.get(name) == null) {
			PermissionMessages.silences.put(name, false);
		}
	}

}
