package com.frontend.utils;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Scanner;

public class FaceUploadUtil {

    public static boolean uploadFace(String deviceIp, int port, String username, String password, File originalImageFile, String fpid) throws IOException, ParseException {
        String url = String.format("http://%s:%d/ISAPI/Intelligent/FDLib/FaceDataRecord?format=json", deviceIp, port);

        File optimizedImageFile = createOptimizedFaceImage(originalImageFile);
        boolean success = sendMultipartRequest(url, optimizedImageFile, deviceIp, port, username, password, fpid);

        if (!success) {
            File smallerImageFile = createSmallerFaceImage(originalImageFile);
            success = sendMultipartRequest(url, smallerImageFile, deviceIp, port, username, password, fpid);
            if (smallerImageFile.exists()) smallerImageFile.delete();
        }

        if (optimizedImageFile.exists()) optimizedImageFile.delete();
        return success;
    }

    private static File createOptimizedFaceImage(File originalFile) throws IOException {
        BufferedImage originalImage = ImageIO.read(originalFile);
        int targetSize = 300;
        BufferedImage optimizedImage = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = optimizedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, targetSize, targetSize);
        g2d.drawImage(originalImage.getScaledInstance(targetSize, targetSize, Image.SCALE_SMOOTH), 0, 0, null);
        g2d.dispose();

        File outputFile = new File(System.getProperty("java.io.tmpdir"), "optimized_face.jpg");
        saveJPEG(optimizedImage, outputFile, 0.85f);
        return outputFile;
    }

    private static File createSmallerFaceImage(File originalFile) throws IOException {
        BufferedImage originalImage = ImageIO.read(originalFile);
        int targetSize = 200;
        BufferedImage smallImage = new BufferedImage(targetSize, targetSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = smallImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, targetSize, targetSize);
        g2d.drawImage(originalImage.getScaledInstance(targetSize, targetSize, Image.SCALE_SMOOTH), 0, 0, null);
        g2d.dispose();

        File smallFile = new File(System.getProperty("java.io.tmpdir"), "small_face.jpg");
        saveJPEG(smallImage, smallFile, 0.75f);
        return smallFile;
    }

    private static void saveJPEG(BufferedImage image, File file, float quality) throws IOException {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
        }
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(file)) {
            writer.setOutput(ios);
            writer.write(null, new javax.imageio.IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
    }

    private static boolean sendMultipartRequest(String url, File imageFile, String ip, int port,
                                                String username, String password, String fpid) throws IOException, ParseException {
        BasicCredentialsProvider creds = new BasicCredentialsProvider();
        creds.setCredentials(new AuthScope(ip, port), new UsernamePasswordCredentials(username, password.toCharArray()));

        try (CloseableHttpClient client = HttpClients.custom().setDefaultCredentialsProvider(creds).build()) {
            HttpPost post = new HttpPost(url);
            String boundary = "----FormBoundary" + System.currentTimeMillis();
            byte[] body = createMultipartBody(boundary, imageFile, fpid);
            post.setEntity(new ByteArrayEntity(body, ContentType.create("multipart/form-data")));
            post.setHeader("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (CloseableHttpResponse res = client.execute(post)) {
                int code = res.getCode();
                String bodyStr = res.getEntity() != null ? EntityUtils.toString(res.getEntity(), StandardCharsets.UTF_8) : "";
                System.out.println("HTTP Status: " + code);
                System.out.println("Response: " + bodyStr);
                return code == 200;
            }
        }
    }

    private static byte[] createMultipartBody(String boundary, File img, String fpid) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true);

        String json = "{\"faceLibType\":\"blackFD\",\"FDID\":\"1\",\"FPID\":\"" + fpid + "\"}";
        writer.printf("--%s\r\n", boundary);
        writer.printf("Content-Disposition: form-data; name=\"FaceDataRecord\"\r\n");
        writer.printf("Content-Type: application/json\r\n\r\n");
        writer.print(json);
        writer.printf("\r\n--%s\r\n", boundary);
        writer.printf("Content-Disposition: form-data; name=\"FaceImage\"; filename=\"%s\"\r\n", img.getName());
        writer.printf("Content-Type: image/jpeg\r\n\r\n");
        writer.flush();

        out.write(Files.readAllBytes(img.toPath()));
        writer.printf("\r\n--%s--\r\n", boundary);
        writer.flush();

        return out.toByteArray();
    }
}
