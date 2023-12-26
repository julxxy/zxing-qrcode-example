package com.example.qrcode.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 二维码额外的信息
 *
 * @author weasley
 */
@Getter
@Setter
@Accessors(chain = true)
public class QRCodeExtraData implements Serializable {
    /**
     * 二维码标题长度, 超过就显示省略号 ...
     */
    private static final int QR_CODE_TITLE_LENGTH = 12;
    /**
     * 未定义的默认值
     */
    private static final String UNDEFINED = "NAN";

    /**
     * 站的名称，e.g: 奥迪充电站杭州城西银泰
     */
    private String stationName;
    /**
     * 所处地区，e.g: 杭州市-拱墅区
     */
    private String area;
    /**
     * 桩名称，e.g: 101号直流
     */
    private String pileName;
    /**
     * 枪编号，e.g: 3301050069101
     */
    private String gunNo;
    /**
     * 枪码，e.g: 202309-330102-100014-1003-01
     */
    private String gunCode;
    /**
     * 二维码文件名，动态赋值
     *
     * @apiNote 码命名规则: 站名_桩名_枪名_统一枪码
     */
    private String fileName;

    /**
     * 获取二维码额外信息实例
     */
    public static QRCodeExtraData getInstance() {
        return new QRCodeExtraData();
    }

    /**
     * 获取默认的二维码额外信息
     *
     * @apiNote 用于测试
     */
    public static QRCodeExtraData getTestInstance() {
        QRCodeExtraData extraData = new QRCodeExtraData();
        extraData.setStationName("奥迪充电站杭州城西银泰");
        extraData.setArea("杭州市-拱墅区");
        extraData.setPileName("101号直流");
        extraData.setGunNo("3301050069101");
        extraData.setGunCode("202309-330102-100014-1003-01");
        return extraData;
    }

    public String getFileName() {
        String suffix = ".png";
        if (fileName != null && !fileName.isEmpty()) {
            return fileName + suffix;
        }
        String defaultFileName;
        String station = (stationName != null && !stationName.isEmpty()) ? stationName : UNDEFINED;
        String pile = (pileName != null && !pileName.isEmpty()) ? pileName : UNDEFINED;
        String gun = (gunNo != null && !gunNo.isEmpty()) ? gunNo : UNDEFINED;
        String code = (gunCode != null && !gunCode.isEmpty()) ? gunCode : UNDEFINED;
        defaultFileName = station + "_" + pile + "_" + gun + "_" + code;
        return defaultFileName + suffix;
    }

    public String getStationName() {
        if (stationName != null && stationName.length() > QR_CODE_TITLE_LENGTH) {
            return stationName.substring(0, QR_CODE_TITLE_LENGTH) + "...";
        }
        return stationName;
    }
}
