package com.example.qrcode.config;

import com.example.qrcode.config.QRCodeProperties.QRCodeFontProperties;
import com.example.qrcode.core.QRCodeGenerator;
import com.example.qrcode.entity.QRCodeExtraData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.dromara.hutool.core.util.SystemUtil;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Enter the description of this class here
 *
 * @author weasley
 * @version 1.0.0
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({QRCodeProperties.class})
public class QRCodeExtraConfig {

    @Bean
    public ApplicationRunner qrCodeExtraTestRunner(QRCodeProperties qrCodeProperties) {
        return args -> {
            if (Boolean.TRUE.equals(qrCodeProperties.getTest())) {
                String text = qrCodeProperties.getBaseText();
                QRCodeExtraData extraData = QRCodeExtraData.getTestInstance();
                String baseName = FilenameUtils.getBaseName(qrCodeProperties.getIconPath());
                String extension = FilenameUtils.getExtension(qrCodeProperties.getIconPath());
                CanDisplayChinese.canDisplay(); // 打印所有支持的中文字体
                String currentDir = SystemUtil.get("user.dir");
                String targetFilepath = currentDir + "/" + baseName + "_target1." + extension;
                String withExtraDataPath = QRCodeGenerator.generateQRCodeWithExtraData(text, 400, qrCodeProperties.getIconPath(), targetFilepath, extraData);
                log.info("生成的二维码位置: {}", withExtraDataPath);
                targetFilepath = currentDir + "/" + baseName + "_target2." + extension;
                extraData.setStationName("奥迪充电站杭州城西银泰宇宙无敌充电站");
                withExtraDataPath = QRCodeGenerator.generateQRCodeWithExtraData(text, 400, qrCodeProperties.getIconPath(), targetFilepath, extraData);
                log.info("生成的二维码位置: {}", withExtraDataPath);
            }
        };
    }

    @Bean
    public Font defaultFont(QRCodeProperties qrCodeProperties) {
        String defaultPath = qrCodeProperties.getFont().getDefaultFont();
        Font defaultFont = new Font("宋体", Font.PLAIN, 18); // 服务器不一定有这个字体
        if (defaultPath.startsWith("classpath:")) {
            log.info("加载默认字体文件: {}", defaultPath);
            defaultPath = StringUtils.removeStart(defaultPath, "classpath:");
            try (InputStream fontStream = getClass().getClassLoader().getResourceAsStream(defaultPath)) {
                assert fontStream != null;
                Font baseFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                defaultFont = baseFont.deriveFont(Font.PLAIN);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(defaultFont);
            } catch (IOException | FontFormatException e) {
                log.error("加载字体失败", e);
            }
        } else {
            File file = new File(defaultPath);
            if (file.exists()) {
                log.error("字体文件不存在: {}", defaultPath);
                return null;
            }
            try {
                Font baseFont = Font.createFont(Font.TRUETYPE_FONT, Files.newInputStream(file.toPath()));
                defaultFont = baseFont.deriveFont(Font.PLAIN);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(defaultFont);
            } catch (IOException | FontFormatException e) {
                log.error("加载字体失败", e);
            }
        }
        return defaultFont;
    }

    @Bean
    public Font miSansMediumFont(QRCodeProperties qrCodeProperties) {
        log.info("加载 MiSans-Medium 字体文件: {}", qrCodeProperties.getFont().getMiSansMediumFont());
        QRCodeFontProperties font = qrCodeProperties.getFont();
        String path = StringUtils.removeStart(font.getMiSansMediumFont(), "classpath:");
        return deduceFont(path);
    }

    @Bean
    public Font miSansNormalFont(QRCodeProperties qrCodeProperties) {
        log.info("加载 MiSans-Normal 字体文件: {}", qrCodeProperties.getFont().getMiSansNormalFont());
        QRCodeFontProperties font = qrCodeProperties.getFont();
        String path = StringUtils.removeStart(font.getMiSansNormalFont(), "classpath:");
        return deduceFont(path);
    }

    private Font deduceFont(String path) {
        try (InputStream fontStream = getClass().getClassLoader().getResourceAsStream(path)) {
            assert fontStream != null;
            Font baseFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            baseFont.deriveFont(Font.PLAIN);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(baseFont);
            return baseFont;
        } catch (IOException | FontFormatException e) {
            log.error("加载字体失败", e);
            return new Font("宋体", Font.PLAIN, 18);
        }
    }

    /**
     * Can Display Chinese
     * <p>
     *
     * @author weasley
     */
    @Slf4j
    public static class CanDisplayChinese {

        /**
         * 打印所有支持的中文字体
         */
        public static void canDisplay() {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            String[] fontNames = ge.getAvailableFontFamilyNames();
            log.warn("打印所有支持的中文字体");
            for (String fontName : fontNames) {
                Font font = new Font(fontName, Font.PLAIN, 12);
                // 判断是否支持中文字符
                if (font.canDisplay('\u4e00')) {
                    log.warn("{}", fontName);
                }
            }
        }
    }
}
