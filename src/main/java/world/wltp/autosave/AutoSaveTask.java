package world.wltp.autosave;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.ChatColor;

public class AutoSaveTask extends BukkitRunnable {
    private final AutoSavePlugin asPlugin;

    public AutoSaveTask(AutoSavePlugin plugin) {
        this.asPlugin = plugin;
    }

    @Override
    public void run() {
        Bukkit.getScheduler().runTask(asPlugin.getPlugin(), () -> {
            for (World w : Bukkit.getWorlds()) {
                w.save();
            }
            String msg = asPlugin.getMsgConfig().getString("saved.done", "&a[AutoSave] 월드 저장 완료!");
            asPlugin.getPlugin().getLogger().info(ChatColor.translateAlternateColorCodes('§', msg));
        });
    }
}
