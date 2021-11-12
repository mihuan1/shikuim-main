package cn.xyz.mianshi.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class QRUtil {

    // 默认二维码宽度
    private static final int WIDTH = 300;

    // 默认二维码高度
    private static final int HEIGHT = 300;

    // 默认二维码高度
    private static final String FORMAT = "png";

    // 二维码参数
    private static final Map<EncodeHintType, Object> HINTS = new HashMap<>();

    static {
        // 设置字符编码集
        HINTS.put(EncodeHintType.CHARACTER_SET, "utf-8");
        // 设置容错等级
        HINTS.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        // 设置图片边距
        HINTS.put(EncodeHintType.MARGIN, 2);
    }

    /**
     * 返回一个 BufferedImage 对象
     * @param content 二维码内容
     * @param width 宽
     * @param height 高
     */
    public static BufferedImage toBufferedImage(String content, int width, int height) throws WriterException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, HINTS);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    /**
     * 将二维码输入到一个流中
     * @param content 二维码内容
     * @param stream 输出流
     * @param width 宽
     * @param height 高
     */
    public static void writeToStream(String content, OutputStream stream, int width, int height) throws WriterException, IOException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, HINTS);
        MatrixToImageWriter.writeToStream(bitMatrix,FORMAT,stream);
    }

    /**
     * 生成二维码图片
     * @param content 二维码内容
     * @param path 文件保存路径
     * @param width 宽
     * @param height 高
     */
    public static void createQRCode(String content, String path, int width, int height) throws WriterException, IOException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, HINTS);
        MatrixToImageWriter.writeToPath(bitMatrix,FORMAT,new File(path).toPath());
    }
}