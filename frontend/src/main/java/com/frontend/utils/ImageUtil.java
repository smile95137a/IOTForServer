package com.frontend.utils;

import jakarta.annotation.PostConstruct;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class ImageUtil {

    @Value("${pictureFile.path}")
    private String picturePath;

    @Value("${pictureFile.path-mapping}")
    private String picturePathMapping;

    private static String staticPicturePath;
    private static String staticPicturePathMapping;
    private static final int TARGET_SIZE = 1600;
    private static final int STORE_TARGET_SIZE = 2000;
    private static final int RECT_WIDTH = 800;
    private static final int STORE_TARGET_WIDTH = 380;
    private static final int RECT_HEIGHT = 600;
    private static final int STORE_TARGET_HEIGHT  = 216;
    private static final float OUTPUT_QUALITY = 0.85f;
    private static final float STORE_OUTPUT_QUALITY = 0.85f;

    @PostConstruct
    public void init() {
        staticPicturePath = picturePath.endsWith("/") ? picturePath : picturePath + "/";
        staticPicturePathMapping = picturePathMapping.endsWith("/") ? picturePathMapping : picturePathMapping + "/";
    }

    public static String upload(MultipartFile file) {
        return uploadFile(file, false);
    }

    public static String storeUpload(MultipartFile file) {
        return storeUploadFile(file, false);
    }

    public static String uploadForCKEditor(MultipartFile file) {
        return uploadFile(file, true);
    }

    public static String uploadRectangle(MultipartFile file) {
        return uploadRectangleFile(file);
    }

    private static String uploadFile(MultipartFile file, boolean isForCKEditor) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String originalFileName = file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID().toString() + "_" + (originalFileName != null ? originalFileName : "image");
        String fileExtension = getFileExtension(originalFileName);

        if (fileExtension == null) {
            throw new IllegalArgumentException("Invalid file extension");
        }

        String filePath = staticPicturePath + uniqueFileName;
        File dest = new File(filePath);

        try {
            Files.createDirectories(Paths.get(staticPicturePath));

            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                throw new IOException("Failed to read image file");
            }


                BufferedImage processedImage = processImageWithAspectRatio(originalImage);
                ImageIO.write(processedImage, fileExtension, dest);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("File upload failed", e);
        }

        return staticPicturePathMapping + uniqueFileName;
    }

    private static String storeUploadFile(MultipartFile file, boolean isForCKEditor) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String originalFileName = file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID().toString() + "_" + (originalFileName != null ? originalFileName : "image");
        String fileExtension = getFileExtension(originalFileName);

        if (fileExtension == null) {
            throw new IllegalArgumentException("Invalid file extension");
        }

        String filePath = staticPicturePath + uniqueFileName;
        File dest = new File(filePath);

        try {
            Files.createDirectories(Paths.get(staticPicturePath));

            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                throw new IOException("Failed to read image file");
            }


                BufferedImage processedImage = storeProcessImageWithAspectRatio(originalImage);
                ImageIO.write(processedImage, fileExtension, dest);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("File upload failed", e);
        }

        return staticPicturePathMapping + uniqueFileName;
    }

    private static BufferedImage storeProcessImageWithAspectRatio(BufferedImage originalImage) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // 定义目标尺寸
        final int TARGET_WIDTH = 400;
        final int TARGET_HEIGHT = 250;

        // 计算缩放比例
        double scale = Math.min(
                (double) TARGET_WIDTH / originalWidth,
                (double) TARGET_HEIGHT / originalHeight
        );

        int scaledWidth = (int) (originalWidth * scale);
        int scaledHeight = (int) (originalHeight * scale);

        BufferedImage finalImage = new BufferedImage(
                TARGET_WIDTH,
                TARGET_HEIGHT,
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g2d = finalImage.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, TARGET_WIDTH, TARGET_HEIGHT);

        try {
            BufferedImage scaledImage = Thumbnails.of(originalImage)
                    .size(scaledWidth, scaledHeight)
                    .outputQuality(OUTPUT_QUALITY)
                    .asBufferedImage();

            // 居中放置图片
            int x = (TARGET_WIDTH - scaledWidth) / 2;
            int y = (TARGET_HEIGHT - scaledHeight) / 2;

            g2d.drawImage(scaledImage, x, y, null);
        } catch (IOException e) {
            throw new RuntimeException("Failed to process image", e);
        } finally {
            g2d.dispose();
        }

        return finalImage;
    }



    private static String uploadRectangleFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String originalFileName = file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID().toString() + "_" + (originalFileName != null ? originalFileName : "rectangle_image");
        String fileExtension = getFileExtension(originalFileName);

        if (fileExtension == null) {
            throw new IllegalArgumentException("Invalid file extension");
        }

        String filePath = staticPicturePath + uniqueFileName;
        File dest = new File(filePath);

        try {
            Files.createDirectories(Paths.get(staticPicturePath));

            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                throw new IOException("Failed to read image file");
            }

            BufferedImage processedImage = processRectangleImageWithAspectRatio(originalImage);
            ImageIO.write(processedImage, fileExtension, dest);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Rectangle file upload failed", e);
        }

        return staticPicturePathMapping + uniqueFileName;
    }

    private static BufferedImage processImageWithAspectRatio(BufferedImage originalImage) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        double scale = Math.min(
                (double) TARGET_SIZE / originalWidth,
                (double) TARGET_SIZE / originalHeight
        );

        int scaledWidth = (int) (originalWidth * scale);
        int scaledHeight = (int) (originalHeight * scale);

        BufferedImage finalImage = new BufferedImage(
                TARGET_SIZE,
                TARGET_SIZE,
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g2d = finalImage.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, TARGET_SIZE, TARGET_SIZE);

        try {
            BufferedImage scaledImage = Thumbnails.of(originalImage)
                    .size(scaledWidth, scaledHeight)
                    .outputQuality(OUTPUT_QUALITY)
                    .asBufferedImage();

            int x = (TARGET_SIZE - scaledWidth) / 2;
            int y = (TARGET_SIZE - scaledHeight) / 2;

            g2d.drawImage(scaledImage, x, y, null);
        } catch (IOException e) {
            throw new RuntimeException("Failed to process image", e);
        } finally {
            g2d.dispose();
        }

        return finalImage;
    }

    private static BufferedImage processRectangleImageWithAspectRatio(BufferedImage originalImage) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        double scale = Math.min(
                (double) RECT_WIDTH / originalWidth,
                (double) RECT_HEIGHT / originalHeight
        );

        int scaledWidth = (int) (originalWidth * scale);
        int scaledHeight = (int) (originalHeight * scale);

        BufferedImage finalImage = new BufferedImage(
                RECT_WIDTH,
                RECT_HEIGHT,
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g2d = finalImage.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, RECT_WIDTH, RECT_HEIGHT);

        try {
            BufferedImage scaledImage = Thumbnails.of(originalImage)
                    .size(scaledWidth, scaledHeight)
                    .outputQuality(OUTPUT_QUALITY)
                    .asBufferedImage();

            int x = (RECT_WIDTH - scaledWidth) / 2;
            int y = (RECT_HEIGHT - scaledHeight) / 2;

            g2d.drawImage(scaledImage, x, y, null);
        } catch (IOException e) {
            throw new RuntimeException("Failed to process rectangle image", e);
        } finally {
            g2d.dispose();
        }

        return finalImage;
    }

    private static String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }
        return null;
    }
}
