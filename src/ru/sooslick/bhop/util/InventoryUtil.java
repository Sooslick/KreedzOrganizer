package ru.sooslick.bhop.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import ru.sooslick.bhop.Engine;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class InventoryUtil {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmssSSS");

    public static boolean invToFile(Player p) {
        Bukkit.getLogger().info("saving inventory of player " + p.getName());
        //store inv contents to file
        File f = new File(Engine.INVENTORY_PATH + p.getName() + Engine.YAML_EXTENSION);
        File backup = new File(Engine.INVENTORY_BACKUP_PATH + sdf.format(new Date()) + p.getName() + Engine.YAML_EXTENSION);
        YamlConfiguration yaml = new YamlConfiguration();
        PlayerInventory inv = p.getInventory();
        yaml.set("inventory.content", inv.getContents());
        //try to save file
        try {
            yaml.save(f);
            yaml.save(backup);
        } catch (IOException e) {
            f.delete();
            return false;
        }
        //clear inventory and put food iin success case
        inv.clear();
        ItemStack is = new ItemStack(Material.GOLDEN_CARROT, 4);
        inv.addItem(is);
        return true;
    }

    public static boolean invFromFile(Player p) {
        Bukkit.getLogger().info("loading inventory of player " + p.getName());
        File f = new File(Engine.INVENTORY_PATH + p.getName() + Engine.YAML_EXTENSION);
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.load(f);
        } catch (IOException | InvalidConfigurationException e) {
            return false;
        }
        ItemStack[] content = ((List<ItemStack>) yaml.get("inventory.content")).toArray(new ItemStack[0]);
        if (!f.delete()) {
            return false;
        }
        PlayerInventory inv = p.getInventory();
        inv.clear();
        inv.setContents(content);
        return true;
    }
}
