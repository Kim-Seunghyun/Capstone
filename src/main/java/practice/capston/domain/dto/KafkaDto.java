package practice.capston.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import practice.capston.domain.entity.Image;
import practice.capston.domain.entity.TextContent;

@Data
@AllArgsConstructor
public class KafkaDto {
    private Long imgId;
    private String imagePath;
    private String imageTitle;
    private Long textId;

    public KafkaDto(Image image, TextContent textContent) {
        this.imgId = image.getId();
        this.imagePath = image.getPath();
        this.imageTitle = image.getTitle();
        this.textId = textContent.getId();
    }
}
