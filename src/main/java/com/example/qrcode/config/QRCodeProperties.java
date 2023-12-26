package com.example.qrcode.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * 充电设备枪二维码配置
 *
 * @author weasley
 * @version 1.0.0
 */
@Setter
@Getter
@RefreshScope
@NoArgsConstructor
@ConfigurationProperties(prefix = "pile.connector.qr-code")
public class QRCodeProperties {
    /**
     * 二维码基础前缀文本，如：访问链接的BaseUrl, https://yjzz.fzggw.zj.gov.cn/zlc/qrcode?code=${GunCode}
     */
    private String baseText = "https://yjzz.fzggw.zj.gov.cn/zlc/qrcode?code=${GunCode}";
    /**
     * 二维码图标路径, e.g: classpath:qr-code-icon/icon_right_angle_1280x1280.png
     */
    private String iconPath = "classpath:qr-code-icon/icon_right_angle_1280x1280.png";
    /**
     * 二维码宽度
     */
    private Integer width = 300;
    /**
     * 二维码高度
     */
    private Integer height = 300;
    /**
     * 是否生成测试二维码图片检查是否支持中文
     */
    private Boolean test = false;
    /**
     * 二维码图片的字体配置
     *
     * @apiNote 可以不配置
     */
    private QRCodeFontProperties font = new QRCodeFontProperties();

    /**
     * 二维码图片的字体配置
     */
    @Getter
    @Setter
    public static class QRCodeFontProperties {
        /**
         * 默认字体路径
         */
        private static final String DEFAULT_FONT_PATH = "classpath:fonts/MiSans-Medium.ttf";
        /**
         * 默认字体路径
         */
        private String defaultFont = DEFAULT_FONT_PATH;
        /**
         * MiSans-Medium 字体路径
         */
        private String miSansMediumFont = DEFAULT_FONT_PATH;
        /**
         * MiSans-Normal 字体路径
         */
        private String miSansNormalFont = "classpath:fonts/MiSans-Normal.ttf";
    }
}
