package practice.capston.service;

import practice.capston.domain.entity.Image;
import practice.capston.domain.entity.Member;

import java.util.List;

public interface ImageService {

    Image saveImage(String title, Member member);
    List<Image> findAllImages(String email);
    Image findImageByTitle(String title, String username);
}
