package cc.eternalcraft.eternalCraftStaffChat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public final class EternalCraftStaffChat extends JavaPlugin implements Listener, CommandExecutor {

    private ArrayList<Player> staffChatEnabled;

    private String staffChatEnableMessage;
    private String staffChatDisableMessage;
    private String noPermissionMessage;
    private String notPlayerMessage;
    private String staffChatMessageFormat;

    @Override
    public void onEnable() {
        staffChatEnabled = new ArrayList<Player>();

        // Load the configuration
        loadConfig();

        // Set command executor for /sc
        getCommand("sc").setExecutor(this);

        // Register events
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        staffChatEnabled.clear();
    }

    public void loadConfig() {
        saveDefaultConfig();
        saveConfig();

        // Load messages from config.yml
        staffChatEnableMessage = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.staffChatEnabled", "&aStaff chat enabled!"));
        staffChatDisableMessage = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.staffChatDisabled", "&cStaff chat disabled!"));
        noPermissionMessage = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.noPermission", "&cYou don't have permission to use this command."));
        notPlayerMessage = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.notPlayer", "&cOnly players can use this command."));
        staffChatMessageFormat = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.staffChatFormat", "&7[StaffChat] &b%player%: &f%message%"));
    }

    // Command handler for /sc (staff chat toggle)
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("sc")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(notPlayerMessage);
                return true;
            }

            Player player = (Player) sender;

            if (player.hasPermission("staffchat.use")) {
                // Toggle staff chat for the player
                if (staffChatEnabled.contains(player)) {
                    staffChatEnabled.remove(player);
                    player.sendMessage(staffChatDisableMessage);
                } else {
                    staffChatEnabled.add(player);
                    player.sendMessage(staffChatEnableMessage);
                }
            } else {
                sender.sendMessage(noPermissionMessage);
            }
            return true;
        }
        return false;
    }

    // Event handler for sending messages in staff chat
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // If the player has staff chat enabled
        if (staffChatEnabled.contains(player)) {
            // Removes old chat message format
            event.setCancelled(true);  // Prevent the message from going to global chat

            // Format the new staff chat message
            String message = staffChatMessageFormat.replace("%player%", player.getName()).replace("%message%", event.getMessage());

            // Send the message only to players with the staff chat enabled
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.hasPermission("staffchat.use")) {
                    System.out.println(message);
                    onlinePlayer.sendMessage(message);
                }
            }
        }
    }
}
