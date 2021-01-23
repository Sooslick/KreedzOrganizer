package ru.sooslick.bhop;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import ru.sooslick.bhop.region.BhopRegion;

import java.util.*;

public class BhopAdmin {

    private final CommandSender admin;
    private final BhopLevel level;
    private final Set<CheckListEntry> checklist;

    private Location bound1;
    private Location bound2;

    private String cpName;
    private Location cpLoad;
    private Location cpTrigger;
    private TriggerType cpType;

    public BhopAdmin(CommandSender admin, BhopLevel level, boolean enableChecklist) {
        this.admin = admin;
        this.level = level;
        cpName = null;
        cpLoad = null;
        cpTrigger = null;
        cpType = TriggerType.MOVEMENT;
        if (level == null) {
            bound1 = null;
            bound2 = null;
        } else {
            bound1 = level.getBhopRegion().getBound1();
            bound2 = level.getBhopRegion().getBound2();
        }
        checklist = new HashSet<>();
        if (enableChecklist) {
            if (Engine.getInstance().isUseWg()) checklist.add(CheckListEntry.REGION);
            checklist.addAll(Arrays.asList(CheckListEntry.BOUNDS, CheckListEntry.START,
                    CheckListEntry.FINISH, CheckListEntry.TRIGGER, CheckListEntry.CHECKPOINTS));
        }
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

                //check start/finish
                if (level.getStartPosition() != null)
                    if (!level.isInside(level.getStartPosition()))
                        admin.sendMessage("§cStart location is outside of BhopLevel");
                if (level.getFinish() != null)
                    if (!level.isInside(level.getFinish()))
                        admin.sendMessage("§cFinish location is outside of BhopLevel");
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
            if (!bound1.getWorld().equals(bound2.getWorld())) {
                admin.sendMessage("§cBounds are located in different worlds, cannot set bounding");
                return;
            }
            level.setBounds(bound1, bound2);
            admin.sendMessage("§eSet bounding for level");
            checklist.remove(CheckListEntry.BOUNDS);
            checklist.remove(CheckListEntry.REGION);
            sendStatus();
            //check start/finish
            if (level.getStartPosition() != null)
                if (!level.isInside(level.getStartPosition()))
                    admin.sendMessage("§cStart location is outside of BhopLevel");
            if (level.getFinish() != null)
                if (!level.isInside(level.getFinish()))
                    admin.sendMessage("§cFinish location is outside of BhopLevel");
        } else {
            admin.sendMessage("§eNow set the second position of level bounding");
        }
    }

    public void setStart(Location loc) {
        //check region
        if (level.getBhopRegion() != null) {
            if (!level.getBhopRegion().getBound1().getWorld().equals(loc.getWorld())) {
                admin.sendMessage("§cBounding are located in different world, cannot set start");
                return;
            }
        }
        level.setStart(loc);
        checklist.remove(CheckListEntry.START);
        admin.sendMessage("§eSet start position for level");
        sendStatus();
    }

    public void setFinish(Location loc) {
        //check region
        if (level.getBhopRegion() != null) {
            if (!level.getBhopRegion().getBound1().getWorld().equals(loc.getWorld())) {
                admin.sendMessage("§cBounding are located in different world, cannot set start");
                return;
            }
        }
        level.setStart(loc);
        checklist.remove(CheckListEntry.START);
        admin.sendMessage("§eSet start position for level");
        sendStatus();
    }

    public void setTriggerType(TriggerType triggerType) {
        level.setTriggerType(triggerType);
        checklist.remove(CheckListEntry.TRIGGER);
        admin.sendMessage("§eSet finish trigger type to " + triggerType.name());
        sendStatus();
    }

    public boolean saveLevel() {
        List<String> errors = new LinkedList<>();
        //validate level
        if (level == null) {
            admin.sendMessage("§cBhopLevel is null o_O");
            return false;
        }
        BhopRegion bhrg = level.getBhopRegion();
        if (bhrg == null) {
            admin.sendMessage("§cLevel bounding not specified");
            return false;
        }
        Location start = level.getStartPosition();
        if (start == null)
            errors.add("§cStart position not specified");
        else if (!level.isInside(start))
            errors.add("§cStart position is outside of level");
        Location finish = level.getFinish();
        if (finish == null)
            errors.add("§cFinish position not specified");
        else if (!level.isInside(finish))
            errors.add("§cFinish position is outside of level");
        for (BhopCheckpoint bhcp : level.getCheckpoints()) {
            if (!level.isInside(bhcp.getLoadLocation()))
                errors.add("§cLoad position of checkpoint " + bhcp.getName() + " is outside of level");
            if (!level.isInside(bhcp.getTriggerLocation()))
                errors.add("§cTrigger position of checkpoint " + bhcp.getName() + " is outside of level");
        }
        //print errors list
        if (errors.size() > 0) {
            errors.forEach(admin::sendMessage);
            return false;
        }
        //post-validate
        if (level.getTriggerType() == null)
            setTriggerType(TriggerType.INTERACT);
        level.setChanged();
        //save level
        BhopLevel old = Engine.getInstance().getBhopLevel(level.getName());
        if (old != null)
            Engine.getInstance().getBhopLevelList().remove(old);
        Engine.getInstance().getBhopLevelList().add(level);
        admin.sendMessage("§aSaved level " + level.getName());
        return true;
    }

    public void createCheckpoint(String name) {
        if (cpName != null) {
            admin.sendMessage("§cYou have pending checkpoint §6" + cpName + "§c. Use §6/bhopmanage checkpoint save §cor §6/bhopmanage checkpoint discard");
            return;
        }
        BhopCheckpoint fcp = level.getCheckpoint(name);
        if (fcp != null) {
            admin.sendMessage("§cCheckpoint with same name exists, use §6/bhopmanage checkpoint edit " + name + " §cinstead.");
            return;
        }
        cpName = name;
        cpLoad = null;
        cpTrigger = null;
        cpType = TriggerType.MOVEMENT;
        admin.sendMessage("§eCreated checkpoint " + cpName + ", now set trigger and load positions");
        checklist.remove(CheckListEntry.CHECKPOINTS);
    }

    public void editCheckpoint(String name) {
        if (cpName != null) {
            admin.sendMessage("§cYou have pending checkpoint §6" + cpName + "§c. Use §6/bhopmanage checkpoint save §cor §6/bhopmanage checkpoint discard");
            return;
        }
        BhopCheckpoint fcp = level.getCheckpoint(name);
        if (fcp == null) {
            admin.sendMessage("§cCheckpoint not exists, use §6/bhopmanage checkpoint create " + name + " §cinstead.");
            return;
        }
        cpName = fcp.getName();
        cpLoad = fcp.getLoadLocation();
        cpTrigger = fcp.getTriggerLocation();
        cpType = fcp.getTriggerType();
        admin.sendMessage("§eStarted editing checkpoint " + cpName + ", now set trigger and load positions");
    }

    public void deleteCheckpoint(String name) {
        BhopCheckpoint fcp = level.getCheckpoint(name);
        if (fcp == null) {
            admin.sendMessage("§cCheckpoint not exists.");
            return;
        }
        level.getCheckpoints().remove(fcp);
        admin.sendMessage("§eCheckpoint " + fcp.getName() + " deleted");
    }

    public void setCheckpointLoad(Location loc) {
        if (cpName == null) {
            admin.sendMessage("§cYou aren't editing any checkpoint, use §6/bhopmanage checkpoint create / edit §ccommands");
            return;
        }
        BhopRegion bhrg = level.getBhopRegion();
        if (bhrg == null || bhrg.isInside(loc)) {
            cpLoad = loc;
            admin.sendMessage("§eSet load position for checkpoint " + cpName);
            return;
        }
        admin.sendMessage("§cLoad position is outside of level bounding");
    }

    public void setCheckpointTrigger(Location loc) {
        if (cpName == null) {
            admin.sendMessage("§cYou aren't editing any checkpoint, use §6/bhopmanage checkpoint create / edit §ccommands");
            return;
        }
        BhopRegion bhrg = level.getBhopRegion();
        if (bhrg == null || bhrg.isInside(loc)) {
            cpTrigger = loc;
            admin.sendMessage("§eSet trigger position for checkpoint " + cpName);
            return;
        }
        admin.sendMessage("§cTrigger position is outside of level bounding");
    }

    public void setCheckpointTriggerType(TriggerType type) {
        if (cpName == null) {
            admin.sendMessage("§cYou aren't editing any checkpoint, use §6/bhopmanage checkpoint create / edit §ccommands");
            return;
        }
        cpType = type;
        admin.sendMessage("§eChanged checkpoint's trigger type");
    }

    public void saveCheckpoint() {
        if (cpName == null) {
            admin.sendMessage("§cYou aren't editing any checkpoint, use §6/bhopmanage checkpoint create / edit §ccommands");
            return;
        }
        //todo: validate and format errors list, check bounding and etc
        if (cpLoad == null && cpTrigger == null) {
            admin.sendMessage("§cCannot save checkpoint, trigger and load positions are not specified");
            return;
        }
        //todo before here
        if (cpLoad == null)
            cpLoad = cpTrigger;
        else if (cpTrigger == null)
            cpTrigger = cpLoad;
        BhopCheckpoint exists = level.getCheckpoint(cpName);
        level.getCheckpoints().remove(exists);
        BhopCheckpoint newCp = new BhopCheckpoint(cpName, cpLoad, cpTrigger, cpType);
        level.addCheckpoint(newCp);
        admin.sendMessage("§eSaved checkpoint " + cpName);
        cpName = null;
        cpLoad = null;
        cpTrigger = null;
        cpType = TriggerType.MOVEMENT;
    }

    public void discardCheckpoint() {
        admin.sendMessage("§eDiscard changes on checkpoint " + cpName);
        cpName = null;
        cpLoad = null;
        cpTrigger = null;
        cpType = TriggerType.MOVEMENT;
    }

    public void sendStatus() {
        if (checklist.isEmpty())
            return;
        StringBuilder sb = new StringBuilder("§cChecklist: ");
        checklist.forEach(e -> sb.append(e.getDescription()).append(", "));
        admin.sendMessage(sb.toString());
    }

    //todo rework (and remove?)
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
