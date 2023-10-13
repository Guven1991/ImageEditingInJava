package com.example.imageeditingdemo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {
    private final ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("imageDefectName") String imageDefectName, @RequestParam("file") MultipartFile file) {
        try {
            imageService.processAndSaveImage(imageDefectName,file);
            return ResponseEntity.ok("The image was saved successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while saving the image: " + e.getMessage());
        }
    }

    @GetMapping("/{imageId}")
    public ResponseEntity<ImageEntity> getImageById(@PathVariable Long imageId) {
        ImageEntity imageEntity = imageService.getImageById(imageId);

        if (imageEntity != null) {
            return ResponseEntity.ok().body(imageEntity);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
