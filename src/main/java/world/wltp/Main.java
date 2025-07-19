package world.wltp;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import world.wltp.autosave.AutoSavePlugin;
import world.wltp.autosave.WorldBackupUtil;

public class Main extends JavaPlugin {

    private FileConfiguration messagesConfig;
    private File msgFile;
    private AutoSavePlugin autoSave;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadMessagesConfig();
        autoSave = new AutoSavePlugin(this);
        autoSave.start();
    }

    private void loadMessagesConfig() {
        msgFile = new File(getDataFolder(), "msgconfig.yml");
        if (!msgFile.exists()) {
            saveResource("msgconfig.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(msgFile);
    }


    @Override
    public void onDisable() {
        if (autoSave != null) {
            autoSave.stop();
        }
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("autosavereload")) {
            reloadConfig();
            if (autoSave != null) autoSave.stop();
            autoSave = new AutoSavePlugin(this);
            autoSave.start();
            sender.sendMessage("§a[AutoSave] 설정을 다시 불러오고 자동 저장을 재시작했습니다.");
            return true;

        } else if (label.equalsIgnoreCase("backupworld")) {
            if (args.length != 1) {
                sender.sendMessage("§c사용법: /backupworld <월드이름>");
                return true;
            }
            String worldName = args[0];
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                sender.sendMessage("§c월드 '" + worldName + "' 을 찾을 수 없습니다.");
                return true;
            }

            sender.sendMessage("§a[백업] 월드 '" + worldName + "' 백업을 시작합니다...");

            // ✅ 안전하게 비동기 + 메인 스레드 메시지 처리
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                try {
                    WorldBackupUtil.backupWorldTarGz(getDataFolder(), world);
                    Bukkit.getScheduler().runTask(this, () -> {
                        sender.sendMessage("§a[백업] 월드 '" + worldName + "' 백업이 완료되었습니다.");
                    });
                    getLogger().info("[백업] " + worldName + " 월드 백업 완료 (명령어)");
                } catch (Exception e) {
                    Bukkit.getScheduler().runTask(this, () -> {
                        sender.sendMessage("§c[백업] 백업 도중 오류가 발생했습니다: " + e.getMessage());
                    });
                    getLogger().warning("[백업] " + worldName + " 백업 실패: " + e.getMessage());
                    e.printStackTrace();
                }
            });

            return true;
        }

        return false;
    }
}
