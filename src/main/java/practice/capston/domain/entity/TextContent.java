package practice.capston.domain.entity;

import lombok.Getter;

import javax.persistence.*;

@Entity
@Table(name = "TEXT_CONTENT")
@Getter
public class TextContent {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "text_content_id")
    private Long id;

    private String resultText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Image image;

    protected TextContent() {}

    public TextContent(String resultText, Image image){
        this.resultText = resultText;
        this.image = image;
        image.getTextContents().add(this);
    }

    public void changeNewText(String text){
        this.resultText = text;
    }

}
