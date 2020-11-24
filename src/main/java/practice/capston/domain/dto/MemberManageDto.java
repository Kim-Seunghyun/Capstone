package practice.capston.domain.dto;

import lombok.Data;
import practice.capston.domain.entity.Member;

@Data
public class MemberManageDto {
    private String username;
    private String email;
    private int nowCount;
    private int maxCount;

    public MemberManageDto(Member member) {
        this.username = member.getUsername();
        this.email = member.getEmail();
        this.nowCount = member.getNowImageCount();
        this.maxCount = member.getMaxImageCount();
    }
}
