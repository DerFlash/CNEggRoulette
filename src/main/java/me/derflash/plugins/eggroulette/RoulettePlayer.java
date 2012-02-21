package me.derflash.plugins.eggroulette;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class RoulettePlayer {

	private CNEggRoulette plugin;
	private Player player;
	private Sign sign;
	private HashMap <String, Integer> bets;
	
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
	public HashMap <String, Integer> getBets() {
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
	
	
	public void addBet(String color) {
		int error = 0;
		int worth = plugin.getBet();

		Integer bet = bets.get(color);
		if (bet != null) {
			
			if (checkMaxBets(worth)) {
				if (CNEggRoulette.getEconomy().withdrawPlayer(player.getName(), worth).transactionSuccess()) {
					bets.put(color, bet + worth);				
				} else error = 3;
				
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
					} else error = 3;
					
				} else {
					error = 1;
				}
			}
		}
		
	
		switch (error) {
		case 0: player.sendMessage(ChatColor.DARK_AQUA + "Du hast nun " + bets.get(color) + " Cublonen auf " + color + " gesetzt."); break;
		case 1: player.sendMessage(ChatColor.DARK_AQUA + "Du kannst nur bis zu " + plugin.getMax() + " Cublonen insgesamt einsetzen."); break;
		case 2: player.sendMessage(ChatColor.DARK_AQUA + "Du hast schon 3 Farben gewŠhlt. Setze weiter auf eine dieser Farben oder entferne die Gebote davon."); break;
		case 3: player.sendMessage(ChatColor.DARK_AQUA + "Du hast scheinbar nicht mehr genug Geld um dein Gebot zu plazieren."); break;
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
				} else error = 3;
				
			} else {
				if (CNEggRoulette.getEconomy().depositPlayer(player.getName(), worth).transactionSuccess()) {
					bets.remove(color);
					error = 1;
				} else error = 3;

			}
		} else {
			error = 2;
		}
		
		switch (error) {
		case 0: player.sendMessage(ChatColor.DARK_AQUA + "Du hast " + worth + " Cublonen von " + color + " entfernt."); break;
		case 1: player.sendMessage(ChatColor.DARK_AQUA + "Du hast nun alle Gebote von " + color + " entfernt."); break;
		case 2: player.sendMessage(ChatColor.DARK_AQUA + "Du kannst keine Gebote mehr von dieser Farbe entfernen."); break;
		case 3: player.sendMessage(ChatColor.DARK_AQUA + "Dein Gebot konnte deinem Konto nicht wieder gutgeschrieben werden. Wende dich an einen Admin!"); break;
		}
	}
	
	
	public void updateSign() {
		String playerName = player.getName();
		if (playerName.length() > 11) playerName = playerName.substring(0,10);
		sign.setLine(0, ChatColor.AQUA + "[" + playerName + "]");
		
		if (bets.size() > 0) {
			int counter = 1;
			for (String color : bets.keySet()) {
				if (counter > 3) return;
				sign.setLine(counter, color.replaceAll("_", "") + ": " + bets.get(color) + "c");
				counter++;
			}
			for (int i = counter; i <= 3; i++) {
				sign.setLine(i, "");
			}
			
		} else {
			sign.setLine(1, "Plaziere nun");
			sign.setLine(2, "bitte deine");
			sign.setLine(3, "3 Farbwetten");
			
		}
		sign.update();
		
	}
	
	public static void resetSign(Sign _sign) {
		_sign.setLine(0, ChatColor.WHITE + "[EggRoulette]");
		_sign.setLine(1, "Schlag hier um");
		_sign.setLine(2, "am Spiel");
		_sign.setLine(3, "teilzunehmen");
		_sign.update();
	}
	
	public void resetSign() {
		RoulettePlayer.resetSign(sign);
	}
	
	public boolean checkForWins(String winColor) {
		for (String color : bets.keySet()) {
			if (color.equals(winColor)) {
				int bet = bets.get(color);
				int win = bet * 2;
				
				if (CNEggRoulette.getEconomy().depositPlayer(player.getName(), win).transactionSuccess()) {
					player.sendMessage(ChatColor.DARK_AQUA + "Gratuliere. Dein Einsatz auf " + color + " hat dir " + win + " Cublonen eingebracht!");
					
				} else {
					player.sendMessage(ChatColor.DARK_AQUA + "Leider konnte dein Gewinn (" + win + " Cublonen) nicht gutgeschrieben werden. Mach bitte mit F2 hiervon einen Screenshot und wende dich an einen Admin!");

				}
				
				sign.setLine(0, ChatColor.GREEN + "[" + player.getName() + "]");
				sign.setLine(1, "* " + color + " *");
				sign.setLine(2, "Gewinn: " + win + "c");
				sign.setLine(3, "* Gratuliere *");
				sign.update();

				return true;
			}
		}
		
		player.sendMessage(ChatColor.DARK_AQUA + "Leider hast du diesmal kein GlŸck gehabt! Vielleicht beim nŠchsten Mal.");

		sign.setLine(0, ChatColor.RED + "[" + player.getName() + "]");
		sign.setLine(1, "Leider hast du");
		sign.setLine(2, "kein GlŸck");
		sign.setLine(3, "gehabt :-(");
		sign.update();

		return false;
	}
	
}
