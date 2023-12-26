package com.example.qrcode.core;

import com.example.qrcode.entity.QRCodeExtraData;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.dromara.hutool.core.util.SystemUtil;
import org.dromara.hutool.extra.spring.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * QRCode Generator
 *
 * @author liuwenjing
 * @since 2023/12/26
 */
public class QRCodeGenerator {
    private static final String UTF_8 = StandardCharsets.UTF_8.displayName();
    private static final Logger log = LoggerFactory.getLogger(QRCodeGenerator.class);

    /**
     * Generate QRCode
     */
    public static void generateQRCode(String text, String filepath, int width, int height) throws WriterException, IOException {
        Map<EncodeHintType, Object> hintMap = new LinkedHashMap<>();
        hintMap.put(EncodeHintType.CHARACTER_SET, UTF_8);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hintMap);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        processQRCode(width, height, bitMatrix, image);

        File qrFile = new File(filepath);
        if (qrFile.exists()) {
            FileUtils.forceDelete(qrFile);
        }
        ImageIO.write(image, "png", qrFile);
        if (qrFile.exists()) {
            log.info("Generate QRCode success: {}", filepath);
        }
    }

    /**
     * Generate QRCode
     */
    public static BufferedImage generateQRCodeImage(String text, int width, int height) throws WriterException {
        Map<EncodeHintType, Object> hintMap = new LinkedHashMap<>();
        hintMap.put(EncodeHintType.CHARACTER_SET, UTF_8);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hintMap);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        processQRCode(width, height, bitMatrix, image);
        return image;
    }

    /**
     * 生成圆角图片
     *
     * @param filepath        图片路径, 如：path/to/qrCode.png
     * @param cornerRadius    圆角半径, 如: 20
     * @param withWhiteBorder 图片外部是否需要填充白色边框
     * @return 圆角图片路径
     * @throws IOException IOException
     * @implNote 圆角图片外部没有一圈白色边框
     */
    public static String makeRoundedCorner(String filepath, int cornerRadius, boolean withWhiteBorder) throws IOException {
        String targetFilepath = makeRoundedCornerWithoutWhiteBorder(filepath, cornerRadius);
        if (withWhiteBorder) {
            return makeRoundedCornerWithWhiteBorder(targetFilepath, cornerRadius);
        }
        return targetFilepath;
    }

    /**
     * 生成圆角图片
     *
     * @param filepath     图片路径, 如：path/to/qrCode.png
     * @param cornerRadius 圆角半径, 如: 20
     * @return 圆角图片路径
     * @throws IOException IOException
     * @implNote 圆角图片外部没有一圈白色边框
     */
    private static String makeRoundedCornerWithoutWhiteBorder(String filepath, int cornerRadius) throws IOException {
        String outputPath = getFileOutputPath(filepath);
        File outputPathFile = new File(outputPath);
        BufferedImage originalImage;
        if (filepath.startsWith("classpath:")) {
            String resourcePath = filepath.substring("classpath:".length());
            originalImage = ImageIO.read(Objects.requireNonNull(QRCodeGenerator.class.getClassLoader().getResourceAsStream(resourcePath)));
        } else {
            originalImage = ImageIO.read(new File(filepath));
        }
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        BufferedImage roundedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = roundedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON); // 抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // 画圆角
        g2d.setColor(Color.WHITE);
        g2d.fill(new RoundRectangle2D.Double(0, 0, width, height, cornerRadius, cornerRadius));
        g2d.setComposite(AlphaComposite.SrcAtop);
        g2d.drawImage(originalImage, 0, 0, null);

        g2d.dispose();
        ImageIO.write(roundedImage, "PNG", outputPathFile);
        return outputPath;
    }

    /**
     * 生成圆角图片
     *
     * @param filepath     图片路径, 如：path/to/qrCode.png
     * @param cornerRadius 圆角半径, 如: 20
     * @return 圆角图片路径
     * @throws IOException IOException
     * @implNote 圆角图片外部添加一圈白色边框
     */
    public static String makeRoundedCornerWithWhiteBorder(String filepath, int cornerRadius) throws IOException {
        File inputFile = new File(filepath);
        if (!inputFile.exists()) {
            log.error("File not found: {}", filepath);
            return null;
        }
        String outputPath = getFileOutputPath(filepath);
        File outputPathFile = new File(outputPath);
        if (!outputPathFile.exists()) {
            outputPathFile = inputFile;
            outputPath = filepath;
        }
        BufferedImage originalImage = ImageIO.read(outputPathFile);

        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        // 添加外边框
        int borderWidth = 10; // 定义边框宽度
        int borderedWidth = width + 2 * borderWidth;
        int borderedHeight = height + 2 * borderWidth;

        BufferedImage borderedImage = new BufferedImage(borderedWidth, borderedHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = borderedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON); // 抗锯齿
        // 填充白色背景
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, borderedWidth, borderedHeight);

        // 画圆角
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(originalImage, borderWidth, borderWidth, null);

        // 绘制圆角边框
        g2d.setColor(Color.WHITE); // 这里改为黑色边框，你也可以根据需要修改边框颜色
        g2d.setStroke(new BasicStroke(4)); // 定义边框粗细
        g2d.draw(new RoundRectangle2D.Double(1, 1, borderedWidth - 3, borderedHeight - 3, cornerRadius, cornerRadius)); // 调整边框位置和大小

        g2d.dispose();
        ImageIO.write(borderedImage, "PNG", outputPathFile);
        return outputPath;
    }

    /**
     * Generate QRCode with icon
     *
     * @param content  二维码内容, 如：https://www.newlink.com
     * @param size     二维码尺寸, 如：300
     * @param filePath 二维码文件路径, 如：./qr-code/qr_code_pic_demo.png
     * @param iconPath icon文件路径, 如：./qr-code/icon_right_angle_1280x1280.png
     */
    public static void generateQRCodeWithIcon(String content, int size, String filePath, String iconPath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                FileUtils.forceDelete(file);
            }
            Map<EncodeHintType, Object> hints = new LinkedHashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, UTF_8);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 2);
            BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints);

            BufferedImage qrImage = toBufferedImage(bitMatrix);

            Graphics2D g2d = qrImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON); // 抗锯齿

            Image logoImage = ImageIO.read(new File(iconPath));
            int logoXOrY = (size - 80) / 2; // Assume logo size as 80x80
            g2d.drawImage(logoImage, logoXOrY, logoXOrY, 80, 80, null);
            g2d.dispose();

            ImageIO.write(qrImage, "png", file);
            log.info("QR Code with icon created successfully. file: {}", filePath);
        } catch (Exception e) {
            log.error("Could not generate QR Code: {}", e.getMessage(), e);
        }
    }

    /**
     * 生成二维码额外的信息
     *
     * @param text      二维码内容
     * @param size      二维码尺寸
     * @param iconPath  二维码图标路径
     * @param filepath  生成的二维码图片路径
     * @param extraData 二维码额外的信息
     * @return 二维码图片路径
     */
    public static String generateQRCodeWithExtraData(String text, int size, String iconPath, String filepath, QRCodeExtraData extraData) {
        String qrCodeText = text.replace("${GunCode}", extraData.getGunCode());
        try {
            String roundedIconPath = makeRoundedCorner(iconPath, 30, true);
            generateQRCodeWithIcon(qrCodeText, size, filepath, roundedIconPath);

            boolean hasTopText = StringUtils.isNoneBlank(extraData.getStationName());
            boolean hasBottomText = StringUtils.isNoneBlank(extraData.getGunCode());
            // 添加文字到二维码图片顶部
            if (hasTopText) {
                filepath = expandQRCodeTop(extraData.getStationName(), filepath, filepath);
            }
            if (hasBottomText) {
                File input = new File(filepath);
                BufferedImage qrImage = ImageIO.read(input);
                // 扩展二维码底部高度
                int bottomExtension = 100; // 扩展高度为100像素
                BufferedImage bottomImage = new BufferedImage(qrImage.getWidth(), qrImage.getHeight() + bottomExtension, BufferedImage.TYPE_INT_RGB);
                Graphics2D bottomGraphics = bottomImage.createGraphics();
                bottomGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON); // 抗锯齿
                bottomGraphics.setColor(Color.WHITE); // 填充白色底部区域
                bottomGraphics.fillRect(0, qrImage.getHeight(), qrImage.getWidth(), bottomExtension); // 扩展的白色区域
                bottomGraphics.drawImage(qrImage, 0, 0, null); // 将原有的二维码绘制在扩展后的图像上
                bottomGraphics.dispose();

                // 在扩展的底部区域添加文字（右对齐）
                Graphics2D bottomGraphicsWithText = bottomImage.createGraphics();
                bottomGraphicsWithText.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON); // 抗锯齿
                bottomGraphicsWithText.setColor(Color.BLACK); // 设置文字颜色
                Font miSansMediumFont = SpringUtil.getBean("miSansMediumFont", Font.class);
                Font font = miSansMediumFont.deriveFont(Font.PLAIN, 18); // 字体和大小
                bottomGraphicsWithText.setFont(font);
                // 额外的文字
                java.util.List<String> additionalTexts = new ArrayList<>();
                additionalTexts.add("所处地区: " + StringUtils.defaultIfBlank(extraData.getArea(), ""));
                additionalTexts.add("桩名称: " + StringUtils.defaultIfBlank(extraData.getPileName(), ""));
                additionalTexts.add("枪编号: " + StringUtils.defaultIfBlank(extraData.getGunNo(), ""));
                additionalTexts.add("枪码: " + StringUtils.defaultIfBlank(extraData.getGunCode(), ""));

                int lineHeight = 22; // 每行文字的高度
                int bottomTextY = qrImage.getHeight() + 20; // 在二维码底部留出一定的空间

                // 绘制底部文字（左对齐）
                int x = 30; // 左对齐，留出一定边距
                for (String line : additionalTexts) {
                    bottomGraphicsWithText.drawString(line, x, bottomTextY);
                    bottomTextY += lineHeight; // 向下移动到下一行
                }
                ImageIO.write(bottomImage, "png", input);
            }
            log.info("QR Code with text created successfully. Filepath: {}", filepath);
            File fileRoundedIcon = new File(roundedIconPath);
            if (fileRoundedIcon.exists()) {
                fileRoundedIcon.delete();
            }
            return filepath;
        } catch (Exception e) {
            log.error("Could not generate QR Code，QR code data: {} ,{}", qrCodeText, e.getMessage(), e);
            return "";
        }
    }

    /**
     * 在二维码顶部添加额外的信息
     *
     * @param topAdditionalInfo 顶部额外的信息
     * @param qrCodePath        二维码图片路径
     * @param targetFilePath    生成的二维码图片路径
     * @return 生成的二维码图片路径
     */
    public static String expandQRCodeTop(String topAdditionalInfo, String qrCodePath, String targetFilePath) {
        if (StringUtils.isBlank(qrCodePath) || StringUtils.isBlank(targetFilePath) || StringUtils.isBlank(topAdditionalInfo)) {
            return targetFilePath;
        }
        try {
            File qrFile = new File(qrCodePath);
            BufferedImage qrImage = ImageIO.read(qrFile);
            // 扩展二维码顶部高度 30 像素
            int height = 30;
            int extendedHeight = qrImage.getHeight() + height;
            BufferedImage extendedImage = new BufferedImage(qrImage.getWidth(), extendedHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = extendedImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON); // 抗锯齿

            // 填充顶部区域
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, qrImage.getWidth(), height);

            // 在顶部区域添加额外的信息
            g2d.setColor(Color.BLACK);
            Font miSansMediumFont = SpringUtil.getBean("miSansMediumFont", Font.class); // 字体和大小
            Font font = miSansMediumFont.deriveFont(Font.BOLD, 22);
            g2d.setFont(font); // 字体和大小
            int textWidth = g2d.getFontMetrics().stringWidth(topAdditionalInfo);
            int x = (extendedImage.getWidth() - textWidth) / 2;
            int y = height - 5; // 调整位置，留出 25 像素间距
            g2d.drawString(new String(topAdditionalInfo.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8), x, y);

            // 将原始二维码图像放置在新图像的中间并重叠边缘
            int xPos = (extendedImage.getWidth() - qrImage.getWidth()) / 2;
            int yPos = height; // 起始位置在填充的顶部区域下方
            g2d.drawImage(qrImage, xPos, yPos, null);

            // 将扩展后的图片保存为文件
            File output = new File(targetFilePath);
            ImageIO.write(extendedImage, "png", output);
            g2d.dispose();
            if (output.exists()) {
                log.info("Generate QRCode success: {}", targetFilePath);
            }
            return targetFilePath;
        } catch (Exception e) {
            log.error("Could not generate QR Code: {}", e.getMessage(), e);
            return "";
        }
    }

    /**
     * 处理二维码
     */
    private static void processQRCode(int width, int height, BitMatrix bitMatrix, BufferedImage image) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int color = bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
                image.setRGB(x, y, color);
            }
        }
    }

    /**
     * 获取文件输出路径
     *
     * @param filepath 源文件路径
     * @return 文件输出路径
     */
    private static String getFileOutputPath(String filepath) {
        String filename = FilenameUtils.getName(filepath);
        String baseName = FilenameUtils.getBaseName(filepath);
        String extension = FilenameUtils.getExtension(filepath);
        log.info("获取文件输出路径 baseName: {}, extension: {}, filename: {}", baseName, extension, filename);
        String tempDir = SystemUtil.getTmpDirPath();
        if (filepath.startsWith("classpath:")) {
            return tempDir + StringUtils.substringAfterLast(filepath, "/");
        }
        if (filepath.endsWith("_rounded.png")) {
            return filepath;
        }
        return tempDir + baseName + "_rounded." + extension;
    }

    /**
     * 生成二维码
     *
     * @param matrix 二维码矩阵
     * @return 二维码图片
     */
    private static BufferedImage toBufferedImage(BitMatrix matrix) {
        int black = 0xFF000000;
        int white = 0xFFFFFFFF;
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? black : white);
            }
        }
        return image;
    }
}
