package me.derflash.plugins.eggroulette;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Wool;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class CNEggRoulette extends JavaPlugin implements Listener {

	public YamlConfiguration settings = null;
	File settingsFile = null;
	
	int setupState;
	Player setupPlayer;
	
	int respawnTask = -1;
	
	boolean winDone;
	boolean joinActive;
	boolean signSetupActive;
	World betWorld;
	CNEggRoulette plugin;
	
	HashSet<LivingEntity> chicken = new HashSet<LivingEntity>();
	Entity lastEgg;

    private static Economy economy = null;

	HashMap<Player, RoulettePlayer> roulettePlayers = new HashMap<Player, RoulettePlayer>();
	
	public void onDisable() {
		resetGame();
		
		if (saveSettings()) settings = null;
        System.out.println(this + " is now disabled!");
    }

    public void onEnable() {
    	this.plugin = this;
    	
        if (!setupEconomy()) {
        	this.setEnabled(false);
            System.out.println(this + " could not find Vault plugin! Disabling...");
            return;
        }

		File dFolder = getDataFolder();
		if(!dFolder.exists()) dFolder.mkdirs();
		
		settingsFile = new File(dFolder, "config.yml");
        if (settingsFile.exists()) settings = YamlConfiguration.loadConfiguration(settingsFile);
        else {
        	settings = new YamlConfiguration();
        }
        
	    String _betWorld = settings.getString("betWorld");
	    if (_betWorld != null) betWorld = Bukkit.getWorld(_betWorld);
    	
        getServer().getPluginManager().registerEvents(this, this);
        
        System.out.println(this + " is now enabled!");
        
    }
    
    private Boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            setEconomy(economyProvider.getProvider());
        }

        return (getEconomy() != null);
    }

    
    public boolean saveSettings() {
		if (!settingsFile.exists()) {
			settingsFile.getParentFile().mkdirs();
		}
		
		try {
			settings.save(settingsFile);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
    
    public void resetGame() {
		Bukkit.getScheduler().cancelAllTasks();
		
    	setupState = 0;
		setupPlayer = null;
		winDone = false;
		
		
		if (lastEgg != null) {
			lastEgg.remove();
			lastEgg = null;
		}
		
		if (!chicken.isEmpty()) {
			for (LivingEntity _chicken : chicken) {
				_chicken.remove();
			}
			chicken.clear();
		}
		
		for (RoulettePlayer rPlayer : roulettePlayers.values()) {
			rPlayer.resetSign();
		}
		roulettePlayers.clear();
    }
    

    
    Player winTap;
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	// player is the warned player; player1 is the sender
    	// permissions cubewarn.staff & cubewarn.admin
    	Player player = (Player) sender;
    	if (label.equalsIgnoreCase("egg")) {
    		
    		if (player.hasPermission("eggroulette.admin")) {
    			
        		if(args.length > 0 && args[0].equalsIgnoreCase("setup") ) {
        			player.sendMessage(ChatColor.DARK_AQUA + "Setup aktiviert. Schlage nun bitte auf die Wollfarben, welche gesetzt werden kšnnen. Zum Abschliessen, schlage irgendwo anders hin.");
        			setupState = 1;
        			setupPlayer = player;
            	    settings.set("colorButtonLoc", null);
            	    settings.set("chickenSpawnLoc", null);
            	    settings.set("chickenSpawnWorld", null);
            	    
        		} else if(args.length > 0 && args[0].equalsIgnoreCase("win") ) {
        			player.sendMessage(ChatColor.DARK_AQUA + "Schlag nun auf die Farbe, die gewonnen hat!");
        			winTap = player;

        		} else if(args.length > 0 && args[0].equalsIgnoreCase("reset") ) {
        			resetGame();
        			player.sendMessage(ChatColor.DARK_AQUA + "Das Spiel wurde zurŸckgesetzt.");

        		} else if(args.length > 0 && args[0].equalsIgnoreCase("go") ) {
        			if (!joinActive) {
            			player.sendMessage(ChatColor.DARK_AQUA + "Es ist kein Spiel aktiv, welches du nun auflšsen kšnntest!");
            	    	return true;
        			}
        			
            	    if (settings.get("chickenSpawnLoc") == null || settings.get("chickenSpawnWorld") == null) {
            			player.sendMessage(ChatColor.DARK_AQUA + "Die Spawnlocation fŸr das Huhn wurde noch nicht korrekt gesetzt!");
            	    	return true;
            	    }
        			
    				localBroadcast("Letzte Chance! Ihr habt noch 10 Sekunden Zeit, eure Gebote zu Šndern.");
    				
    				Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable () {
    					public void run (){
    	        			joinActive = false;
    	        			
    	    				localBroadcast("Rien ne va plus - nichts geht mehr! Mšge das Huhn entscheiden.");
    	    				
    	    				World _spawnWorld = Bukkit.getWorld(settings.getString("chickenSpawnWorld"));
    	    				Vector _spawnVec = settings.getVector("chickenSpawnLoc");
    	    				
    	    				LivingEntity _newChicken = _spawnWorld.spawnCreature(_spawnVec.toLocation(_spawnWorld), CreatureType.CHICKEN);
    	    				chicken.add(_newChicken);
    	    				
    	    				startMoreChickenTask();
    	    				
    					}
    				}, 20 * 10);

        		} else if(args.length > 0 && (args[0].equalsIgnoreCase("restart") || args[0].equalsIgnoreCase("start")) ) {
        			resetGame();
        			getServer().broadcastMessage(ChatColor.AQUA + "[EggRoulette] Auf zu einer neuen Runde! WŠhlt euer Schild und gebt eure Gebote ab.");
    				joinActive = true;

        		} else if(args.length > 1 && args[0].equalsIgnoreCase("setRespawn") ) {
        			int rs = Integer.parseInt(args[1]);        			
            	    settings.set("nextChicken", rs);
            	    saveSettings();
        			player.sendMessage(ChatColor.DARK_AQUA + "Respawnzeit fŸrs zweite Huhn gesetzt auf: " + rs);
    				
        		} else if(args.length > 1 && args[0].equalsIgnoreCase("setBet") ) {
        			int worth = Integer.parseInt(args[1]);        			
            	    settings.set("betWorth", worth);
            	    saveSettings();
        			player.sendMessage(ChatColor.DARK_AQUA + "Gebote gesetzt auf: " + worth);

        		} else if(args.length > 0 && args[0].equalsIgnoreCase("setWorld") ) {
        			if (args.length == 1) {
            			player.sendMessage(ChatColor.DARK_AQUA + "Spielwelt gesetzt auf: alle");
                	    settings.set("betWorld", null);
                	    betWorld = null;

        			} else {
                	    betWorld = Bukkit.getWorld(args[1]);
                	    if (betWorld == null) {
                			player.sendMessage(ChatColor.DARK_AQUA + "Diese Welt existiert nicht. Spielwelt gesetzt auf: alle");

                	    } else {
                			player.sendMessage(ChatColor.DARK_AQUA + "Spielwelt gesetzt auf: " + args[1]);
                    	    settings.set("betWorld", args[1]);
                	    	
                	    }
        			}
        			
        		} else if(args.length > 1 && args[0].equalsIgnoreCase("setMax") ) {
        			int worth = Integer.parseInt(args[1]);        			
            	    settings.set("betMax", worth);
            	    saveSettings();
        			player.sendMessage(ChatColor.DARK_AQUA + "Maximalgebot gesetzt auf: " + worth);

        		} else if(args.length > 0 && args[0].equalsIgnoreCase("stop") ) {
        			player.sendMessage(ChatColor.DARK_AQUA + "Das Spiel wird nach der aktuellen Runde angehalten. Gib '/" + label + " restart' ein, um es wieder zu starten.");
        			joinActive = false;

        		} else if(args.length > 0 && args[0].equalsIgnoreCase("sign") ) {
        			signSetupActive = !signSetupActive;
        			if (signSetupActive) {
            			player.sendMessage(ChatColor.DARK_AQUA + "Schild-Erstell-Modus aktiviert. Klicke nun auf Blockseiten, wo du ein Schild erstellen mšchtest. Klicke dann irgendwo auf den Boden oder gib noch einmal '/" + label + " " + args[0] + "' ein.");
            			setupPlayer = player;
            			
        			} else {
            			player.sendMessage(ChatColor.DARK_AQUA + "Schild-Erstell-Modus deaktiviert.");
            			setupPlayer = null;
        			}
        			
        		}
    			
    		}
    		
    	}
		return true;
    }
    
    protected void cancelMoreChickenTask() {
	    if (respawnTask != -1) Bukkit.getScheduler().cancelTask(respawnTask);
    }
    
    protected void startMoreChickenTask() {
	    int seconds = settings.getInt("nextChicken");
	    if (seconds == 0) seconds = 120;
	    
	    cancelMoreChickenTask();
	    respawnTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable () {
			public void run (){    	    						
				localBroadcast("Unsere HŸhner scheinen heut Verstopfung zu haben. Wir schicken zur Sicherheit noch eins mehr ins Rennen!");
				
				World _spawnWorld = Bukkit.getWorld(settings.getString("chickenSpawnWorld"));
				Vector _spawnVec = settings.getVector("chickenSpawnLoc");
				
				LivingEntity _newChicken = _spawnWorld.spawnCreature(_spawnVec.toLocation(_spawnWorld), CreatureType.CHICKEN);
				chicken.add(_newChicken);
			}
		}, 20 * seconds, 20 * seconds);		
	}

	public Wool getWoolForBlock(Block block) {
		if (block.getType() == Material.WOOL) {
        	MaterialData data = block.getType().getNewData(block.getData());
        	if (data instanceof Wool) {
        	    return (Wool)data;
        	}
		}
		return null;
    }
    

    @EventHandler
    public void onItemSpawn (final ItemSpawnEvent event) {
		Entity egg = event.getEntity();
		
		if (!chicken.isEmpty()) {
			for (LivingEntity _chicken : chicken) {
				if (egg.getWorld() == _chicken.getWorld() && _chicken.getLocation().distance(egg.getLocation()) < 1) {
					if (lastEgg != null) {
						event.setCancelled(true);
						return;
					}
				    cancelMoreChickenTask();
					
					Block blockBelow = null;
		    		int down = -1;
		    		while (blockBelow == null) {
		    			if (down < -10) {
		        			System.out.println("[EggRoulette] Fehler: ei zu tief gefallen?!");
		        			down = 0;
		        			break;
		    			}
		        		blockBelow = egg.getWorld().getBlockAt(egg.getLocation().add(0, down, 0));
		        		if (blockBelow != null && blockBelow.getType() == Material.AIR) blockBelow = null;
		    			down--;
		    		}
		    		
		    		boolean failed = true;
		    		if (down < 0 && blockBelow.getType() == Material.WOOL) {
		            	MaterialData data = blockBelow.getType().getNewData(blockBelow.getData());
		            	if (data instanceof Wool) {
		            	    Wool wool = (Wool)data;
		            	    String woolColor = wool.getColor().toString();
		            		
		            		if (settings.getConfigurationSection("colorButtonLoc").getKeys(false).contains(woolColor)) {
		            			checkWins(woolColor);
		            			lastEgg = egg;
		            			failed = false;
		            		}
		            	}
		    		}

		    		if (failed) {
						localBroadcast("Uhhh... Das ging voll daneben! Weiter gehts...");

						Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable () {
	    					public void run (){
	    						event.getEntity().remove();
	    					}
	    				}, 20 * 10);
						
						if (chicken.size() < 2) {
			    			startMoreChickenTask();
						}
		    			
		    		}
				}
			}
    	}
    }
    
    @EventHandler
	public void onPlayerInteract (PlayerInteractEvent event) {
		Block block = event.getClickedBlock();
		Player player = event.getPlayer();
		
		if (player == null || block == null || block.getType() == null || !(player instanceof Player)) return;
		
		if (betWorld != null && player.getWorld() != betWorld) return;
		
		if (signSetupActive && setupPlayer == player) {
			// Command: /egg sign
			
			if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {

				if (event.getBlockFace() == BlockFace.NORTH || event.getBlockFace() == BlockFace.WEST || event.getBlockFace() == BlockFace.SOUTH || event.getBlockFace() == BlockFace.EAST) {
					Block signBlock = block.getRelative(event.getBlockFace());
					if (signBlock.getType() == Material.AIR) {
						signBlock.setType(Material.WALL_SIGN);
						
						BlockState _sign = signBlock.getState();
						((org.bukkit.material.Sign)_sign.getData()).setFacingDirection(event.getBlockFace());

				        if (_sign instanceof Sign) {
				        	Sign sign = (Sign)_sign;
				    		RoulettePlayer.resetSign(sign);
				        }

					}
				} else {
					signSetupActive = false;
        			setupPlayer = null;
        			player.sendMessage(ChatColor.DARK_AQUA + "Schild-Erstell-Modus deaktiviert.");
				}
			}
			event.setCancelled(true);
			
			
		} else if (setupState > 0 && setupPlayer == player) {
			// Command: /egg setup
			
			if (setupState == 1) {
				Wool wool = getWoolForBlock(block);
				if (wool != null) {
					Vector blockLoc = block.getLocation().toVector();
		    	    settings.set("colorButtonLoc."+wool.getColor(), blockLoc);
		    	    player.sendMessage(ChatColor.DARK_AQUA + "Buttonblock fŸr " + wool.getColor() + " gesetzt.");
				
				} else {
	    	    	player.sendMessage(ChatColor.DARK_AQUA + "Farbsetup abgeschlossen. Stell dich nun an die Stelle, an der das Huhn spawnen soll und schlage irgendwo hin.");
	    	    	setupState = 2;
				}
				
			} else if (setupState == 2) {
				Location chickenSpawn = player.getLocation();
        	    settings.set("chickenSpawnLoc", chickenSpawn.toVector());
        	    settings.set("chickenSpawnWorld", chickenSpawn.getWorld().getName());
        	    saveSettings();
        	    
    	    	player.sendMessage(ChatColor.DARK_AQUA + "Setup abgeschlossen. Viel Spass!");
    	    	setupState = 0;
    			setupPlayer = null;

			}
			event.setCancelled(true);
			
			
		} else if (winTap != null && winTap == player) {
			// Command: /egg win
			winTap = null;

			Wool wool = getWoolForBlock(block);
			if (wool != null) {
				String woolColor = wool.getColor().toString();
				checkWins(woolColor);
				
			} else {
				player.sendMessage(ChatColor.DARK_AQUA + "Das war keine Wolle. Abgebrochen...");

			}
			event.setCancelled(true);
		
		} else {
			// user stuff
			
			Wool wool = getWoolForBlock(block);
			if (wool != null) {
				
				RoulettePlayer rPlayer = roulettePlayers.get(player);
				if (rPlayer == null) return;
				
	    	    Vector woolLoc = settings.getVector("colorButtonLoc."+wool.getColor());
	    	    Vector touchedLoc = block.getLocation().toVector();
	    	    
				if (woolLoc != null && touchedLoc.getBlockX() == woolLoc.getBlockX() && touchedLoc.getBlockY() == woolLoc.getBlockY() && touchedLoc.getBlockZ() == woolLoc.getBlockZ()) {					
					
					if (winDone) {
	                	player.sendMessage(ChatColor.DARK_AQUA + "Das Spiel ist bereits entschieden. Warte auf die nŠchste  Runde.");
	                	
					} else if (!chicken.isEmpty()) {
	                	player.sendMessage(ChatColor.DARK_AQUA + "Das Huhn ist bereits unterwegs. Warte auf die nŠchste Runde.");

					} else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
						rPlayer.addBet(wool.getColor().toString());
						rPlayer.updateSign();

					} else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
						rPlayer.removeBet(wool.getColor().toString());
						rPlayer.updateSign();
						
					}
					if (!player.isSneaking()) event.setCancelled(true);
					
				}
				
			} else if (block.getType() == Material.WALL_SIGN) {
	            Sign sign = (Sign) block.getState();
	            if (sign.getLine(0).equalsIgnoreCase(ChatColor.WHITE + "[EggRoulette]")) {
	            	if (winDone) {
	                	player.sendMessage(ChatColor.DARK_AQUA + "Das Spiel ist bereits entschieden. Warte auf die nŠchste  Runde.");

					} else if (!chicken.isEmpty()) {
	                	player.sendMessage(ChatColor.DARK_AQUA + "Das Huhn ist bereits unterwegs. Warte auf die nŠchste Runde.");

					} else if (!joinActive) {
	                	player.sendMessage(ChatColor.DARK_AQUA + "Das Spiel ist derzeit deaktiviert.");

	            	} else if (!roulettePlayers.containsKey(player)) {
	                	RoulettePlayer rPlayer = new RoulettePlayer(player, sign, this);
	                	roulettePlayers.put(player, rPlayer);
	                	rPlayer.updateSign();
	                	
	                	player.sendMessage(ChatColor.DARK_AQUA + "Willkommen bei EggRoulette. Plaziere nun bis zu " + getMax() + " Cublonen auf maximal 3 verschiedene Farben.");
	                	
	            	} else {
	                	player.sendMessage(ChatColor.DARK_AQUA + "Du spielst doch schon mit!");
	            		
	            	}
	            	
	            	if (!player.isSneaking()) event.setCancelled(true);
	            }
			}
						
		}		
		
	}
    
    private void checkWins(String woolColor) {
		winDone = true;
		
    	String gewinner = "";
		for (RoulettePlayer rPlayer : roulettePlayers.values()) {
			if (rPlayer.checkForWins(woolColor)) {
				if (gewinner.length() == 0) {
					gewinner = rPlayer.getPlayer().getName();
				} else {
					gewinner += ", " + rPlayer.getPlayer().getName();
				}
			}
		}
		
		if (gewinner.length() > 0) {
			if (gewinner.indexOf(",") == -1) {
				localBroadcast("Das Huhn hat's entschieden und auf " + woolColor + " gelegt. Der Gewinner ist: " + gewinner);
				
			} else {
				localBroadcast("Das Huhn hat's entschieden und auf " + woolColor + " gelegt. Die Gewinner sind: " + gewinner);
				
			}
			
		} else {
			localBroadcast("Das Huhn hat's entschieden und auf " + woolColor + " gelegt. Leider gab es diesmal keine Gewinner :-(");

		}		
	}

	public int getMax() {
    	int _max = settings.getInt("betMax");
    	if (_max == 0) return 100;
    	else return _max;
    }
    
    public int getBet() {
    	int _worth = settings.getInt("betWorth");
    	if (_worth == 0) return 5;
    	else return _worth;
    }

	public static Economy getEconomy() {
		return economy;
	}

	public static void setEconomy(Economy economy) {
		CNEggRoulette.economy = economy;
	}
	
	public void localBroadcast(String msg) {
		if (betWorld != null) {
			List<Player> players = betWorld.getPlayers();
			for (Player player : players) {
				player.sendMessage(ChatColor.AQUA + "[EggRoulette] " + msg);
			}
			
		} else {
			Player[] players = getServer().getOnlinePlayers();
			for (Player player : players) {
				player.sendMessage(ChatColor.AQUA + "[EggRoulette] " + msg);
			}
			
		}
	}
}

