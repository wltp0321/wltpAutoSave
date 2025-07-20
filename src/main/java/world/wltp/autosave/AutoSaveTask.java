package world.wltp.autosave;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.ChatColor;
import world.wltp.Main;

public class AutoSaveTask extends BukkitRunnable {
    private final AutoSavePlugin asPlugin;
    private final Main server;

    public AutoSaveTask(AutoSavePlugin plugin, Main server) {
        this.asPlugin = plugin;
        this.server = server;
    }

    @Override
    public void run() {
        Bukkit.getScheduler().runTask(asPlugin.getPlugin(), () -> {
            for (World w : Bukkit.getWorlds()) {
                w.save();
            }
            String msg = asPlugin.getMsgConfig().getString("save.done", "&a[AutoSave] 월드 저장 완료!");
            server.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        });
    }
}
