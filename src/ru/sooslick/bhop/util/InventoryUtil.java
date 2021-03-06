package ru.sooslick.bhop.util;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import ru.sooslick.bhop.Engine;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class InventoryUtil {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmssSSS");
    private static final Random random = new Random();

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean invToFile(Player p) {
        Engine.LOG.info("saving inventory of player " + p.getName());
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
            //noinspection ResultOfMethodCallIgnored
            f.delete();
            return false;
        }
        //clear inventory and put food iin success case
        inv.clear();
        ItemStack is = new ItemStack(Material.GOLDEN_CARROT, 4);
        inv.addItem(is);
        is = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta am = (LeatherArmorMeta) is.getItemMeta();
        if (am != null)
            am.setColor(Color.fromRGB(random.nextInt(16777216)));
        is.setItemMeta(am);
        inv.setBoots(is);
        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean invFromFile(Player p) {
        Engine.LOG.info("loading inventory of player " + p.getName());
        File f = new File(Engine.INVENTORY_PATH + p.getName() + Engine.YAML_EXTENSION);
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.load(f);
        } catch (IOException | InvalidConfigurationException e) {
            return false;
        }
        ItemStack[] content;
        try {
            content = ((List<ItemStack>) yaml.get("inventory.content")).toArray(new ItemStack[0]);
        } catch (Exception e) {
            Engine.LOG.warning("Error while attempt to restore player's inventory\n" + e.getMessage());
            return false;
        }
        if (!f.delete()) {
            return false;
        }
        PlayerInventory inv = p.getInventory();
        inv.clear();
        inv.setContents(content);
        return true;
    }
}
