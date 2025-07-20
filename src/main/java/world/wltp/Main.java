package world.wltp;

import org.bukkit.plugin.java.JavaPlugin;
import world.wltp.autosave.AutoSavePlugin;
import world.wltp.commands.BackupCommand;
import world.wltp.commands.AutoSaveCommand;

public class Main extends JavaPlugin {
    private AutoSavePlugin autoSave;

    @Override
    public void onEnable() {
        saveDefaultConfig();  // config.yml 저장
        autoSave = new AutoSavePlugin(this);
        autoSave.start();

        // 명령어 및 탭 완성 등록
        BackupCommand backupCommand = new BackupCommand(autoSave, this);
        getCommand("backupworld").setExecutor(backupCommand);
        getCommand("backupworld").setTabCompleter(backupCommand);

        AutoSaveCommand autoSaveCommand = new AutoSaveCommand(autoSave, this);
        getCommand("autosave").setExecutor(autoSaveCommand);
        getCommand("autosave").setTabCompleter(autoSaveCommand);
    }

    @Override
    public void onDisable() {
        if (autoSave != null) {
            autoSave.stop();
        }
    }
}
