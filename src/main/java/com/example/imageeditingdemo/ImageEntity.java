package com.example.imageeditingdemo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ImageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String defectName;
    @Lob
    private byte[] newDefectImage;
    @Lob
    private byte[] harigamiDefectImage;
    @Lob
    private byte[] previousDefectImage;
    @Lob
    private byte[] repairedDefectImage;
}
