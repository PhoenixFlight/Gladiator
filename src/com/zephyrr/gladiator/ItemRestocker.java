package com.zephyrr.gladiator;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Phoenix
 */
public class ItemRestocker implements Listener {
    private static ArrayList<ItemStack> items;
    static {
        items = new ArrayList<ItemStack>();
        fill();
    }
    public static ArrayList<ItemStack> getItems() {
        return items;
    }
    private static void fill() {
        try {
            if(!new File("plugins/Gladiator/itemList.txt").exists())
                new File("plugins/Gladiator/itemList.txt").createNewFile();
            Scanner s = new Scanner(new File("plugins/Gladiator/itemList.txt"));
            while(s.hasNext()) {
                String[] args = s.nextLine().split(",");
                int type = Integer.parseInt(args[0]);
                int count = Integer.parseInt(args[1]);
                items.add(new ItemStack(type, count));
            }
            s.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void initialStock(PlayerLoginEvent event) {
        event.getPlayer().getInventory().clear();
        for(int i = 0; i < items.size(); i++)
            event.getPlayer().getInventory().addItem(items.get(i).clone());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void restockItems(final PlayerRespawnEvent event) {
        //if(SpawnHandler.getFirstPlayer() != null)
            //SpawnHandler.getFirstPlayer().getInventory().clear();
        //event.getPlayer().getKiller().getInventory().clear();
        //for(int i = 0; i < items.size(); i++)
            //event.getPlayer().getKiller().getInventory().addItem(items.get(i).clone());
        for(Player p : event.getPlayer().getWorld().getPlayers()) {
            p.getInventory().clear();
            for(ItemStack s : ItemRestocker.getItems())
                p.getInventory().addItem(s.clone());
            p.setHealth(p.getMaxHealth());
            p.setFoodLevel(20);
        }
        event.getPlayer().getServer().getScheduler().scheduleAsyncDelayedTask(Gladiator.getPlugin(), new Runnable() {
            public void run() {
                event.getPlayer().getInventory().clear();
                for(ItemStack s : items)
                    event.getPlayer().getInventory().addItem(s.clone());

                    }
        }, 100L);
    }
}
