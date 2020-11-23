package practice.capston.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import practice.capston.domain.entity.Image;

import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {

    @Query("select im from Image im where im.member.username = :username")
    List<Image> findAllImageByEmail(@Param("username") String username);

    @Query("select im from Image im where im.title = :title")
    Optional<Image> findImageByTitle(@Param("title") String title);

    @Query("select im from Image im where im.title = :title")
    @EntityGraph(attributePaths = {"textContents"})
    Optional<Image> findImageWithTextContent(@Param("title") String title);

    Optional<Image> findImageById(Long imagId);
}
