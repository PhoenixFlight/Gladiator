package com.zephyrr.gladiator;

import java.util.ArrayList;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 *
 * @author Phoenix
 */
public class SpawnHandler implements Listener {

    private static ArrayList<Player> playerOrder;

    static {
        playerOrder = new ArrayList<Player>();
    }

    public static Player getFirstPlayer() {
        if (playerOrder.size() >= 1) {
            return playerOrder.get(0);
        }
        return null;
    }

    public static Player getSecondPlayer() {
        if (playerOrder.size() >= 2) {
            return playerOrder.get(1);
        }
        return null;
    }

    public static int findPlayer(Player p) {
        return playerOrder.indexOf(p);
    }

    public static int playerCount() {
        return playerOrder.size();
    }

    public static void addPlayer(Player p) {
        playerOrder.add(p);
    }

    public static void removePlayer(Player p) {
        playerOrder.remove(p);
    }

    @EventHandler
    public void onPlayerLogin(final PlayerLoginEvent event) {
        playerOrder.add(event.getPlayer());
        if (SpawnPoint.getRandomPoint() != null) {
            event.getPlayer().getServer().getScheduler().scheduleAsyncDelayedTask(Gladiator.getPlugin(), new Runnable() {

                public void run() {
                    event.getPlayer().teleport(SpawnPoint.getRandomPoint());
                }
            }, 100L);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerOrder.remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Location loc;
        String data = Gladiator.getPlugin().getConfig().getString("respawnLocation");
        if (data.equalsIgnoreCase("rien")) {
            loc = SpawnPoint.getRandomPoint();
        } else {
            loc = new Location(event.getEntity().getServer().getWorld(data.split(",")[0]), Double.parseDouble(data.split(",")[1]), Double.parseDouble(data.split(",")[2]), Double.parseDouble(data.split(",")[3]));
        }
        event.getEntity().teleport(loc);
        playerOrder.remove(event.getEntity());
        //playerOrder.add(event.getEntity());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Location loc;
        String data = event.getPlayer().getServer().getPluginManager().getPlugin("Gladiator").getConfig().getString("respawnLocation");
        if (data.equalsIgnoreCase("NULL")) {
            loc = SpawnPoint.getRandomPoint();
        } else {
            loc = new Location(event.getPlayer().getServer().getWorld(data.split(",")[0]), Double.parseDouble(data.split(",")[1]), Double.parseDouble(data.split(",")[2]), Double.parseDouble(data.split(",")[3]));
        }
        event.setRespawnLocation(loc);
    }
}
