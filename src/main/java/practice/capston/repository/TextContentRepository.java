package practice.capston.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import practice.capston.domain.entity.TextContent;

import java.util.List;

public interface TextContentRepository extends JpaRepository<TextContent, Long> {

    @Query("select tc from TextContent tc where tc.image.id =:id")
    List<TextContent> findAllTextContent(@Param("id") Long id);


}
