package me.derflash.plugins.eggroulette;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Wool;

public class Functions {

    static Wool getWoolForBlock(Block block) {
		if (block.getType() == Material.WOOL) {
        	MaterialData data = block.getType().getNewData(block.getData());
        	if (data instanceof Wool) {
        	    return (Wool)data;
        	}
		}
		return null;
    }
    
    
    
    static void showHelp(Player player, CNEggRoulette plugin) {
		player.sendMessage(ChatColor.AQUA + plugin.translate("help1"));
		player.sendMessage(ChatColor.AQUA + plugin.translate("help2"));
		player.sendMessage(ChatColor.AQUA + plugin.translate("help3"));
		player.sendMessage(ChatColor.AQUA + plugin.translate("help4"));
		
		if (player.hasPermission("eggroulette.admin")) player.sendMessage(ChatColor.AQUA + plugin.translate("help5"));
    }
    
    static void showAdminHelp(Player player, CNEggRoulette plugin) {
		player.sendMessage(ChatColor.AQUA + plugin.translate("adminHelp1"));
		player.sendMessage(ChatColor.AQUA + plugin.translate("adminHelp2"));
		player.sendMessage(ChatColor.AQUA + plugin.translate("adminHelp3"));
		player.sendMessage(ChatColor.AQUA + plugin.translate("adminHelp4"));
		player.sendMessage(ChatColor.AQUA + plugin.translate("adminHelp5"));
    }
    
    
}
