package io.dogsbean.bedwarspractice.setup.manager;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SetupManager {
    private final Map<UUID, Location> pos1Selections = new HashMap<>();
    private final Map<UUID, Location> pos2Selections = new HashMap<>();

    public void setPos1(Player player, Location location) {
        pos1Selections.put(player.getUniqueId(), location);
    }

    public void setPos2(Player player, Location location) {
        pos2Selections.put(player.getUniqueId(), location);
    }

    public Location getPos1(Player player) {
        return pos1Selections.get(player.getUniqueId());
    }

    public Location getPos2(Player player) {
        return pos2Selections.get(player.getUniqueId());
    }

    public boolean hasFullSelection(Player player) {
        return pos1Selections.containsKey(player.getUniqueId()) && pos2Selections.containsKey(player.getUniqueId());
    }
}
