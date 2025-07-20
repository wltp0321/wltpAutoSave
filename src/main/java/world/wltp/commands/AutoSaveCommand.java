package world.wltp.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.*;
import world.wltp.Main;
import world.wltp.autosave.AutoSavePlugin;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

public class AutoSaveCommand implements CommandExecutor, TabCompleter {

    private final AutoSavePlugin plugin;
    private final Main mainPlugin;

    public AutoSaveCommand(AutoSavePlugin plugin, Main mainPlugin) {
        this.plugin = plugin;
        this.mainPlugin = mainPlugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            String usage = plugin.getMsgConfig().getString("usage", "&9&l[AutoSave]&f &c사용법: /autosave backup 또는 /autosave reload");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', usage));
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("backup")) {
            if (!sender.hasPermission("autosave.backup")) {
                String noPermission = plugin.getMsgConfig().getString("nopermission", "&9&l[AutoSave]&f &c이 명령어를 사용할 권한이 없습니다.");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', noPermission));
                return true;
            }

            if (args.length == 1) {
                // 모든 월드 백업
                String startAllMsg = plugin.getMsgConfig().getString("backup.startall", "&9&l[AutoSave]&f &a서버의 모든 월드 백업을 시작합니다.");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', startAllMsg));

                for (World world : Bukkit.getWorlds()) {
                    backupWorld(sender, world);
                }
                return true;
            }

            // 특정 월드 백업
            String worldName = args[1];
            World targetWorld = Bukkit.getWorld(worldName);
            if (targetWorld == null) {
                String notFoundMsg = plugin.getMsgConfig().getString("backup.notfound", "&9&l[AutoSave]&f &c월드 '%world%' 을 찾을 수 없습니다.");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', notFoundMsg).replace("%world%", worldName));
                return true;
            }

            backupWorld(sender, targetWorld);
            return true;
        }

        if (subCommand.equals("reload")) {
            if (!sender.hasPermission("autosave.reload")) {
                String noPermission = plugin.getMsgConfig().getString("nopermission", "&9&l[AutoSave]&f &c이 명령어를 사용할 권한이 없습니다.");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', noPermission));
                return true;
            }

            plugin.reloadConfig();
            plugin.reloadMsgConfig();
            plugin.start();

            String reloadDone = plugin.getMsgConfig().getString("reload.done", "&9&l[AutoSave]&f &a설정이 성공적으로 리로드되었습니다.");
            String finalMsg = ChatColor.translateAlternateColorCodes('&', reloadDone);

            sender.sendMessage(finalMsg);
            mainPlugin.getServer().getConsoleSender().sendMessage(finalMsg);
            return true;
        }

        String unknown = plugin.getMsgConfig().getString("unknown.command", "&9&l[AutoSave]&f &c알 수 없는 하위 명령어입니다.");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', unknown));
        return true;
    }

    private void backupWorld(CommandSender sender, World world) {
        String startMsg = plugin.getMsgConfig().getString("backup.start", "&9&l[AutoSave]&f &a월드 '%world%' 백업을 시작합니다.");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', startMsg).replace("%world%", world.getName()));

        // 1. 메인 스레드에서 월드 저장
        Bukkit.getScheduler().runTask(mainPlugin, () -> {
            try {
                world.save();

                // 2. 저장 후 비동기 백업 작업 실행
                Bukkit.getScheduler().runTaskAsynchronously(mainPlugin, () -> {
                    try {
                        File sourceFolder = world.getWorldFolder();
                        File backupDir = new File(mainPlugin.getDataFolder(), "backups");
                        if (!backupDir.exists()) backupDir.mkdirs();

                        String tarGzName = world.getName() + "_" + System.currentTimeMillis() + ".tar.gz";
                        File tarGzFile = new File(backupDir, tarGzName);

                        createTarGz(sourceFolder, tarGzFile);

                        // 백업 완료 메시지 출력 (메인 스레드)
                        Bukkit.getScheduler().runTask(mainPlugin, () -> {
                            String doneMsg = plugin.getMsgConfig().getString("backup.done", "&9&l[AutoSave]&f &a월드 '%world%' 백업이 완료되었습니다. 위치: %path%");
                            doneMsg = ChatColor.translateAlternateColorCodes('&', doneMsg)
                                    .replace("%world%", world.getName())
                                    .replace("%path%", tarGzFile.getAbsolutePath());
                            sender.sendMessage(doneMsg);
                            mainPlugin.getServer().getConsoleSender().sendMessage("[AutoSave] " + doneMsg);
                        });
                    } catch (Exception e) {
                        Bukkit.getScheduler().runTask(mainPlugin, () -> {
                            String errorMsg = plugin.getMsgConfig().getString("backup.error", "&9&l[AutoSave]&f &c백업 중 오류 발생: %error%");
                            errorMsg = ChatColor.translateAlternateColorCodes('&', errorMsg).replace("%error%", e.getMessage());
                            sender.sendMessage(errorMsg);
                        });
                    }
                });

            } catch (Exception e) {
                String errorMsg = plugin.getMsgConfig().getString("backup.error", "&9&l[AutoSave]&f &c백업 중 오류 발생: %error%");
                errorMsg = ChatColor.translateAlternateColorCodes('&', errorMsg).replace("%error%", e.getMessage());
                sender.sendMessage(errorMsg);
            }
        });
    }

    private void createTarGz(File sourceDir, File outputFile) throws IOException {
        try (
                FileOutputStream fos = new FileOutputStream(outputFile);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                GZIPOutputStream gos = new GZIPOutputStream(bos);
                TarArchiveOutputStream taos = new TarArchiveOutputStream(gos)
        ) {
            taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

            Path sourcePath = sourceDir.toPath();

            Files.walk(sourcePath)
                    .filter(path -> !Files.isDirectory(path))
                    .filter(path -> !path.getFileName().toString().equals("session.lock"))
                    .forEach(path -> {
                        TarArchiveEntry entry = new TarArchiveEntry(path.toFile(), sourcePath.relativize(path).toString().replace("\\", "/"));
                        try {
                            taos.putArchiveEntry(entry);
                            Files.copy(path, taos);
                            taos.closeArchiveEntry();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });

            taos.finish();
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            if ("backup".startsWith(partial)) completions.add("backup");
            if ("reload".startsWith(partial)) completions.add("reload");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("backup")) {
            String partial = args[1].toLowerCase();
            for (World world : Bukkit.getWorlds()) {
                if (world.getName().toLowerCase().startsWith(partial)) {
                    completions.add(world.getName());
                }
            }
        }

        return completions;
    }
}
