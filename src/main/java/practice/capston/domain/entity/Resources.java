package practice.capston.domain.entity;

import lombok.Getter;

import javax.persistence.*;

/*
* 리소스를 테이블 연관관계를 갖지 않도록 설정했습니다.
* 관계를 갖고 있는게 더 안 좋아 보였습니다.
* 해당 테이블은 설정을 한 후 조회용으로만 사용되기 때문입니다.
* 이때 조회되는 방식은 Role 타입으로만 조회가 됩니다.
*
* 상당히 많이 조회 되는 테이블이기에... 변경이 될 수 있는 우려가 없으면 좋겠습니다.
* */

@Entity
@Table(name = "RESOURCES")
@Getter
public class Resources {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resource_id")
    private Long id;

    private String resourcePath;


    private String role;

    protected Resources() { }

    public Resources(String resourcePath, String role) {
        this.resourcePath = resourcePath;
        this.role = role;
    }
}
