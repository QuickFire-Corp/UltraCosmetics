package be.isach.ultracosmetics.cosmetics.morphs;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.cosmetics.type.MorphType;
import be.isach.ultracosmetics.player.UltraPlayer;
import com.cryptomorin.xseries.XSound;
import me.libraryaddict.disguise.disguisetypes.watchers.SheepWatcher;
import org.bukkit.DyeColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Represents an instance of a sheep morph summoned by a player.
 *
 * @author RadBuilder
 * @since 07-03-2017
 */
public class MorphSheep extends MorphLeftClickCooldown {
    public MorphSheep(UltraPlayer owner, MorphType type, UltraCosmetics ultraCosmetics) {
        super(owner, type, ultraCosmetics, 3);
    }

    @Override
    @EventHandler
    public void onLeftClick(PlayerInteractEvent event) {
        event.setCancelled(true);
        XSound.ENTITY_SHEEP_AMBIENT.play(event.getPlayer().getLocation(), 1.0f, 1.0f);
        SheepWatcher sheepWatcher = (SheepWatcher) getDisguise().getWatcher();
        new BukkitRunnable() {
            private int count = 0;

            @Override
            public void run() {
                if (count > 9) {
                    cancel();
                    return;
                }
                sheepWatcher.setColor(DyeColor.values()[RANDOM.nextInt(DyeColor.values().length)]);
                count++;
            }
        }.runTaskTimer(getUltraCosmetics(), 0, 2);
    }
}
