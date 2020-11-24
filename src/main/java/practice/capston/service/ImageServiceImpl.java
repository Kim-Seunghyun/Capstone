package practice.capston.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import practice.capston.domain.entity.Image;
import practice.capston.domain.entity.Member;
import practice.capston.repository.ImageRepository;
import practice.capston.repository.MemberRepository;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final MemberRepository memberRepository;

    /*
    * 여러 사용자가 사용할 수 있는 환경이므로,
    * 타이틀 입력시 자신의 아이디가 덧붙여져,
    * 파일이 덮어쓰여지는 것을 방지함.
    * */
    @Transactional
    @Override
    public Image saveImage(String title, Member member) {
        Optional<Member> memberByUsername = memberRepository.findMemberByUsername(member.getUsername());
        if(!memberByUsername.isPresent()){
            throw new IllegalStateException("ServiceImpl: 28");
        }
        Member findMember = memberByUsername.get();
        Image image = new Image(title, findMember);

        findMember.possibleToStore();
        Image save = imageRepository.save(image);
        findMember.increaseImageCount();

        return save;
    }


    @Override
    public List<Image> findAllImages(String username) {
        List<Image> imageList = imageRepository.findAllImageByEmail(username);
        return imageList;
    }

    @Override
    public Image findImageByTitle(String title, String username) {
        String imageTitle = title + username;
        Optional<Image> imageByTitle = imageRepository.findImageByTitle(imageTitle);
        if(!imageByTitle.isPresent()){
            throw new IllegalStateException("ServiceImpl: 50");
        }
        return imageByTitle.get();
    }

    @Override
    @Transactional
    public void deleteImage(Image image, Member member) {
        imageRepository.delete(image);
        member.decreaseImageCount();
    }

    @Override
    public Image findImageById(Long id) {
        Optional<Image> findImage = imageRepository.findImageById(id);
        if(findImage.isEmpty()){
            throw new IllegalStateException("ImageServiceImpl:74");
        }
        return findImage.get();
    }
}
