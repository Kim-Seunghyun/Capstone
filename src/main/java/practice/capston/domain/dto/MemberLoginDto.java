package practice.capston.domain.dto;

import lombok.Data;

@Data
public class MemberLoginDto {
    private String username;
    private String password;
    private String email;
}
