package practice.capston.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import practice.capston.domain.entity.Image;
import practice.capston.domain.entity.Member;
import practice.capston.domain.entity.TextContent;
import practice.capston.service.ImageService;
import practice.capston.service.TextContentService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final ImageService imageService;
    private final TextContentService textContentService;


    @GetMapping("/myDirectory")
    public String myDirectory(@AuthenticationPrincipal Member member,
                              Model model) {
        List<Image> imageList = imageService.findAllImages(member.getUsername());
        model.addAttribute("images", imageList);
        return "user/myImageDirectory";
    }

    @GetMapping("/myDirectory/showContents/{id}/imageContent")
    public String showContents(@PathVariable("id") Long id, Model model) {
        List<TextContent> textContents = textContentService.findAllTextContent(id);
        Image image = imageService.findImageById(id);
        model.addAttribute("image", image);
        model.addAttribute("textContents", textContents);
        return "user/showContentsList";
    }

    @GetMapping("/myDirectory/showContents/{id}/imageRemove")
    public String deleteImage(@PathVariable("id") Long id, Model model) {
        Image findImage = imageService.findImageById(id);
        imageService.deleteImage(findImage, findImage.getMember());
        return "redirect:/myDirectory";
    }

    @GetMapping("/myDirectory/updateContent/{id}/remove")
    public String deleteContent(@PathVariable("id") Long id) {
        textContentService.removeTextById(id);
        return "redirect:/myDirectory";
    }

    @GetMapping("/myDirectory/updateContent/{id}/edit")
    public String updateContent(@PathVariable("id") Long id, Model model) {
        TextContent textContent = textContentService.findTextById(id);
        model.addAttribute("textContent", textContent);
        return "user/updateContent";
    }

    @PostMapping("/myDirectory/updateContent/{id}")
    public String updateContents(@PathVariable("id") Long id, @RequestParam("resultText") String resultText) {
        textContentService.updateTextContent(id, resultText);
        return "redirect:/myDirectory";
    }

}
