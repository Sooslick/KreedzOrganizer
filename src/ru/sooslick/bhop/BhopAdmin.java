package ru.sooslick.bhop;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BhopAdmin {

    private final CommandSender admin;
    private final BhopLevel level;
    private final List<CheckListEntry> checklist;

    private Location bound1;
    private Location bound2;

    public BhopAdmin(CommandSender admin, BhopLevel level, boolean enableChecklist) {
        this.admin = admin;
        this.level = level;
        if (level == null) {
            bound1 = null;
            bound2 = null;
        } else {
            bound1 = level.getBhopRegion().getBound1();
            bound2 = level.getBhopRegion().getBound2();
        }
        //todo: do not add region in useWg is disabled
        checklist = enableChecklist ?
                Arrays.asList(CheckListEntry.REGION, CheckListEntry.BOUNDS, CheckListEntry.START,
                        CheckListEntry.FINISH, CheckListEntry.TRIGGER, CheckListEntry.CHECKPOINTS) :
                Collections.emptyList();
    }

    public CommandSender getAdmin() {
        return admin;
    }

    public void setRegion(World world, String rgName) {
        try {
            if (level.setRegion(world, rgName)) {
                admin.sendMessage("§eAssigned region to BhopLevel");
                checklist.remove(CheckListEntry.REGION);
                checklist.remove(CheckListEntry.BOUNDS);
                sendStatus();
                bound1 = level.getBhopRegion().getBound1();
                bound2 = level.getBhopRegion().getBound2();
            } else {
                admin.sendMessage("§cCannot assign region to BhopLevel");
            }
        } catch (Exception e) {
            admin.sendMessage("§cCannot assign region to BhopLevel: " + e.getMessage());
        }
    }

    public void setBound(String cmd, Location loc) {
        //totally weird bound1 / bound2 check
        if (cmd.contains("1")) {
            bound1 = loc;
        } else {
            bound2 = loc;
        }
        if (bound1 != null && bound2 != null) {
            level.setBounds(bound1, bound2);
            admin.sendMessage("§eSet bounding for level");
            checklist.remove(CheckListEntry.BOUNDS);
            checklist.remove(CheckListEntry.REGION);
            sendStatus();
        } else {
            admin.sendMessage("§eNow set the second position of level bounding");
        }
    }

    public void sendStatus() {
        if (checklist.isEmpty())
            return;
        StringBuilder sb = new StringBuilder("§cChecklist: ");
        checklist.forEach(e -> sb.append(e.getDescription()).append(", "));
        admin.sendMessage(sb.toString());
    }

    private enum CheckListEntry {
        REGION("Region"),
        BOUNDS("Boundaries"),
        START("Start position"),
        FINISH("Finish location"),
        TRIGGER("Finish trigger"),
        CHECKPOINTS("Checkpoints");

        private final String description;

        CheckListEntry(String description) {
            this.description = description;
        }

        String getDescription() {
            return description;
        }
    }
}
