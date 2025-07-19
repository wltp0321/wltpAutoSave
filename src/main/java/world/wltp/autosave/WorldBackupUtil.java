package world.wltp.autosave;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.bukkit.World;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WorldBackupUtil {

    public static void backupWorldTarGz(File pluginDataFolder, World world) throws IOException {
        File worldFolder = world.getWorldFolder();

        File backupDir = new File(pluginDataFolder, "backups");
        if (!backupDir.exists()) backupDir.mkdirs();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File targetFile = new File(backupDir, world.getName() + "_" + timeStamp + ".tar.gz");

        try (
                FileOutputStream fos = new FileOutputStream(targetFile);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(bos);
                TarArchiveOutputStream taos = new TarArchiveOutputStream(gzos)
        ) {
            taos.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
            addFileToTar(taos, worldFolder, "");
        }
    }

    private static void addFileToTar(TarArchiveOutputStream taos, File file, String parent) throws IOException {
        String entryName = parent + file.getName();
        TarArchiveEntry entry = new TarArchiveEntry(file, entryName);

        taos.putArchiveEntry(entry);

        if (file.isFile()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) != -1) {
                    taos.write(buffer, 0, len);
                }
            }
            taos.closeArchiveEntry();
        } else {
            taos.closeArchiveEntry();
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    addFileToTar(taos, child, entryName + "/");
                }
            }
        }
    }
}
