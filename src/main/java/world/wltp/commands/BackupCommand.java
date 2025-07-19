package world.wltp.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import world.wltp.autosave.AutoSavePlugin;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupCommand implements CommandExecutor {

    private final AutoSavePlugin plugin;

    public BackupCommand(AutoSavePlugin plugin) {
        this.plugin = plugin;
    }

    // 폴더를 zip으로 압축하는 함수
    private void zipFolder(File sourceFolder, File zipFile) throws IOException {
        try (
                FileOutputStream fos = new FileOutputStream(zipFile);
                ZipOutputStream zos = new ZipOutputStream(fos)
        ) {
            Path sourcePath = sourceFolder.toPath();
            Files.walk(sourcePath)
                    .filter(path -> !Files.isDirectory(path))
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
            sender.sendMessage(ChatColor.RED + "이 명령어를 사용할 권한이 없습니다.");
            return true;
        }

        if (args.length < 1) {
            String usage = plugin.getMsgConfig().getString("backup.usage", "§9§l[AutoSave]§f §c사용법: /backupworld <월드이름>");
            sender.sendMessage(usage);
            return true;
        }

        String inputWorldName = args[0];
        if (inputWorldName.equalsIgnoreCase("nether")) {
            inputWorldName = "world_nether";
        } else if (inputWorldName.equalsIgnoreCase("end")) {
            inputWorldName = "world_the_end";
        }

        final String finalWorldName = inputWorldName;
        final CommandSender finalSender = sender;

        World targetWorld = Bukkit.getWorld(finalWorldName);
        if (targetWorld == null) {
            String startMsg0 = plugin.getMsgConfig().getString("backup.notfound", "§9§l[AutoSave]§f §c월드 '%world%' 을 찾을 수 없습니다.");
            finalSender.sendMessage(startMsg0.replace("%world%", finalWorldName));
            if (targetWorld == null) {
                String startMsg = plugin.getMsgConfig().getString("backup.start", "§9§l[AutoSave]§f §a월드 '%world%' 백업을 시작합니다.");
                finalSender.sendMessage(startMsg.replace("%world%", finalWorldName));
                return true;
            }
        }

        String startMsg = plugin.getMsgConfig().getString("backup.start", "§9§l[AutoSave]§f §a월드 '%world%' 백업을 시작합니다.");
        finalSender.sendMessage(startMsg.replace("%world%", finalWorldName));

        try {
            // 월드 저장은 메인 스레드에서 반드시 실행
            targetWorld.save();
        } catch (Exception e) {
            String errorMsg = plugin.getMsgConfig().getString("backup.error", "§9§l[AutoSave]§f §c백업 도중 오류가 발생했습니다: %error%");
            finalSender.sendMessage(errorMsg.replace("%error%", e.getMessage()));
            return true;
        }

        // 비동기 작업으로 zip 압축 수행
        World finalTargetWorld = targetWorld;
        Bukkit.getScheduler().runTaskAsynchronously(plugin.getPlugin(), () -> {
            File sourceFolder = finalTargetWorld.getWorldFolder();

            String zipName = finalWorldName + "_" + System.currentTimeMillis() + ".zip";
            File zipFile = new File(plugin.getPlugin().getDataFolder(), "backups/" + zipName);
            zipFile.getParentFile().mkdirs();

            try {
                zipFolder(sourceFolder, zipFile);
            } catch (Exception e) {
                Bukkit.getScheduler().runTask(plugin.getPlugin(), () -> {
                    String errorMsg = plugin.getMsgConfig().getString("backup.error", "§9§l[AutoSave]§f §c백업 도중 오류가 발생했습니다: %error%");
                    finalSender.sendMessage(errorMsg.replace("%error%", e.getMessage()));
                });
                return;
            }

            Bukkit.getScheduler().runTask(plugin.getPlugin(), () -> {
                String doneMsg = plugin.getMsgConfig().getString("backup.done", "§9§l[AutoSave]§f §a월드 '%world%' 백업이 완료되었습니다. 위치 : ") + zipFile.getPath();
                finalSender.sendMessage(doneMsg.replace("%world%", finalWorldName));
            });
        });

        return true;
    }
}
