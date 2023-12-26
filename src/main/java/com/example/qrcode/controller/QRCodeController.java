package com.example.qrcode.controller;

import com.example.qrcode.config.QRCodeProperties;
import com.example.qrcode.core.QRCodeGenerator;
import com.example.qrcode.entity.QRCodeExtraData;
import com.example.qrcode.util.ZipUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.dromara.hutool.core.util.SystemUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 二维码生成控制器
 *
 * @author weasley
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/qrcode")
public class QRCodeController {

    @Autowired
    private QRCodeProperties qrCodeProperties;

    /**
     * 生成二维码图片
     */
    @GetMapping("/generate")
    public void generate(HttpServletRequest request, HttpServletResponse response) {
        log.info("生成二维码图片");

        String extension = FilenameUtils.getExtension(qrCodeProperties.getIconPath());
        QRCodeExtraData testInstance = QRCodeExtraData.getTestInstance();
        String currentDir = SystemUtil.get("user.dir");
        String targetFilepath = currentDir + File.separator + testInstance.getFileName() + "." + extension;

        String withExtraDataPath = QRCodeGenerator.generateQRCodeWithExtraData(
                qrCodeProperties.getBaseText(), 400,
                qrCodeProperties.getIconPath(), targetFilepath, testInstance);

        log.info("生成的二维码位置: {}", withExtraDataPath);

        String encodedFileName = getEncodedFileName(withExtraDataPath);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(URLConnection.guessContentTypeFromName(encodedFileName));
        response.addHeader("File-Base-Name", encodedFileName); // 告诉前端文件基本名
        response.setHeader("Content-Disposition", "inline; filename=" + encodedFileName);

        try {
            Files.copy(new File(withExtraDataPath).toPath(), response.getOutputStream());
        } catch (Exception e) {
            log.error("文件数据转换失败, 文件名 {},", encodedFileName, e);
        }

        CompletableFuture.runAsync(() -> {
            try {
                Files.deleteIfExists(new File(withExtraDataPath).toPath());
            } catch (Exception e) {
                log.error("删除文件失败: {}", withExtraDataPath, e);
            }
        });
    }

    /**
     * 下载二维码压缩包
     */
    @GetMapping("/download")
    public void download(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 生成两个二维码图片
        QRCodeExtraData testInstance1 = QRCodeExtraData.getTestInstance();
        QRCodeExtraData testInstance2 = QRCodeExtraData.getTestInstance().setStationName("奥迪充电站杭州城西银泰超级充电站");

        String currentDir = SystemUtil.get("user.dir");
        String targetFilepath1 = currentDir + File.separator + testInstance1.getFileName() + ".png";
        String targetFilepath2 = currentDir + File.separator + testInstance2.getFileName() + ".png";

        String qrCodePath1 = QRCodeGenerator.generateQRCodeWithExtraData(qrCodeProperties.getBaseText(), 400, qrCodeProperties.getIconPath(), targetFilepath1, testInstance1);
        String qrCodePath2 = QRCodeGenerator.generateQRCodeWithExtraData(qrCodeProperties.getBaseText(), 400, qrCodeProperties.getIconPath(), targetFilepath2, testInstance2);

        // 压缩成zip包
        String zipFilePath = currentDir + File.separator + testInstance1.getStationName() + "充电枪编码集.zip";
        String encodedFileName = getEncodedFileName(zipFilePath);
        ZipUtil.addToZip(zipFilePath, List.of(qrCodePath1, qrCodePath2));

        // 将zip包写入response
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(URLConnection.guessContentTypeFromName(encodedFileName));
        response.addHeader("File-Base-Name", encodedFileName);
        response.setHeader("Content-Disposition", "attachment; filename=" + encodedFileName);
        try {
            Files.copy(new File(zipFilePath).toPath(), response.getOutputStream());
        } catch (Exception e) {
            log.error("文件数据转换失败, 文件名 {},", encodedFileName, e);
        }

        // 清理文件
        CompletableFuture.runAsync(() -> {
            try {
                Files.deleteIfExists(new File(qrCodePath1).toPath());
                Files.deleteIfExists(new File(qrCodePath2).toPath());
                Files.deleteIfExists(new File(zipFilePath).toPath());
            } catch (Exception e) {
                log.error("删除文件失败: {}", zipFilePath, e);
            }
        });
    }

    private String getEncodedFileName(String fileBaseName) {
        String extension = FilenameUtils.getExtension(fileBaseName);
        String baseName = FilenameUtils.getBaseName(fileBaseName);
        String baseNameRemoveBlank = baseName.replaceAll("\\s+", "");
        String encodedFileName = URLEncoder.encode(baseNameRemoveBlank, StandardCharsets.UTF_8);
        encodedFileName = encodedFileName.replace("\\+", "%20");
        encodedFileName = encodedFileName.concat(".").concat(extension);
        return encodedFileName;
    }
}
