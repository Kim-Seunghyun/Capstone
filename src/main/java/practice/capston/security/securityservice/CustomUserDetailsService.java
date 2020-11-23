package practice.capston.security.securityservice;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practice.capston.domain.entity.Member;
import practice.capston.repository.MemberRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Member> findMember = memberRepository.findMemberByUsername(username);

        if(findMember.isEmpty()){
            throw new UsernameNotFoundException("CustomUserDetailsService: 24");
        }

        List<String> roleList = new ArrayList<>();
        roleList.add(findMember.get().getRole());
        List<SimpleGrantedAuthority> collect = roleList.stream().map(r -> new SimpleGrantedAuthority(r)).collect(Collectors.toList());

        MemberContext memberContext = new MemberContext(findMember.get(), collect);

        return memberContext;
    }
}
