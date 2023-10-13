package com.example.imageeditingdemo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ImageService {
    private final ImageRepository imageRepository;

    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    public void processAndSaveImage(String imageDefectName, MultipartFile file) throws IOException {
        // Convert the uploaded file to a BufferedImage
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        int targetWidth = 32;
        int targetHeight = 32;
        originalImage = resizeImage(originalImage, targetWidth, targetHeight);


        // Convert the image to black and white
        BufferedImage grayscaleImage = convertToGrayscale(originalImage);


        // Convert the image to red and white
        BufferedImage redWhiteImage = convertToRedWhite(grayscaleImage);

        // Convert the image to blue and white
        BufferedImage blueWhiteImage = convertToBlueWhite(grayscaleImage);

        // Convert image to black and white in yellow frame
        BufferedImage yellowFramedImage = addYellowFrame(grayscaleImage);

        // Convert BufferedImage to byte array
        byte[] imageBytes = convertToBytes(grayscaleImage);
        byte[] imageBytesBlue = convertToBytes(blueWhiteImage);
        byte[] imageBytesRed = convertToBytes(redWhiteImage);
        byte[] imageBytesYellowBorder = convertToBytes(yellowFramedImage);


        //Create ImageEntity and save it to database
        ImageEntity imageEntity = new ImageEntity();
        imageEntity.setDefectName(imageDefectName);
        imageEntity.setNewDefectImage(imageBytesRed);
        imageEntity.setPreviousDefectImage(imageBytes);
        imageEntity.setRepairedDefectImage(imageBytesYellowBorder);
        imageEntity.setHarigamiDefectImage(imageBytesBlue);

        imageRepository.save(imageEntity);
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        // If the image is already at the target size, return the original image
        if (originalWidth == targetWidth && originalHeight == targetHeight) {
            return originalImage;
        }

        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, originalImage.getType());
        Graphics2D graphics = resizedImage.createGraphics();
        graphics.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics.dispose();

        return resizedImage;
    }

    //Converting to black and white
    private BufferedImage convertToGrayscale(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage grayscaleImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                int grayValue = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                int grayRgb = (grayValue << 16) | (grayValue << 8) | grayValue;

                grayscaleImage.setRGB(x, y, grayRgb);
            }
        }

        return grayscaleImage;
    }

    private BufferedImage convertToRedWhite(BufferedImage grayscaleImage) {
        int width = grayscaleImage.getWidth();
        int height = grayscaleImage.getHeight();
        BufferedImage redWhiteImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int redColor = 0xFF0000;
        int whiteColor = 0xFFFFFF;

        int threshold = 128; // Threshold value is used to separate dark and light areasr

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int grayValue = (grayscaleImage.getRGB(x, y) >> 16) & 0xFF;

                if (grayValue < threshold) {
                    // Make dark areas red
                    redWhiteImage.setRGB(x, y, redColor);
                } else {
                    // Make light areas white
                    redWhiteImage.setRGB(x, y, whiteColor);
                }
            }
        }

        return redWhiteImage;
    }

private BufferedImage convertToBlueWhite(BufferedImage grayscaleImage) {
    int width = grayscaleImage.getWidth();
    int height = grayscaleImage.getHeight();
    BufferedImage blueWhiteImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    int blueColor = 0x0000FF;

    for (int x = 0; x < width; x++) {
        for (int y = 0; y < height; y++) {
            int grayValue = (grayscaleImage.getRGB(x, y) >> 16) & 0xFF;

            // Separate dark and light areas using threshold value (e.g. 128)
            int threshold = 128;
            if (grayValue < threshold) {
                // Make dark areas blue
                blueWhiteImage.setRGB(x, y, blueColor);
            } else {
                // Make light areas white
                blueWhiteImage.setRGB(x, y, 0xFFFFFF);
            }
        }
    }

    return blueWhiteImage;
}
// Adding yellow frame
private BufferedImage addYellowFrame(BufferedImage grayscaleImage) {
    int width = grayscaleImage.getWidth();
    int height = grayscaleImage.getHeight();
    int frameSize = 4; //Frame thickness, you can adjust it to the size you want

    BufferedImage framedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    int yellowColor = 0xFFFF00;

    for (int x = 0; x < width; x++) {
        for (int y = 0; y < height; y++) {
            //Check conditions to add yellow frame based on frame thickness
            boolean isFrame = x < frameSize || x >= width - frameSize || y < frameSize || y >= height - frameSize;

            if (isFrame) {
                // If you are in the pixel frame zone, use yellow color
                framedImage.setRGB(x, y, yellowColor);
            } else {
                // If pixel is inside, use original image
                framedImage.setRGB(x, y, grayscaleImage.getRGB(x, y));
            }
        }
    }

    return framedImage;
}


    // Converting BufferedImage to byte array
    private byte[] convertToBytes(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }


    public ImageEntity getImageById(Long imageId) {
        ImageEntity imageEntity = imageRepository.findById(imageId).orElse(null);

        if (imageEntity != null) {
            return imageEntity;
        } else {
            return null;
        }
    }
}
