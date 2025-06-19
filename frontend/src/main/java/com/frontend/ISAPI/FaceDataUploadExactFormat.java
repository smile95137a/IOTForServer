package com.frontend.ISAPI;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;

public class FaceDataUploadExactFormat {

    private static final String DEVICE_IP = "192.168.1.111";
    private static final int PORT = 80;
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "Handsome0202@";
    private static final String IMAGE_PATH = "C:\\Users\\user\\Desktop\\S__9576589.jpg";

    public static void main(String[] args) {
        FaceDataUploadExactFormat client = new FaceDataUploadExactFormat();
        try {
            client.uploadFaceData();
        } catch (Exception e) {
            System.err.println("上傳失敗: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void uploadFaceData() throws IOException, ParseException {
        String url = String.format("http://%s:%d/ISAPI/Intelligent/FDLib/FaceDataRecord?format=json",
                DEVICE_IP, PORT);

        // 驗證檔案是否存在
        File originalImageFile = new File(IMAGE_PATH);
        if (!originalImageFile.exists()) {
            throw new IllegalArgumentException("圖片檔案不存在: " + IMAGE_PATH);
        }

        System.out.println("=== 改進的人臉圖片處理 ===");

        // 創建高質量的人臉圖片
        File optimizedImageFile = createOptimizedFaceImage(originalImageFile);

        System.out.println("嘗試上傳優化後的圖片...");
        boolean success = sendMultipartRequest(url, optimizedImageFile);

        if (!success) {
            System.out.println("\n嘗試更小尺寸的圖片...");
            File smallerImageFile = createSmallerFaceImage(originalImageFile);
            success = sendMultipartRequest(url, smallerImageFile);

            if (smallerImageFile.exists()) {
                smallerImageFile.delete();
            }
        }

        // 清理臨時檔案
        if (optimizedImageFile.exists()) {
            optimizedImageFile.delete();
        }

        if (success) {
            System.out.println("✅ 人臉資料上傳成功！");
        } else {
            System.err.println("❌ 所有嘗試都失敗了");
        }
    }

    private File createOptimizedFaceImage(File originalFile) throws IOException {
        BufferedImage originalImage = ImageIO.read(originalFile);

        System.out.println("原始圖片: " + originalImage.getWidth() + "x" + originalImage.getHeight());
        System.out.println("原始大小: " + originalFile.length() + " bytes");

        // 人臉識別建議的最佳尺寸：200x200 到 400x400
        int targetSize = 300;

        // 創建高質量的調整大小圖片
        BufferedImage optimizedImage = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = optimizedImage.createGraphics();

        // 設置高質量渲染提示
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 填充白色背景
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, targetSize, targetSize);

        // 繪製調整大小的圖片
        Image scaledImage = originalImage.getScaledInstance(targetSize, targetSize, Image.SCALE_SMOOTH);
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();

        // 保存為高質量 JPEG
        File optimizedFile = new File(System.getProperty("java.io.tmpdir"), "optimized_face.jpg");
        saveHighQualityJPEG(optimizedImage, optimizedFile, 0.85f); // 85% 質量

        System.out.println("優化後圖片: " + targetSize + "x" + targetSize);
        System.out.println("優化後大小: " + optimizedFile.length() + " bytes");

        return optimizedFile;
    }

    private File createSmallerFaceImage(File originalFile) throws IOException {
        BufferedImage originalImage = ImageIO.read(originalFile);

        // 創建更小的圖片 (200x200)
        int targetSize = 200;
        BufferedImage smallImage = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = smallImage.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, targetSize, targetSize);

        Image scaledImage = originalImage.getScaledInstance(targetSize, targetSize, Image.SCALE_SMOOTH);
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();

        File smallFile = new File(System.getProperty("java.io.tmpdir"), "small_face.jpg");
        saveHighQualityJPEG(smallImage, smallFile, 0.75f); // 75% 質量

        System.out.println("小尺寸圖片: " + targetSize + "x" + targetSize);
        System.out.println("小尺寸大小: " + smallFile.length() + " bytes");

        return smallFile;
    }

    private void saveHighQualityJPEG(BufferedImage image, File outputFile, float quality) throws IOException {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();

        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
        }

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile)) {
            writer.setOutput(ios);
            writer.write(null, new javax.imageio.IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
    }

    private boolean sendMultipartRequest(String url, File imageFile) throws IOException, ParseException {
        // 設定認證
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        AuthScope authScope = new AuthScope(DEVICE_IP, PORT);
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(USERNAME, PASSWORD.toCharArray());
        credentialsProvider.setCredentials(authScope, credentials);

        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCredentialsProvider(credentialsProvider)
                .build()) {

            HttpPost httpPost = new HttpPost(url);

            String boundary = "----FormBoundary" + System.currentTimeMillis();
            byte[] multipartBody = createMultipartBody(boundary, imageFile);

            ByteArrayEntity entity = new ByteArrayEntity(multipartBody,
                    ContentType.create("multipart/form-data"));
            httpPost.setEntity(entity);

            // 設定 headers
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("User-Agent", "FaceUploadClient/1.0");
            httpPost.setHeader("Content-Type", "multipart/form-data; boundary=" + boundary);

            System.out.println("發送請求...");
            System.out.println("圖片檔案: " + imageFile.getName() + " (" + imageFile.length() + " bytes)");

            // 執行請求
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getCode();
                String responseBody = "";

                if (response.getEntity() != null) {
                    responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                }

                System.out.println("HTTP Status: " + statusCode);
                System.out.println("Response: " + responseBody);

                if (statusCode == 200) {
                    return true;
                } else {
                    System.err.println("請求失敗，狀態碼: " + statusCode);

                    // 分析具體的錯誤
                    if (responseBody.contains("SubpicAnalysisModelingError")) {
                        System.err.println("提示：圖片中的人臉可能無法正確識別，請嘗試：");
                        System.err.println("  1. 使用清晰的正面人臉照片");
                        System.err.println("  2. 確保人臉占圖片的主要部分");
                        System.err.println("  3. 光線充足，無陰影遮擋");
                        System.err.println("  4. 避免側臉或角度過大的照片");
                    }

                    return false;
                }
            }
        }
    }

    private byte[] createMultipartBody(String boundary, File imageFile) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8), true);

        // JSON 部分
        String jsonData = "{\n" +
                "  \"faceLibType\": \"blackFD\",\n" +
                "  \"FDID\": \"1\",\n" +
                "  \"FPID\": \"TEST8812\"\n" +
                "}";

        writer.printf("--%s\r\n", boundary);
        writer.printf("Content-Disposition: form-data; name=\"FaceDataRecord\"\r\n");
        writer.printf("Content-Type: application/json\r\n");
        writer.printf("\r\n");
        writer.flush();

        baos.write(jsonData.getBytes(StandardCharsets.UTF_8));
        writer.printf("\r\n");

        // 圖片部分
        byte[] imageBytes = Files.readAllBytes(imageFile.toPath());

        writer.printf("--%s\r\n", boundary);
        writer.printf("Content-Disposition: form-data; name=\"FaceImage\"; filename=\"%s\"\r\n", imageFile.getName());
        writer.printf("Content-Type: image/jpeg\r\n");
        writer.printf("\r\n");
        writer.flush();

        baos.write(imageBytes);
        writer.printf("\r\n");

        // 結束邊界
        writer.printf("--%s--\r\n", boundary);
        writer.flush();

        return baos.toByteArray();
    }
}