package practice.capston.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import practice.capston.domain.entity.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {


    Optional<Member> findMemberByEmail(String email);
    Optional<Member> findMemberByUsername(String username);
    List<Member> findAllBy();

    @Query("select m from Member m where m.username =:username")
    @EntityGraph(attributePaths = {"images"})
    Optional<Member> findMemberWithImage(@Param("username") String username);



}
