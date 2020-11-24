package practice.capston.domain.entity;

import lombok.Getter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "IMAGES")
@Getter
public class Image {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

    private String title;
    private String path;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "image", cascade = CascadeType.ALL)
    private List<TextContent> textContents = new ArrayList<>();

    protected Image() {}

    public Image(String title, Member member) {
        this.title = title;
        this.member = member;
        this.path = "/home/ubuntu/바탕화면/CapstonProject/capston/src/main/resources/static/user/";
        this.member.getImages().add(this);
    }
}
