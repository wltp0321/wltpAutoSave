// ReloadCommand.java
package world.wltp.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import world.wltp.autosave.AutoSavePlugin;

public class AutoSaveReloadCommand implements CommandExecutor {

    private final AutoSavePlugin plugin;

    public AutoSaveReloadCommand(AutoSavePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("autosave.reload")) {
            sender.sendMessage(ChatColor.RED + "이 명령어를 사용할 권한이 없습니다.");
            return true;
        }

        plugin.reloadConfig();          // config.yml
        plugin.reloadMsgConfig();      // msgconfig.yml
        String startMsg = plugin.getMsgConfig().getString("reload.done", "§9§l[AutoSave]§f §a설정이 성공적으로 리로드되었습니다.");
        sender.sendMessage(startMsg);
        return true;
    }
}
