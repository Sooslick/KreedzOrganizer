package ru.sooslick.bhop;

import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class InventoryUtil {

    public static boolean invToFile(Player p) {
        //store inv contents to file
        File f = new File(Engine.INVENTORY_PATH + p.getName() + Engine.YAML_EXTENSION);
        YamlConfiguration yaml = new YamlConfiguration();
        PlayerInventory inv = p.getInventory();
        yaml.set("inventory.content", inv.getContents());
        yaml.set("inventory.armor", inv.getArmorContents());
        //todo offHand + extra slots?
        //try to save file
        try {
            yaml.save(f);
        } catch (IOException e) {
            return false;
        }
        //clear inventory and put food iin success case
        inv.clear();
        ItemStack is = new ItemStack(Material.GOLDEN_CARROT, 64);
        inv.addItem(is);
        return true;
    }

    public static boolean invFromFile(Player p) {
        File f = new File(Engine.INVENTORY_PATH + p.getName() + Engine.YAML_EXTENSION);
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.load(f);
        } catch (IOException | InvalidConfigurationException e) {
            return false;
        }
        ItemStack[] content = ((List<ItemStack>) yaml.get("inventory.content")).toArray(new ItemStack[0]);
        ItemStack[] armor = ((List<ItemStack>) yaml.get("inventory.armor")).toArray(new ItemStack[0]);
        if (!f.delete()) {
            return false;
        }
        PlayerInventory inv = p.getInventory();
        inv.clear();
        inv.setContents(content);
        inv.setArmorContents(armor);
        //todo hand + extra slots?
        return true;
    }
}
