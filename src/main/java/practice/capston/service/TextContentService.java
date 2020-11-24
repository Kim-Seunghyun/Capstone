package practice.capston.service;

import practice.capston.domain.entity.TextContent;

import java.util.List;

public interface TextContentService{
    List<TextContent> findAllTextContent(Long id);
    TextContent createTextContent(String result, Long imageId);
    TextContent updateTextContent(Long id, String newValue);
    TextContent findTextById(Long id);
    TextContent removeTextById(Long id);

}
