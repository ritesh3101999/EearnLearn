package com.earnlearn.controller;

import com.earnlearn.service.LectureService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/lectures")
public class GlobalLectureController {

    private final LectureService lectureService;

    public GlobalLectureController(LectureService lectureService) {
        this.lectureService = lectureService;
    }

    @GetMapping
    public String listAllLectures(Model model) {
        model.addAttribute("lectures", lectureService.getAllLectures());
        return "lectures/list";
    }
}