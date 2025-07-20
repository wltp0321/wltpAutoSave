package world.wltp.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import world.wltp.Main;
import world.wltp.autosave.AutoSavePlugin;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupCommand implements CommandExecutor, TabCompleter {

    private final AutoSavePlugin plugin;
    private final Main server;

    public BackupCommand(AutoSavePlugin plugin, Main server) {
        this.plugin = plugin;
        this.server = server;
    }

    private void zipFolder(File sourceFolder, File zipFile) throws IOException {
        try (
                FileOutputStream fos = new FileOutputStream(zipFile);
                ZipOutputStream zos = new ZipOutputStream(fos)
        ) {
            Path sourcePath = sourceFolder.toPath();
            Files.walk(sourcePath)
                    .filter(path -> !Files.isDirectory(path))
                    .filter(path -> {
                        String name = path.getFileName().toString();
                        return !name.equals("session.lock"); // 충돌 방지
                    })
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(sourcePath.relativize(path).toString().replace("\\", "/"));
                        try {
                            zos.putNextEntry(zipEntry);
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!sender.hasPermission("autosave.backup")) {
            String permission = plugin.getMsgConfig().getString("nopermission", "&9&l[AutoSave]&f &c이 명령어를 사용할 권한이 없습니다.");
            String finalpermission = ChatColor.translateAlternateColorCodes('&', permission);
            sender.sendMessage(finalpermission);
            return true;
        }

        if (args.length < 1) {
            String usage = plugin.getMsgConfig().getString("backup.usage", "&9&l[AutoSave]&f &c사용법: /backupworld <월드이름>");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', usage));
            return true;
        }

        String inputWorldName = args[0];
        if (inputWorldName.equalsIgnoreCase("nether")) inputWorldName = "world_nether";
        else if (inputWorldName.equalsIgnoreCase("end")) inputWorldName = "world_the_end";

        final String finalWorldName = inputWorldName;
        final CommandSender finalSender = sender;

        World targetWorld = Bukkit.getWorld(finalWorldName);
        if (targetWorld == null) {
            String notFoundMsg = plugin.getMsgConfig().getString("backup.notfound", "&9&l[AutoSave]&f &c월드 '%world%' 을 찾을 수 없습니다.");
            finalSender.sendMessage(ChatColor.translateAlternateColorCodes('&', notFoundMsg).replace("%world%", finalWorldName));
            server.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', notFoundMsg).replace("%world%", finalWorldName));
            return true;
        }

        String startMsg = plugin.getMsgConfig().getString("backup.start", "&9&l[AutoSave]&f &a월드 '%world%' 백업을 시작합니다.");
        finalSender.sendMessage(ChatColor.translateAlternateColorCodes('&', startMsg).replace("%world%", finalWorldName));
        server.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', startMsg).replace("%world%", finalWorldName));


        // 동기적으로 월드 저장
        targetWorld.save();

        // 비동기 작업
        Bukkit.getScheduler().runTaskAsynchronously(plugin.getPlugin(), () -> {
            File sourceFolder = targetWorld.getWorldFolder();
            File backupDir = new File(plugin.getPlugin().getDataFolder(), "backups");
            backupDir.mkdirs();

            String zipName = finalWorldName + "_" + System.currentTimeMillis() + ".zip";
            File zipFile = new File(backupDir, zipName);

            try {
                zipFolder(sourceFolder, zipFile);
            } catch (Exception e) {
                Bukkit.getScheduler().runTask(plugin.getPlugin(), () -> {
                    String errorMsg = plugin.getMsgConfig().getString("backup.error", "&9&l[AutoSave]&f &c백업 도중 오류 발생: %error%");
                    finalSender.sendMessage(ChatColor.translateAlternateColorCodes('&', errorMsg).replace("%error%", e.getMessage()));
                    server.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', errorMsg).replace("%error%", e.getMessage()));

                });
                return;
            }

            Bukkit.getScheduler().runTask(plugin.getPlugin(), () -> {
                String doneMsg = plugin.getMsgConfig().getString("backup.done", "&9&l[AutoSave]&f &a월드 '%world%' 백업이 완료되었습니다. 위치: %path%");
                String finalMsg = ChatColor.translateAlternateColorCodes('&', doneMsg)
                        .replace("%world%", finalWorldName)
                        .replace("%path%", zipFile.getAbsolutePath());
                finalSender.sendMessage(finalMsg);
                server.getServer().getConsoleSender().sendMessage(finalMsg);

            });
        });

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String partial = args[0].toLowerCase();

            for (World world : Bukkit.getWorlds()) {
                String name = world.getName();
                if (name.toLowerCase().startsWith(partial)) {
                    completions.add(name);
                }
            }
            return completions;
        }
        return null;
    }
}
