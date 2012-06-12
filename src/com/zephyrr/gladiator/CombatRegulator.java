package com.zephyrr.gladiator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author Phoenix
 */
public class CombatRegulator implements Listener {

    private boolean isActive;
    private static boolean isGameTime;

    public CombatRegulator() {
        isActive = false;
        isGameTime = false;
    }

    public static void setActive(boolean val) {
        isGameTime = val;
    }

    public static boolean isActive() {
        return isGameTime;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (isActive || SpawnHandler.getSecondPlayer() == null) {
            return;
        }
        isActive = true;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (SpawnHandler.playerCount() == 1 && SpawnHandler.getFirstPlayer().isOp()) {
            return;
        }
        if (!isGameTime || SpawnHandler.playerCount() < Gladiator.getPlugin().getConfig().getInt("minPlayers") || (event.getPlayer() != SpawnHandler.getFirstPlayer() && event.getPlayer() != SpawnHandler.getSecondPlayer())) {
            if (event.getFrom().getBlock().getLocation().getBlockX() != event.getTo().getBlock().getLocation().getBlockX()
                    || event.getFrom().getBlock().getLocation().getBlockZ() != event.getTo().getBlock().getLocation().getBlockZ()) {
                event.getPlayer().teleport(event.getFrom());
            }
        }
    }
    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if(!isGameTime) {
            event.setCancelled(true);
            return;
        }
        if(!(event.getEntity() instanceof Player))
            return;
        if(event.getDamager().getType() == EntityType.ARROW) {
            Arrow a = (Arrow)event.getDamager();
            if(a.getShooter() != SpawnHandler.getFirstPlayer() && a.getShooter() != SpawnHandler.getSecondPlayer())
                event.setCancelled(true);
        }
        Player hit = (Player)event.getEntity();
        Entity hitter = event.getDamager();
        if(!(hit.equals(SpawnHandler.getFirstPlayer()) || hit.equals(SpawnHandler.getSecondPlayer())))
            event.setCancelled(true);
        if(hitter instanceof Player && !(hitter.equals(SpawnHandler.getFirstPlayer()) || hitter.equals(SpawnHandler.getSecondPlayer())))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(final PlayerDeathEvent event) {
        if (SpawnHandler.getSecondPlayer() == null) {
            if(SpawnHandler.getFirstPlayer() != null)
                SpawnHandler.getFirstPlayer().teleport(SpawnPoint.getRandomPoint());
            try {
                ArrayList<String> lines = new ArrayList<String>();
                int score = -1;
                String check;
                Scanner in = new Scanner(new File("plugins/Gladiator/records.txt"));
                while (in.hasNext()) {
                    if ((check = in.nextLine()).split(" ")[0].equals(SpawnHandler.getFirstPlayer().getDisplayName())) {
                        score = Integer.parseInt(check.split(" ")[1]);
                        lines.add(SpawnHandler.getFirstPlayer().getDisplayName() + " " + (score + 1));
                    } else {
                        lines.add(check);
                    }
                }
                if (score == -1) {
                    score = 0;
                    lines.add(SpawnHandler.getFirstPlayer().getDisplayName() + " 1");
                }
                score++;
                in.close();
                FileOutputStream fos = new FileOutputStream(new File("plugins/Gladiator/records.txt"));
                PrintStream out = new PrintStream(fos);
                for (String s : lines) {
                    out.println(s);
                }
                out.close();
                fos.close();
                event.getEntity().getServer().broadcastMessage(ChatColor.GREEN + SpawnHandler.getFirstPlayer().getDisplayName() + " has won " + score + " time(s)");
                SpawnHandler.removePlayer(SpawnHandler.getFirstPlayer());
                for (World w : event.getEntity().getServer().getWorlds()) {
                    for (Player p : w.getPlayers()) {
                        SpawnHandler.addPlayer(p);
                        p.teleport(SpawnPoint.getRandomPoint());
                    }
                }

                event.getEntity().getServer().broadcastMessage(ChatColor.GREEN + "A new event will soon begin...");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        event.getDrops().clear();
        SpawnHandler.getFirstPlayer().setHealth(SpawnHandler.getFirstPlayer().getMaxHealth());
        SpawnHandler.getFirstPlayer().setFoodLevel(20);
        SpawnHandler.getFirstPlayer().getInventory().clear();
        //event.getEntity().getKiller().teleport(SpawnPoint.getRandomPoint());
        isGameTime = false;
        event.getEntity().getServer().getScheduler().scheduleAsyncRepeatingTask(Gladiator.getPlugin(), new Runnable() {

            private int ticks = 10;

            public void run() {
                if(SpawnHandler.playerCount() < Gladiator.getPlugin().getConfig().getInt("minPlayers")) {
                    ticks = 10;
                    return;
                }
                if(ticks > 0) {
                    Gladiator.getPlugin().getServer().broadcastMessage(ChatColor.GREEN + "" + ticks + " seconds until the first match begins!");
                } else Gladiator.getPlugin().getServer().getScheduler().cancelTasks(Gladiator.getPlugin());
                if(ticks == 0) {
                    Gladiator.getPlugin().getServer().broadcastMessage(ChatColor.GREEN + SpawnHandler.getFirstPlayer().getDisplayName() + " and " + SpawnHandler.getSecondPlayer().getDisplayName() + ", begin!");
                    CombatRegulator.setActive(true);
                }
                ticks--;
            }
        }, 0L, 20L);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (SpawnHandler.getSecondPlayer() == null) {
            isActive = false;
        }
        if (SpawnHandler.getFirstPlayer() != null && SpawnPoint.getRandomPoint() != null) {
            SpawnHandler.getFirstPlayer().teleport(SpawnPoint.getRandomPoint());
        }
    }
}
