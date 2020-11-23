package practice.capston.service;

import practice.capston.domain.entity.Member;

import java.util.List;

public interface MemberService {
    Member joinMember(Member member);
    int upgradeUsage(String username);
    List<Member> findAllMembers();
    Member MemberWithImages(String username);
}
