package practice.capston.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    @GetMapping("/myDirectory")
    public String myDirectory() {
        return "/user/myImageDirectory";
    }

    @GetMapping("/showContents")
    public String showContents() {
        return "/user/showContentsList";
    }

    @GetMapping("/updateContent")
    public String updateContent() {
        return "/user/updateContent";
    }

    @GetMapping("/summaryService")
    public String summaryService() {
        return "/summary/fileupload";
    }
}
