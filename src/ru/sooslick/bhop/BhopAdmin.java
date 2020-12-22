package ru.sooslick.bhop;

import org.bukkit.command.CommandSender;

public class BhopAdmin {

    private CommandSender admin;
    private BhopLevel level;

    public BhopAdmin(CommandSender admin, BhopLevel level) {
        this.admin = admin;
        this.level = level;
    }

    public CommandSender getAdmin() {
        return admin;
    }
}
