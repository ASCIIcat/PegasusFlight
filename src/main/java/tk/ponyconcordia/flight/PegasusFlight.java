package tk.ponyconcordia.flight;

import java.io.File;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class PegasusFlight extends JavaPlugin
  implements Listener
{
  private double decayRate = 1.0D;
  private int minFoodForFly = 3;
  private HashMap<Player, Double> degrade = new HashMap<Player, Double>();
  private HashMap<String, Integer> foodInFlight = new HashMap<String, Integer>();

  public void onEnable() {
    getLogger().info("PegasusFlight plugin enabled.");
    
    //Generate Config
    if (!new File(getDataFolder(), "config.yml").exists()) { //If the config dont exists...
        getLogger().info("Generating Default Config"); //Say this.
        saveDefaultConfig(); //Generate Config...
        getLogger().info("Default Config Generated"); //Say This...
        }

    getServer().getPluginManager().registerEvents(this, this);

    reloadConfig();
    activeTickTimer();

    getConfig().options().copyDefaults(true);
    saveConfig();
  }

  private boolean isFlat(Vector vec) {
    double y = vec.getY();
    return y > -0.1D;
  }

  public void onDisable() {
    getLogger().info("PegasusFlight plugin disabled.");
  }

  public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
    String commandString = cmd.getName();
    if (commandString.equalsIgnoreCase("pegaflightreload")) {
      sender.sendMessage("Reloading PegasusFlight configs.");
      reloadConfig();
      return true;
    }
    if (commandString.equalsIgnoreCase("pegaflightdecay")) {
      double newDecay = 0.0D;
      try {
        newDecay = Double.parseDouble(args[0]);
      } catch (Exception e) {
        sender.sendMessage(args[0] + " cannot be parsed as a number.");
        return false;
      }
      getConfig().set("HungerDecayRate", Double.valueOf(newDecay));
      saveConfig();
      reloadConfig();
      sender.sendMessage("Setting hunger decay rate to " + newDecay + " per second.");
      return true;
    }
    if (commandString.equalsIgnoreCase("pegaflighfoodmin")) {
      int newMin = 0;
      try {
        newMin = Integer.parseInt(args[0]);
      } catch (Exception e) {
        sender.sendMessage(args[0] + " cannot be parsed as a number.");
        return false;
      }
      getConfig().set("MinFoodForFly", Integer.valueOf(newMin));
      saveConfig();
      reloadConfig();
      sender.sendMessage("Setting minimum food for flying to " + newMin + ".");
      return true;
    }
    if (commandString.equalsIgnoreCase("pegaflightEatingMax")) {
      int newMin = 0;
      try {
        newMin = Integer.parseInt(args[0]);
      } catch (Exception e) {
        sender.sendMessage(args[0] + " cannot be parsed as a number.");
        return false;
      }
      getConfig().set("EatingPerFlight", Integer.valueOf(newMin));
      saveConfig();
      reloadConfig();
      sender.sendMessage("Setting maximium allowed eating sessions per flight to " + newMin + ".");
      return true;
    }
    return false;
  }

  public void reloadConfig() {
    super.reloadConfig();
    this.decayRate = getConfig().getDouble("HungerDecayRate");
    this.minFoodForFly = getConfig().getInt("MinFoodForFly");
  }

  private void activeTickTimer()
  {
    getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
      public void run() {
    	  PegasusFlight.this.onTick();
    	  PegasusFlight.this.activeTickTimer();
      }
    }
    , 1L);
  }
  
  private boolean hasLanded()
  {
	  for (Player player : getServer().getOnlinePlayers()) {
		  if ((player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR) || 
		            (player.getLocation().getBlock().getRelative(BlockFace.EAST).getType() != Material.AIR) || 
		            (player.getLocation().getBlock().getRelative(BlockFace.EAST_NORTH_EAST).getType() != Material.AIR) || 
		            (player.getLocation().getBlock().getRelative(BlockFace.EAST_SOUTH_EAST).getType() != Material.AIR) || 
		            (player.getLocation().getBlock().getRelative(BlockFace.NORTH).getType() != Material.AIR) || 
		            (player.getLocation().getBlock().getRelative(BlockFace.NORTH_EAST).getType() != Material.AIR) || 
		            (player.getLocation().getBlock().getRelative(BlockFace.NORTH_NORTH_EAST).getType() != Material.AIR) || 
		            (player.getLocation().getBlock().getRelative(BlockFace.NORTH_NORTH_WEST).getType() != Material.AIR) || 
		            (player.getLocation().getBlock().getRelative(BlockFace.NORTH_WEST).getType() != Material.AIR) || 
		            (player.getLocation().getBlock().getRelative(BlockFace.SOUTH).getType() != Material.AIR) || 
		            (player.getLocation().getBlock().getRelative(BlockFace.SOUTH_EAST).getType() != Material.AIR) || 
		            (player.getLocation().getBlock().getRelative(BlockFace.SOUTH_SOUTH_EAST).getType() != Material.AIR) || 
		            (player.getLocation().getBlock().getRelative(BlockFace.SOUTH_SOUTH_WEST).getType() != Material.AIR) || 
		            (player.getLocation().getBlock().getRelative(BlockFace.SOUTH_WEST).getType() != Material.AIR) || 
		            (player.getLocation().getBlock().getRelative(BlockFace.WEST).getType() != Material.AIR) || 
		            (player.getLocation().getBlock().getRelative(BlockFace.WEST_NORTH_WEST).getType() != Material.AIR) || 
		            (player.getLocation().getBlock().getRelative(BlockFace.WEST_SOUTH_WEST).getType() != Material.AIR) || 
		            (player.getLocation().getBlock().getRelative(BlockFace.SELF).getType() != Material.AIR))
		          {
		            return true;
		          }  
	  	}
	  return false;
  
  }

  private void onTick()
  {
    for (Player player : getServer().getOnlinePlayers()) {
      String playerName = player.getName();
      if ((!player.isFlying()) && (isFlat(player.getVelocity())) && hasLanded()) {
        this.foodInFlight.remove(playerName);
        player.setAllowFlight(false);
        player.setFlying(false);
      }
      
      

      if(Bukkit.getServer().getPluginManager().getPlugin("AntiCheat") != null)
      {
	      if ((player.getGameMode() != GameMode.CREATIVE) && (player.getFoodLevel() < this.minFoodForFly)) 
	      {
	        this.degrade.remove(player);
	        player.setAllowFlight(false);
	        player.setFlying(false);
	      } 
	      else if ((player.hasPermission("pegasusflight.fly")) || (player.getGameMode() == GameMode.CREATIVE)) 
	      {
	        player.setAllowFlight(true);
	        
	        double hunger = 0.0D;
	        if (this.degrade.containsKey(player)) {
	          hunger = ((Double)this.degrade.get(player)).doubleValue();
	          this.degrade.remove(player);
	        }
	        hunger += this.decayRate / 20.0D;
	        if (hunger > 1.0D) {
	          player.setFoodLevel((int)(player.getFoodLevel() - hunger));
	          hunger %= 1.0D;
	        }
	        this.degrade.put(player, Double.valueOf(hunger));
	      }
	
	      /*
	      if ((player.getGameMode() != GameMode.CREATIVE) && (player.isFlying())) {
	        double hunger = 0.0D;
	        if (this.degrade.containsKey(player)) {
	          hunger = ((Double)this.degrade.get(player)).doubleValue();
	          this.degrade.remove(player);
	        }
	        hunger += this.decayRate / 20.0D;
	        if (hunger > 1.0D) {
	          player.setFoodLevel((int)(player.getFoodLevel() - hunger));
	          hunger %= 1.0D;
	        }
	        this.degrade.put(player, Double.valueOf(hunger));
	      }
	      */
      }
    }
  }

  private void handleLeave(Player player) {
    this.degrade.remove(player);
    String playerName = player.getName();
    if ((this.foodInFlight.containsKey(playerName)) && (((Integer)this.foodInFlight.get(playerName)).intValue() == 0))
      this.foodInFlight.remove(playerName);
  }

  @EventHandler
  public void onPlayerLogout(PlayerQuitEvent event)
  {
    handleLeave(event.getPlayer());
  }
  @EventHandler
  public void onPlayerKick(PlayerKickEvent event) {
    handleLeave(event.getPlayer());
  }
}