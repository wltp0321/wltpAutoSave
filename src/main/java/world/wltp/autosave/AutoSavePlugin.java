package world.wltp.autosave;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import world.wltp.Main;

public class AutoSavePlugin {
    private final Main plugin;
    private AutoSaveTask task;
    private int interval;
    private FileConfiguration msgConfig;

    public AutoSavePlugin(Main plugin) {
        this.plugin = plugin;
        reloadConfig(); // ← 초기 config도 로드
        reloadMsgConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        interval = plugin.getConfig().getInt("autosave.interval", 300);
    }

    public void reloadMsgConfig() {
        File file = new File(plugin.getDataFolder(), "msgconfig.yml");
        if (!file.exists()) {
            plugin.saveResource("msgconfig.yml", false);
        }
        msgConfig = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getMsgConfig() {
        return msgConfig;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public void start() {
        stop();
        task = new AutoSaveTask(this, plugin);
        task.runTaskTimer(plugin, interval * 20L, interval * 20L);
        String msg = getMsgConfig().getString("save.start", "&9&l[AutoSave]&f 자동저장을 %interval% 초 간격으로 시작합니다.");
        String intervalMsg = msg.replace("%interval%", String.valueOf(interval));
        plugin.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', intervalMsg));
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
            String msg = getMsgConfig().getString("save.stop", "&9&l[AutoSave]&f 자동 저장을 중지합니다.");
            plugin.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        }
    }
}
