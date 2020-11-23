package practice.capston.domain.entity;

import lombok.Getter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


/*
* LAZY FETCH를 사용하지 않을 경우 필요하지 않는 경우에도 모든 데이터를 가져올 수 있으며 잘못 설계된 경우 무한루프에 진입됩니다.
* 실제로는 반드시 사용한 후 필요한 경우 proxy 로딩하는 방향으로 사용 해야 합니다.
*
* */
@Entity
@Table(name="MEMBER")
@Getter
public class Member {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String username;
    private String password;
    private String email;

    private int nowImageCount;
    private int maxImageCount;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "member")
    private List<Image> images = new ArrayList<>();

    protected Member () {    }

    public Member(String username, String password, String email) {
        this.username = username;
        this.email = email;
        this.password = password;

        this.maxImageCount = 30;
        this.nowImageCount = 0;
        this.role = Role.ROLE_USER;
    }

    public boolean possibleToStore() {
        int count = this.nowImageCount;

        if(count > this.maxImageCount) {
            throw new IllegalStateException("사용 가능한 용량을 넘었습니다.");
        }
        return true;
    }

    public void increaseImageCount() {
        this.nowImageCount++;
    }

    public void decreaseImageCount() {
        this.nowImageCount--;
    }

    public void changeImageUsage(int count) {
        this.maxImageCount += count;
    }
}
