package org.fatecrafters.plugins;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

public class PermissionMessages extends JavaPlugin {

	@SuppressWarnings("deprecation")
	@Override
	public void onEnable() { 
		saveDefaultConfig();
		PMUtil.setPlugin(this);
		PMUtil.addToHashmap();
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new PMTask(this), 15L, 50L);
		for (Object key : PMUtil.loops.keySet()) {
			Permission perm = new Permission("permissionmessages."+key.toString());
			perm.setDefault(PermissionDefault.FALSE);
			getServer().getPluginManager().addPermission(perm);
			getLogger().info("[PermissionMessages] Task loaded for "+key.toString());
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
					PMUtil.reload();
					sender.sendMessage(ChatColor.GRAY + "Config and Timers reloaded.");
					sender.sendMessage(ChatColor.GRAY + "Took " + ChatColor.YELLOW + (System.currentTimeMillis() - first) + "ms" + ChatColor.GRAY + ".");
					return true;
				} else if (args[0].equalsIgnoreCase("add")) {
					if (!sender.hasPermission("permissionmessages.add")) {
						sender.sendMessage(ChatColor.GRAY + "You do not have permission.");
						return false;
					} else if (args.length <= 3) {
						sender.sendMessage(ChatColor.GRAY + "Correct usage:"+ChatColor.LIGHT_PURPLE+"/pm add <permission> <message>");
						return false;
					}
					String perm = args[1];
					StringBuilder stringbuild = new StringBuilder();
					for(int i = 3; i < args.length; i++) {
						if (i > 3) stringbuild.append(" ");
						stringbuild.append(args[i]);
					}
					String message = stringbuild.toString();
					if (PMUtil.addToExistingPerm(perm, message)) {
						sender.sendMessage(ChatColor.GRAY+"Message has been added to "+ChatColor.LIGHT_PURPLE+perm);
						PMUtil.reload();
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
						PMUtil.reload();
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
					} else if (args.length <= 4) {
						sender.sendMessage(ChatColor.GRAY+"Correct usage:"+ChatColor.LIGHT_PURPLE+"/pm create <permission> <time> <method> <message>");
						return false;
					} else if (!PMUtil.checkIfArgIsTimer(args[2])) {
						sender.sendMessage(ChatColor.GRAY + "You did not correctly enter a time. Please use a number with <s/m/h>");
						return false;
					}
					String perm = args[1];
					String method = args[3];
					String time = args[2];
					if (method.equalsIgnoreCase("cycle") || method.equalsIgnoreCase("random")) {
						StringBuilder stringbuild = new StringBuilder();
						for(int i = 4; i < args.length; i++) {
							if (i > 4) stringbuild.append(" ");
							stringbuild.append(args[i]);
						}
						String message = stringbuild.toString();
						PMUtil.addToConfig(perm, method, time, message);
						PMUtil.reload();
						sender.sendMessage(ChatColor.LIGHT_PURPLE+perm+ChatColor.GRAY+" was added to the config.");
						return true;
					} else {
						sender.sendMessage(ChatColor.GRAY + "Please only use "+ChatColor.LIGHT_PURPLE+"cycle"+ChatColor.GRAY+" or "+ChatColor.LIGHT_PURPLE+"random "+ChatColor.GRAY+"for the method.");
						return false;
					}
				} else if (args[0].equalsIgnoreCase("silence") || args[0].equalsIgnoreCase("stfu")) {
					if (!sender.hasPermission("permissionmessages.silence")) {
						sender.sendMessage(ChatColor.GRAY + "You do not have permission.");
						return false;
					} else if (!(sender instanceof Player)) {
						sender.sendMessage("This command can only be run by a player.");
						return false;
					}
					Player p = (Player) sender;
					String name = p.getName();
					Boolean silenced = PMUtil.silences.get(name);
					if (silenced == null || !silenced) {
						PMUtil.silences.put(name, true);
						p.sendMessage(ChatColor.GRAY+"You are now silenced from timed messages.");
						return true;
					} else {
						PMUtil.silences.put(name, false);
						p.sendMessage(ChatColor.GRAY+"You are no longer silenced.");
						return true;
					}
				} else if (args[0].equalsIgnoreCase("list")) {
					if (!sender.hasPermission("permissionmessages.list")) {
						sender.sendMessage(ChatColor.GRAY + "You do not have permission.");
						return false;
					}
					int i = 0;
					sender.sendMessage(ChatColor.GRAY+"~ "+ChatColor.LIGHT_PURPLE+"Permission Messages Loaded");
					sender.sendMessage(ChatColor.GRAY+"----------------------------------");
					for (Object perms : PMUtil.loops.keySet()) {
						i++;
						sender.sendMessage(ChatColor.DARK_GREEN+""+i+". "+ChatColor.GREEN+perms.toString());
					}
					return true;
				} else if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
					if (!sender.hasPermission("permissionmessages.help")) {
						sender.sendMessage(ChatColor.GRAY + "You do not have permission.");
						return false;
					}
					sender.sendMessage(ChatColor.GRAY+"~ "+ChatColor.LIGHT_PURPLE+"Commands");
					sender.sendMessage(ChatColor.GRAY+"------------------------");
					sender.sendMessage(ChatColor.LIGHT_PURPLE+"/pm reload "+ChatColor.GRAY+"- Reload the timers and configuration.");
					sender.sendMessage(ChatColor.LIGHT_PURPLE+"/pm create <permission> <time> <method> <message>"+ChatColor.GRAY+"- Create a new Permission Message.");
					sender.sendMessage(ChatColor.LIGHT_PURPLE+"/pm add <permission> <message>"+ChatColor.GRAY+"- Add a message to an existing Permission Message.");
					sender.sendMessage(ChatColor.LIGHT_PURPLE+"/pm list "+ChatColor.GRAY+"- List all of the loaded Permission Messages.");
					sender.sendMessage(ChatColor.LIGHT_PURPLE+"/pm remove <permission> "+ChatColor.GRAY+"- Remove a Permission Message.");
					sender.sendMessage(ChatColor.LIGHT_PURPLE+"/pm silence "+ChatColor.GRAY+"- Silence all messages from this plugin.");
				}
			}
		}
		return false;
	}

}
