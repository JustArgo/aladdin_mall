package com.maiquan.aladdin_mall.util;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

/**
 * 二维码
 * 
 * @author JSC
 *
 */
public class QRCodeUtil {
	public static void encode(OutputStream os, String content) throws Exception {
		int width = 300;
		int height = 300;
		Map<EncodeHintType, Object> hints = new HashMap<EncodeHintType, Object>();
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);// 生成矩阵
		MatrixToImageWriter.writeToStream(matrix, "jpeg", os);
	}
}
