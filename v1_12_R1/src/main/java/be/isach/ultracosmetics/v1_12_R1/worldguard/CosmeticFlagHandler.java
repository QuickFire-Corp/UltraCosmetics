package be.isach.ultracosmetics.v1_12_R1.worldguard;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.SetFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.Handler;

import java.util.HashSet;
import java.util.Set;

import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.player.UltraPlayerManager;

public class CosmeticFlagHandler extends Handler {
    private static final Set<Category> ALL_CATEGORIES = new HashSet<>();
    static {
        for (Category cat : Category.values()) {
            ALL_CATEGORIES.add(cat);
        }
    }
    private final UltraPlayerManager pm;
    private final StateFlag cosmeticsFlag;
    private final SetFlag<Category> categoryFlag;   
    private Set<Category> lastCategoryFlagValue = null;
    protected CosmeticFlagHandler(Session session, StateFlag cosmeticsFlag, SetFlag<Category> categoryFlag) {
        super(session);
        pm = UltraCosmeticsData.get().getPlugin().getPlayerManager();
        this.cosmeticsFlag = cosmeticsFlag;
        this.categoryFlag = categoryFlag;
    }

    @Override
    public boolean onCrossBoundary(Player player, Location from, Location to, ApplicableRegionSet toSet, Set<ProtectedRegion> entered, Set<ProtectedRegion> exited, MoveType moveType) {
        // ensure player is a real player and not an NPC
        if (Bukkit.getPlayer(player.getUniqueId()) == null) return true;

        LocalPlayer wrappedPlayer = getSession().getManager().getPlugin().wrapPlayer(player);
        State currentValue = toSet.queryState(wrappedPlayer, cosmeticsFlag);
        if (currentValue == State.DENY) {
            if (pm.getUltraPlayer(player).clear()) {
                player.sendMessage(MessageManager.getMessage("Region-Disabled"));
            }
            // This is effectively what DENY represents for the `uc-cosmetics` flag
            lastCategoryFlagValue = ALL_CATEGORIES;
            return true;
        }
        Set<Category> categoryValue = toSet.queryValue(wrappedPlayer, categoryFlag);
        Set<Category> needsUpdating = compareSets(categoryValue, lastCategoryFlagValue);
        // This check is not actually required, but it saves the call to getUltraPlayer if we don't actually need it.
        if (needsUpdating.size() > 0) {
            UltraPlayer up = pm.getUltraPlayer(player);
            for (Category cat : needsUpdating) {
                if (up.removeCosmetic(cat)) {
                    player.sendMessage(MessageManager.getMessage("Region-Disabled-Category").replace("%category%", cat.getConfigName()));
                }
            }
        }
        lastCategoryFlagValue = categoryValue;
        return true;
    }

    /**
     * Returns any values in currentSet that are not in previousSet,
     * or an empty set if no comparison is required.
     * 
     * @param currentSet The active set of categories
     * @param previousSet The last known active set of categories
     * @return a set containing any Categories not present in previousSet  
     */
    private Set<Category> compareSets(Set<Category> currentSet, Set<Category> lastSet) {
        Set<Category> newValues = new HashSet<>();
        if (currentSet == null) return newValues;
        if (lastSet == null) return currentSet;
        newValues.addAll(currentSet);
        newValues.removeAll(lastSet);
        return newValues;
    }
}
