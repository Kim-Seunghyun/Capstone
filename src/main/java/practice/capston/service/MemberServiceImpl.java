package practice.capston.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practice.capston.domain.entity.Member;
import practice.capston.repository.MemberRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    @Override
    public Member joinMember(Member member) {
        Optional<Member> memberByUsername = memberRepository.findMemberByUsername(member.getUsername());
        if(memberByUsername.isPresent()){
            throw new IllegalStateException("이미 존재하는 회원 입니다.");
        }

        Member createdMember = memberRepository.save(member);
        return createdMember;
    }

    @Transactional
    @Override
    public int upgradeUsage(String username) {
        int upgradeCount = 30;

        Optional<Member> memberByEmail = memberRepository.findMemberByUsername(username);

        if(memberByEmail.isEmpty()){
            throw new IllegalStateException("MemberServiceImpl: 39");
        }

        Member member = memberByEmail.get();
        member.changeImageUsage(upgradeCount);
        return upgradeCount;

    }

    @Override
    public List<Member> findAllMembers() {
        List<Member> memberList = memberRepository.findAllBy();
        return memberList;
    }

    @Override
    public Member MemberWithImages(String username) {
        Optional<Member> memberWithImage = memberRepository.findMemberWithImage(username);
        if(memberWithImage.isEmpty()){
            if(memberWithImage.isEmpty()){
                throw new IllegalStateException("log");
            }
        }
        return memberWithImage.get();
    }
}
