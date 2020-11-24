package practice.capston.service;

import practice.capston.domain.entity.Image;
import practice.capston.domain.entity.Member;

import java.util.List;

public interface ImageService {

    Image saveImage(String title, Member member);
    List<Image> findAllImages(String username);
    Image findImageByTitle(String title, String username);
    void deleteImage(Image image, Member member);
    Image findImageById(Long id);
}
