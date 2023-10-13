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
        // Yüklenen dosyayı bir BufferedImage'a dönüştürün
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        int targetWidth = 32;
        int targetHeight = 32;
        originalImage = resizeImage(originalImage, targetWidth, targetHeight);


        // Resmi siyah beyaza dönüştürün
        BufferedImage grayscaleImage = convertToGrayscale(originalImage);

        // Resmi kırmızı beyaza dönüştürün
        BufferedImage redWhiteImage = convertToRedWhite(grayscaleImage);
//
        // Resmi mavi beyaza dönüştürün
        BufferedImage blueWhiteImage = convertToBlueWhite(grayscaleImage);
//
        // Resmi sarı çerçeve içinde siyah beyaza dönüştürün
        BufferedImage yellowFramedImage = addYellowFrame(grayscaleImage);

        // BufferedImage'ı byte dizisine dönüştürün
        byte[] imageBytes = convertToBytes(grayscaleImage);
        byte[] imageBytesBlue = convertToBytes(blueWhiteImage);
        byte[] imageBytesRed = convertToBytes(redWhiteImage);
        byte[] imageBytesYellowBorder = convertToBytes(yellowFramedImage);

        // ImageEntity oluşturun ve veritabanına kaydedin
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

        // Eğer resim zaten hedef boyutta ise, orijinal resmi geri döndür
        if (originalWidth == targetWidth && originalHeight == targetHeight) {
            return originalImage;
        }

        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, originalImage.getType());
        Graphics2D graphics = resizedImage.createGraphics();
        graphics.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics.dispose();

        return resizedImage;
    }
    // Siyah beyaza dönüştürme işlemi
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

        int redColor = 0xFF0000; // Kırmızı rengin RGB değeri
        int whiteColor = 0xFFFFFF; // Beyaz rengin RGB değeri

        int threshold = 128; // Eşik değeri, koyu ve açık bölgeleri ayırmak için kullanılır

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int grayValue = (grayscaleImage.getRGB(x, y) >> 16) & 0xFF;

                if (grayValue < threshold) {
                    // Koyu bölgeleri kırmızı yapın
                    redWhiteImage.setRGB(x, y, redColor);
                } else {
                    // Açık bölgeleri beyaz yapın
                    redWhiteImage.setRGB(x, y, whiteColor);
                }
            }
        }

        return redWhiteImage;
    }
//
private BufferedImage convertToBlueWhite(BufferedImage grayscaleImage) {
    int width = grayscaleImage.getWidth();
    int height = grayscaleImage.getHeight();
    BufferedImage blueWhiteImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    int blueColor = 0x0000FF; // Mavi rengin RGB değeri

    for (int x = 0; x < width; x++) {
        for (int y = 0; y < height; y++) {
            int grayValue = (grayscaleImage.getRGB(x, y) >> 16) & 0xFF;

            // Eşik değeri (örneğin, 128) kullanarak koyu ve açık bölgeleri ayırın
            int threshold = 128; // Eşik değeri
            if (grayValue < threshold) {
                // Koyu bölgeleri mavi yapın
                blueWhiteImage.setRGB(x, y, blueColor);
            } else {
                // Açık bölgeleri beyaz yapın
                blueWhiteImage.setRGB(x, y, 0xFFFFFF); // Beyaz rengin RGB değeri
            }
        }
    }

    return blueWhiteImage;
}
//
    // Sarı çerçeve ekleme işlemi
private BufferedImage addYellowFrame(BufferedImage grayscaleImage) {
    int width = grayscaleImage.getWidth();
    int height = grayscaleImage.getHeight();
    int frameSize = 4; // Çerçeve kalınlığı, istediğiniz boyuta göre ayarlayabilirsiniz

    BufferedImage framedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

    int yellowColor = 0xFFFF00; // Sarı rengin RGB değeri

    for (int x = 0; x < width; x++) {
        for (int y = 0; y < height; y++) {
            // Çerçeve kalınlığına göre sarı çerçeve eklemek için koşulları kontrol edin
            boolean isFrame = x < frameSize || x >= width - frameSize || y < frameSize || y >= height - frameSize;

            if (isFrame) {
                // Eğer piksel çerçeve bölgesindeyse, sarı rengi kullan
                framedImage.setRGB(x, y, yellowColor);
            } else {
                // Eğer piksel içerideyse, orijinal resmi kullan
                framedImage.setRGB(x, y, grayscaleImage.getRGB(x, y));
            }
        }
    }

    return framedImage;
}

    // BufferedImage'ı byte dizisine dönüştürme işlemi
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
