package com.zephyrr.gladiator;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Phoenix
 */
public class Gladiator extends JavaPlugin {
    private static Gladiator plug;
    public static Gladiator getPlugin() {
        return plug;
    }
    @Override
    public void onEnable() {
        Gladiator.plug = this;
        if(!firstLoad())
            return;
        SpawnPoint.fill(getServer());
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new SpawnHandler(), this);
        getServer().getPluginManager().registerEvents(new CombatRegulator(), this);
        getServer().getPluginManager().registerEvents(new ItemRestocker(), this);
        for(World w : getServer().getWorlds())
            for(Player p : w.getPlayers())
                SpawnHandler.addPlayer(p);
            getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
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
                        Gladiator.getPlugin().getServer().broadcastMessage(ChatColor.GREEN + "Begin!");
                        CombatRegulator.setActive(true);
                    }
                    ticks--;
                }
            }, Gladiator.getPlugin().getConfig().getLong("startTime") * 20L, 20L);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("addSpawn")) {
            if(sender instanceof Player)
                SpawnPoint.addPoint(((Player)(sender)).getLocation());
            else sender.sendMessage("Only players can use this command.");
            return true;
        } else if(command.getName().equalsIgnoreCase("remSpawn")) {
            if(args.length >= 1) {
                int index;
                try {
                    index = Integer.parseInt(args[0]);
                } catch(NumberFormatException e) {
                    return false;
                }
                return SpawnPoint.removePoint(index);
            }
        } else if(command.getName().equalsIgnoreCase("listPoints")) {
            for(int i = 0; i < SpawnPoint.getFullList().size(); i++) {
                sender.sendMessage(ChatColor.GREEN + "" + i + ": " + SpawnPoint.getFullList().get(i).toString());
            }
            return true;
        } else if(command.getName().equalsIgnoreCase("getLinePlace")) {
            if(sender instanceof Player) {
                sender.sendMessage(ChatColor.GREEN + "Your space in line is #" + (SpawnHandler.findPlayer((Player)sender)+1) + " of " + SpawnHandler.playerCount() + ".");
            } else sender.sendMessage(ChatColor.RED + "You must be a player to use this comamnd.");
            return true;
        } else if(command.getName().equalsIgnoreCase("checkRecord")) {
            if(args.length == 0) {
                if(!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "You must be a player to use this command.");
                    return true;
                }
                try {
                    Scanner s = new Scanner(new File("plugins/Gladiator/records.txt"));
                    String name = ((Player)(sender)).getDisplayName();
                    int score = -1;
                    while(s.hasNext()) {
                        String check = s.nextLine();
                        if(check.split(" ")[0].equals(name)) {
                            score = Integer.parseInt(check.split(" ")[1]);
                            break;
                        }
                    }
                    s.close();
                    if(score == -1)
                        score = 0;
                    sender.sendMessage(ChatColor.GREEN + "You have won " + score + " time(s)");
                    return true;
                } catch(IOException e) {
                    e.printStackTrace();
                }
            } else {
                String name = args[0];
                try {
                    Scanner s = new Scanner(new File("plugins/Gladiator/records.txt"));
                    int score = -1;
                    while(s.hasNext()) {
                        String check = s.nextLine();
                        if(check.split(" ")[0].equals(name)) {
                            score = Integer.parseInt(check.split(" ")[1]);
                            break;
                        }
                    }
                    s.close();
                    if(score == -1)
                        score = 0;
                    sender.sendMessage(ChatColor.GREEN + name + " has won " + score + " time(s)");
                } catch(IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        } else if(command.getName().equalsIgnoreCase("setspecpoint")) {
            if(sender instanceof Player) {
                Location loc = ((Player)sender).getLocation();
                getConfig().set("respawnLocation"
                       , loc.getWorld().getName() +
                         loc.getX() + " " +
                         loc.getY() + " " + 
                         loc.getZ());
                saveConfig();
                return true;
            }
        } else if(command.getName().equalsIgnoreCase("getspecpoint")) {
            sender.sendMessage(getConfig().getString("respawnLocation"));
            return true;
        }
        return false;
    }

    private boolean firstLoad() {
        try {
            File s = new File("plugins/Gladiator");
            if(!s.exists())
                s.mkdir();
            File out = new File("plugins/Gladiator/spawnPoints.txt");
            if(!out.exists())
                out.createNewFile();
            out = new File("plugins/Gladiator/records.txt");
            if(!out.exists())
                out.createNewFile();
            if(!new File("plugins/Gladiator/config.yml").exists())
                saveDefaultConfig();
        } catch(IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void onDisable() {
        SpawnPoint.writeOut();
    }
}
