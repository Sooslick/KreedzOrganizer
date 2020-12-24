package ru.sooslick.bhop;

import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BhopAdmin {

    private CommandSender admin;
    private BhopLevel level;
    private List<CheckListEntry> checklist;

    public BhopAdmin(CommandSender admin, BhopLevel level, boolean enableChecklist) {
        this.admin = admin;
        this.level = level;
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
                checklist.remove(CheckListEntry.REGION);
                checklist.remove(CheckListEntry.BOUNDS);
                admin.sendMessage("§eAssigned region to BhopLevel");
            } else {
                admin.sendMessage("§cCannot assign region to BhopLevel");
            }
        } catch (Exception e) {
            admin.sendMessage("§cCannot assign region to BhopLevel: " + e.getMessage());
        }
        sendStatus();
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
