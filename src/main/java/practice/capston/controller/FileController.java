package practice.capston.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import practice.capston.domain.dto.KafkaDto;
import practice.capston.domain.entity.Image;
import practice.capston.domain.entity.Member;
import practice.capston.domain.entity.TextContent;
import practice.capston.kafkaconfig.ProduceMessage;
import practice.capston.service.ImageService;
import practice.capston.service.TextContentService;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Controller
@RequiredArgsConstructor
public class FileController {

    private final ImageService imageService;
    private final TextContentService textContentService;
    private final ProduceMessage produceMessage;

    @GetMapping("/summaryService")
    public String summaryService(@AuthenticationPrincipal Member member) {
        return "summary/fileupload";
    }

    @PostMapping("/summaryService")
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile,
                             @AuthenticationPrincipal Member member) throws IOException {


        Image saveImage = imageService.saveImage(member.getUsername() + multipartFile.getOriginalFilename(), member);
        String path = saveImage.getPath();

        // 시언어 짜듯이 작성을 하게 되었는데...  리팩토링이 필요한 부분.
        // 근데 생각을 해보면, 버퍼에다가 데이터를 입력하는 부분이므로 필요한 변수가 꽤 많을 수 밖에 없고,
        // 파일 클래스를 별도로 만들어서 작업하는것은 오히려 별로라고 판단되는게, 컨트롤단의 역할로서 적당하다고 생각되기 때문에 고민 중.
        try{
            int bufferSize = 1024;
            int len = 0;
            int minusOne = -1;
            byte[] buffer = new byte[bufferSize];

            //쓰기 전용 스트림
            FileOutputStream fileOutputStream = new FileOutputStream(path + member.getUsername() + multipartFile.getOriginalFilename());

            //읽기 전용 스트림
            InputStream inputStream = multipartFile.getInputStream();

            while((len = inputStream.read(buffer)) != minusOne){
                fileOutputStream.write(buffer, 0, len);
            }

        }catch (Exception exception) {
            imageService.deleteImage(saveImage, member);
            throw new FileNotFoundException("FileController: 59");
        } finally {
            // 카프카 메세지에 이미지 DTO를 던지는 부분.
            TextContent text = textContentService.createTextContent("result test", saveImage.getId());
            KafkaDto kafkaDto = new KafkaDto(saveImage, text);
            produceMessage.sendMessage(kafkaDto);
        }


        return "redirect:/";
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data;
    }



}
