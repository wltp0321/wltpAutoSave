package world.wltp.autosave;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class AutoSaveTask extends BukkitRunnable {

    private final JavaPlugin plugin;

    public AutoSaveTask(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        // 메인 스레드에서 world.save() 실행
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (World world : Bukkit.getWorlds()) {
                world.save();
            }
            plugin.getLogger().info("[AutoSave] Every worlds is saved");
        });
    }
}
