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
        // 1. Load Data from File System
        List<CourseModels.Module> courseData = contentService.loadCourseData();

        // 2. Convert to JSON
        Gson gson = new Gson();
        String jsonString = gson.toJson(courseData);

        // 3. Read the HTML Template
        // Assumes index.html is in src/main/resources/static/index.html or templates
        Resource resource = new ClassPathResource("static/index.html");

        if (!resource.exists()) {
            return "Error: index.html not found in src/main/resources/static/";
        }

        String htmlContent = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

        // 4. Inject JSON
        // We look for 'const courseData = [ ... ];' and replace it
        // The regex finds "const courseData =" followed by anything until a semicolon
        String processedHtml = htmlContent.replaceAll(
                "const courseData = \\[([\\s\\S]*?)\\];",
                "const courseData = " + jsonString + ";"
        );

        return processedHtml;
    }
}
