package me.derflash.plugins.eggroulette;

import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class RoulettePlayer {

    private CNEggRoulette plugin;
    private Player player;
    private Sign sign;
    private HashMap<String, Integer> bets;

    RoulettePlayer(Player player, Sign sign, CNEggRoulette plugin) {
        this.player = player;
        this.sign = sign;
        this.bets = new HashMap<String, Integer>();
        this.plugin = plugin;
    }

    public Player getPlayer() {
        return player;
    }

    public Sign getSign() {
        return sign;
    }

    public HashMap<String, Integer> getBets() {
        return bets;
    }

    public boolean checkMaxBets(int adding) {
        int max = plugin.getMax();
        int count = 0;
        for (Integer val : bets.values()) {
            count += val;
        }
        return (count + adding) <= max;
    }

    public void addBet(final String color) {
        int error = 0;
        final int worth = plugin.getBet();
        final Integer bet = bets.get(color);
        if (bet != null) {
            if (checkMaxBets(worth)) {
                if (CNEggRoulette.getEconomy().withdrawPlayer(player.getName(), worth).transactionSuccess()) {
                        bets.put(color, bet + worth);				
                } else {
                    error = 3;
                }
            } else {
                error = 1;
            }
        } else {
            if (bets.size() >= 3) {
                error = 2;
            } else {
                if (checkMaxBets(worth)) {
                    if (CNEggRoulette.getEconomy().withdrawPlayer(player.getName(), worth).transactionSuccess()) {
                        bets.put(color, worth);
                    } else {
                        error = 3;
                    }
                } else {
                    error = 1;
                }
            }
        }

        switch (error) {
            case 0:
                player.sendMessage(ChatColor.DARK_AQUA + plugin.translate("addBetAdd", new String[] {"bet", Integer.toString(bets.get(color)), "color", color, "currency", plugin.getCurrency()}));
                break;
            case 1:
                player.sendMessage(ChatColor.DARK_AQUA + plugin.translate("addBetMax", new String[] {"max", Integer.toString(plugin.getMax()), "currency", plugin.getCurrency()}));
                break;
            case 2:
                player.sendMessage(ChatColor.DARK_AQUA + plugin.translate("addBetColors"));
                break;
            case 3:
                player.sendMessage(ChatColor.DARK_AQUA + plugin.translate("addBetMoney"));
                break;
        }
    }

    public void removeBet(String color) {
        int error = 0;
        int worth = plugin.getBet();

        Integer bet = bets.get(color);
        if (bet != null) {
            if (bet.intValue() > worth) {
                if (CNEggRoulette.getEconomy().depositPlayer(player.getName(), worth).transactionSuccess()) {
                    bets.put(color, bet - worth);
                } else {
                    error = 3;
                }
            } else {
                if (CNEggRoulette.getEconomy().depositPlayer(player.getName(), worth).transactionSuccess()) {
                    bets.remove(color);
                    error = 1;
                } else {
                    error = 3;
                }
            }
        } else {
            error = 2;
        }
        switch (error) {
            case 0:
                player.sendMessage(ChatColor.DARK_AQUA + plugin.translate("delBetDel", new String[] {"amount", Integer.toString(worth), "color", color, "currency", plugin.getCurrency()}));
                break;
            case 1:
                player.sendMessage(ChatColor.DARK_AQUA + plugin.translate("delBetAll", new String[] {"color", color}));
                break;
            case 2:
                player.sendMessage(ChatColor.DARK_AQUA + plugin.translate("delBetZero"));
                break;
            case 3:
                player.sendMessage(ChatColor.DARK_AQUA + plugin.translate("delBetMoney"));
                break;
        }
    }

    public void updateSign() {
        String playerName = player.getName();
        if (playerName.length() > 11) {
            playerName = playerName.substring(0, 10);
        }
        sign.setLine(0, ChatColor.AQUA + "[" + playerName + "]");
        if (bets.size() > 0) {
            int counter = 1;
            for (String color : bets.keySet()) {
                if (counter > 3) {
                    return;
                }
                sign.setLine(counter, color.replaceAll("_", "") + ": " + bets.get(color) + "c");
                counter++;
            }
            for (int i = counter; i <= 3; i++) {
                sign.setLine(i, "");
            }
        } else {
            sign.setLine(1, plugin.translate("signNew1"));
            sign.setLine(2, plugin.translate("signNew2"));
            sign.setLine(3, plugin.translate("signNew3"));
        }
        sign.update();
    }

    public static void resetSign(Sign _sign, CNEggRoulette plugin) {
        _sign.setLine(0, ChatColor.WHITE + "[EggRoulette]");
        _sign.setLine(1, plugin.translate("signFree1"));
        _sign.setLine(2, plugin.translate("signFree2"));
        _sign.setLine(3, plugin.translate("signFree3"));
        _sign.update();
    }

    public void resetSign() {
        RoulettePlayer.resetSign(sign, plugin);
    }

    public boolean checkForWins(String winColor) {
        for (String color : bets.keySet()) {
            if (color.equals(winColor)) {
                int bet = bets.get(color);
                int win = bet * 2;
                if (CNEggRoulette.getEconomy().depositPlayer(player.getName(), win).transactionSuccess()) {
                    player.sendMessage(ChatColor.DARK_AQUA + plugin.translate("winMsgPriv", new String[] {"currency", plugin.getCurrency(), "color", color, "win", Integer.toString(win)}));
                } else {
                    player.sendMessage(ChatColor.DARK_AQUA + plugin.translate("winMsgDepositFail", new String[] {"currency", plugin.getCurrency(), "win", Integer.toString(win)}));
                }
                sign.setLine(0, ChatColor.GREEN + "[" + player.getName() + "]");
                sign.setLine(1, "* " + color + " *");
                sign.setLine(2, "Gewinn: " + win + "c");
                sign.setLine(3, plugin.translate("signLuck3"));
                sign.update();
                return true;
            }
        }
        player.sendMessage(ChatColor.DARK_AQUA + plugin.translate("noLuck"));
        sign.setLine(0, ChatColor.RED + "[" + player.getName() + "]");
        sign.setLine(1, plugin.translate("signNoLuck1"));
        sign.setLine(2, plugin.translate("signNoLuck2"));
        sign.setLine(3, plugin.translate("signNoLuck3"));
        sign.update();
        return false;
    }
}
