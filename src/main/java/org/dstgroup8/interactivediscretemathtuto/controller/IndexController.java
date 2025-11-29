package org.dstgroup8.interactivediscretemathtuto.controller;


import org.springframework.web.bind.annotation.RestController;
import org.dstgroup8.interactivediscretemathtuto.model.CourseModels;
import org.dstgroup8.interactivediscretemathtuto.service.ContentService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;


@RestController
public class IndexController {
    @Autowired
    private ContentService contentService;

    @GetMapping("/")
    public String index() throws IOException {
        List<CourseModels.Module> courseData = contentService.loadCourseData();

        Gson gson = new Gson();
        String jsonString = gson.toJson(courseData);

        Resource resource = new ClassPathResource("static/index.html");

        if (!resource.exists()) {
            return "Error: index.html not found in src/main/resources/static/";
        }

        String htmlContent = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

        return htmlContent.replaceAll(
                "const courseData = \\[([\\s\\S]*?)\\];",
                "const courseData = " + jsonString + ";"
        );
    }
}
