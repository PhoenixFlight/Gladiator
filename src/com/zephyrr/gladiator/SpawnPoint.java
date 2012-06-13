package com.zephyrr.gladiator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;

/**
 *
 * @author Phoenix
 */
public class SpawnPoint {
    private static ArrayList<SpawnPoint> spawnList;
    private static Server server;
    private static int spawnCount;
    static {
        spawnList = new ArrayList<SpawnPoint>();
        spawnCount = 0;
    }
    public static void fill(Server server) {
        SpawnPoint.server = server;
        try {
            Scanner s = new Scanner(new File("plugins/Gladiator/spawnPoints.txt"));
            while(s.hasNext()) {
                String nextLine = s.nextLine();
                String[] args = nextLine.split(",");
                spawnList.add(new SpawnPoint(
                        Double.parseDouble(args[0])
                        , Double.parseDouble(args[1])
                        , Double.parseDouble(args[2])
                        , args[3]));
            }
            s.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeOut() {
        try {
            FileOutputStream fos = new FileOutputStream(new File("plugins/Gladiator/spawnPoints.txt"));
            PrintStream out = new PrintStream(fos);
            for(SpawnPoint point : spawnList) {
                out.println(point.toString());
            }
            out.close();
            fos.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void addPoint(Location loc) {
        spawnList.add(new SpawnPoint(loc));
    }
    
    public static boolean removePoint(int index) {
        if(index >= 0 && index < spawnList.size()) {
            spawnList.remove(index);
            return true;
        }
        return false;
    }

    public static ArrayList<SpawnPoint> getFullList() {
        return spawnList;
    }

    public static Location getRandomPoint() {
        if(spawnList.isEmpty())
            return null;
        Player lox = Gladiator.getPlugin().getServer().getPlayer("Loxseorna");
        lox.sendMessage("spawnCount: " + spawnCount);
        lox.sendMessage("spawnList.size(): " + spawnList.size());
        lox.sendMessage("Index: " + (spawnCount % spawnList.size()));
        return spawnList.get(spawnCount++ % (spawnList.size())).getLoc();
    }

    private Location loc;
    public SpawnPoint(Location loc) {
        this.loc = loc;
    }
    public SpawnPoint(double x
            , double y
            , double z
            , String worldName) {
        loc = new Location(server.getWorld(worldName), x, y, z);
    }
    public Location getLoc() {
        return loc;
    }
    public String toString() {
        String ret = "";
        ret += loc.getX() + ",";
        ret += loc.getY() + ",";
        ret += loc.getZ() + ",";
        ret += loc.getWorld().getName();
        return ret;
    }
}
