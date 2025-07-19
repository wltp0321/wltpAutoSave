package world.wltp.autosave;

import org.bukkit.plugin.java.JavaPlugin;

public class AutoSavePlugin {

    private final JavaPlugin plugin;
    private AutoSaveTask task;
    private int interval; // 초 단위

    public AutoSavePlugin(JavaPlugin plugin) {
        this.plugin = plugin;
        this.interval = plugin.getConfig().getInt("autosave.interval", 300); // 기본 300초(5분)
    }

    public void start() {
        stop(); // 기존 작업이 있으면 중지
        task = new AutoSaveTask(plugin);
        task.runTaskTimer(plugin, interval * 20L, interval * 20L);
        plugin.getLogger().info("[AutoSave] starting auto save  " + interval + "초 간격으로 시작합니다.");
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
            plugin.getLogger().info("[AutoSave] 자동 저장을 중지합니다.");
        }
    }
}
