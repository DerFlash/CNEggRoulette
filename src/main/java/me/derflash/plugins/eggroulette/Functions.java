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
    
    
    
    static void showHelp(Player player) {
		player.sendMessage(ChatColor.AQUA + "[EggRoulette] Befehlsübersicht");
		player.sendMessage(ChatColor.AQUA + "* /egg restart|start - Schaltet das Spiel (wieder) aktiv");
		player.sendMessage(ChatColor.AQUA + "* /egg go - Schliesst die Wetten und gibt das Huhn frei");
		player.sendMessage(ChatColor.AQUA + "* /egg reset - Setzt das Spiel zurück (Achtung!)");
		
		if (player.hasPermission("eggroulette.admin")) player.sendMessage(ChatColor.AQUA + "* /egg admin - Gibt eine Übersicht der Adminbefehle aus");
    }
    
    static void showAdminHelp(Player player) {
		player.sendMessage(ChatColor.AQUA + "[EggRoulette] Befehlsübersicht");
		player.sendMessage(ChatColor.AQUA + "* /egg setup - Startet das EggRoulette Setup");
		player.sendMessage(ChatColor.AQUA + "* /egg sign - Aktiviert/Deaktiviert den Schild-Erstell-Modus");
		player.sendMessage(ChatColor.AQUA + "* /egg set <key> <value> - Stellt die verschiedenen Settings ein");
		player.sendMessage(ChatColor.AQUA + "< Keys: bet, max, respawn, world >");
		
		if (player.hasPermission("eggroulette.admin")) player.sendMessage(ChatColor.AQUA + "* /egg admin - Gibt eine Übersicht der Adminbefehle aus");
    }
    
    
}
