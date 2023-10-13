package com.example.imageeditingdemo;

import lombok.Data;

@Data
public class ImageEntityDTO {
    private Long id;
    private String defectName;
    private byte[] newDefectImage;
    private byte[] harigamiDefectImage;
    private byte[] previousDefectImage;
    private byte[] repairedDefectImage;
}
