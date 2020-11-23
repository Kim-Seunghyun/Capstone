package practice.capston.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import practice.capston.domain.entity.Image;
import practice.capston.domain.entity.TextContent;

import java.util.List;

public interface TextContentService{
    List<TextContent> findAllTextContent(Long id);
    TextContent createTextContent(String result, Long imageId);

}
