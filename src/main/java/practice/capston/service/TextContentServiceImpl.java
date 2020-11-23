package practice.capston.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practice.capston.domain.entity.Image;
import practice.capston.domain.entity.TextContent;
import practice.capston.repository.ImageRepository;
import practice.capston.repository.TextContentRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TextContentServiceImpl implements TextContentService {

    private final TextContentRepository textContentRepository;
    private final ImageRepository imageRepository;

    @Override
    public List<TextContent> findAllTextContent(Long id) {
        List<TextContent> textContentList = textContentRepository.findAllTextContent(id);
        return textContentList;
    }

    @Override
    public TextContent createTextContent(String result, Long imageId) {
        Optional<Image> imageById = imageRepository.findImageById(imageId);

        if(imageById.isEmpty()){
            throw new IllegalStateException("TextContentServiceImpl: 32");
        }

        Image image = imageById.get();
        TextContent textContent = new TextContent(result, image);
        TextContent save = textContentRepository.save(textContent);
        return save;
    }
}
