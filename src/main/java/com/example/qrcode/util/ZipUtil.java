package com.example.qrcode.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * ZIP 工具类
 *
 * @author liuwenjing
 * @since 2023-12-17
 */
public final class ZipUtil {

    /**
     * 将文件列表添加到指定的 ZIP 包中
     *
     * @param zipFilePath 要生成的 ZIP 文件路径
     * @param files       包含文件路径的列表
     * @throws IOException 当操作过程中发生 I/O 错误时抛出异常
     */
    public static void addToZip(String zipFilePath, List<String> files) throws IOException {
        Path path = Paths.get(zipFilePath);
        Path parentDir = path.getParent();

        // 确保父目录存在，如果不存在则创建
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            for (String filePath : files) {
                File file = new File(filePath);
                if (file.exists()) {
                    addToZipEntry(zos, file);
                }
            }
        }
    }

    private static void addToZipEntry(ZipOutputStream zos, File file) throws IOException {
        ZipEntry zipEntry = new ZipEntry(file.getName());
        zos.putNextEntry(zipEntry);

        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = fis.read(buffer)) > 0) {
            zos.write(buffer, 0, length);
        }
        fis.close();
        zos.closeEntry();
    }

}
