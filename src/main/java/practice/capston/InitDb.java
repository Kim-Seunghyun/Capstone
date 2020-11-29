package practice.capston;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import practice.capston.domain.entity.Member;
import practice.capston.domain.entity.Resources;
import practice.capston.repository.ResourceRepository;
import practice.capston.service.MemberService;
import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class InitDb {

    private final InitData initData;

    @PostConstruct
    public void init() {
        initData.initDb();
    }

    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitData {
        private final MemberService memberService;
        private final ResourceRepository resourceRepository;
        private final PasswordEncoder passwordEncoder;

        public void initDb() {
            String password = passwordEncoder.encode("password");
            Member member = new Member("admin", password, "admin@gmail.com", "ROLE_ADMIN");
            memberService.joinMember(member);
            Member member2 = new Member("brutowa13", password, "admin@gmail.com", "ROLE_USER");
            memberService.joinMember(member2);


            Resources userResource1 = new Resources("/myDirectory", "ROLE_USER");
            Resources userResource2 = new Resources("/myDirectory**", "ROLE_USER");
            Resources userResource3 = new Resources("/myDirectory/**", "ROLE_USER");

            Resources adminResource1 = new Resources("/adminPage", "ROLE_ADMIN");
            Resources adminResource2 = new Resources("/adminPage**", "ROLE_ADMIN");
            Resources adminResource3 = new Resources("/adminPage/**", "ROLE_ADMIN");

            resourceRepository.save(userResource1);
            resourceRepository.save(userResource2);
            resourceRepository.save(userResource3);
            resourceRepository.save(adminResource1);
            resourceRepository.save(adminResource2);
            resourceRepository.save(adminResource3);
        }
    }
}
