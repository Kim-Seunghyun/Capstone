package practice.capston.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import practice.capston.domain.dto.MemberManageDto;
import practice.capston.domain.entity.Member;
import practice.capston.service.MemberService;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final MemberService memberService;

    @GetMapping("/adminPage")
    public String adminPage(Model model){
        List<Member> memberList = memberService.findAllMembers();
        List<MemberManageDto> members = memberList
                .stream()
                .map(member -> new MemberManageDto(member))
                .collect(Collectors.toList());
        model.addAttribute("members", members);
        return "admin/adminPage";
    }

    @GetMapping("/admin/{username}/updateUsage")
    public String updateUsage(@PathVariable("username") String username) {
        memberService.upgradeUsage(username);
        return "redirect:/adminPage";
    }

    @GetMapping("/admin/{username}/removeuser")
    public String removeUser(@PathVariable("username") String username) {
        memberService.deleteMember(username);
        return "redirect:/adminPage";
    }


}
