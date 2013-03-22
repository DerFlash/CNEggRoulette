package me.derflash.plugins.eggroulette;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_5_R2.CraftWorld;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Wool;

public class Utils {

    static Wool getWoolForBlock(final Block block) {
        if (block.getType() == Material.WOOL) {
            final MaterialData data = block.getType().getNewData(block.getData());
            if (data instanceof Wool) {
                return (Wool) data;
            }
        }
        return null;
    }

    static void showHelp(final Player player, final CNEggRoulette plugin) {
        player.sendMessage(ChatColor.AQUA + plugin.translate("help1"));
        player.sendMessage(ChatColor.AQUA + plugin.translate("help2"));
        player.sendMessage(ChatColor.AQUA + plugin.translate("help3"));
        player.sendMessage(ChatColor.AQUA + plugin.translate("help4"));
        if (player.hasPermission("eggroulette.admin")) {
            player.sendMessage(ChatColor.AQUA + plugin.translate("help5"));
        }
    }

    static void showAdminHelp(final Player player, final CNEggRoulette plugin) {
        player.sendMessage(ChatColor.AQUA + plugin.translate("adminHelp1"));
        player.sendMessage(ChatColor.AQUA + plugin.translate("adminHelp2"));
        player.sendMessage(ChatColor.AQUA + plugin.translate("adminHelp3"));
        player.sendMessage(ChatColor.AQUA + plugin.translate("adminHelp4"));
        player.sendMessage(ChatColor.AQUA + plugin.translate("adminHelp5"));
    }
    
    static LivingEntity spawnChicken(final Location loc) {
        final net.minecraft.server.v1_5_R2.World notchWorld = ((CraftWorld) loc.getWorld()).getHandle();
        final RouletteChicken chicken = new RouletteChicken(loc, notchWorld);
        notchWorld.addEntity(chicken, CreatureSpawnEvent.SpawnReason.CUSTOM);
        return (LivingEntity) chicken.getBukkitEntity();
    }
}
