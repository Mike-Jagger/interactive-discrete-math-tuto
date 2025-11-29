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
import java.util.stream.Collectors;

@RestController
public class IndexController {
    @Autowired
    private ContentService contentService;

    @GetMapping("/")
    public String index() throws IOException {
        List<CourseModels.Module> courseData = contentService.loadCourseData();

        // Generate JavaScript Object Literal (Not standard JSON)
        String jsObjectString = generateJsSource(courseData);

        Resource resource = new ClassPathResource("static/index.html");

        if (!resource.exists()) {
            return "Error: index.html not found in src/main/resources/static/";
        }

        String htmlContent = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);

        String processedHtml = htmlContent.replaceAll(
                "const courseData = \\[([\\s\\S]*?)\\];",
                "const courseData = " + jsObjectString + ";"
        );

        return processedHtml;
    }

    /**
     * Manually builds a JavaScript Object string to allow for Backticks (Template Literals)
     * and unquoted keys, making the source code readable.
     */
    private String generateJsSource(List<CourseModels.Module> modules) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");

        for (int i = 0; i < modules.size(); i++) {
            CourseModels.Module mod = modules.get(i);
            sb.append("    {\n");
            sb.append("        id: \"").append(escape(mod.id)).append("\",\n");
            sb.append("        title: \"").append(escape(mod.title)).append("\",\n");
            sb.append("        description: \"").append(escape(mod.description)).append("\",\n");
            sb.append("        lessons: [\n");

            for (int j = 0; j < mod.lessons.size(); j++) {
                CourseModels.Lesson les = mod.lessons.get(j);
                sb.append("            {\n");
                sb.append("                id: \"").append(escape(les.id)).append("\",\n");
                sb.append("                title: \"").append(escape(les.title)).append("\",\n");

                // We must escape existing backticks inside the content to avoid syntax errors
                String safeHtml = les.lessonHtml != null ? les.lessonHtml.replace("`", "\\`") : "";
                sb.append("                lessonHtml: `").append(safeHtml).append("`,\n");

                if (les.quiz != null) {
                    sb.append("                quiz: {\n");
                    sb.append("                    id: \"").append(les.quiz.id).append("\",\n");
                    sb.append("                    type: \"").append(les.quiz.type).append("\",\n");
                    sb.append("                    question: \"").append(escape(les.quiz.question)).append("\",\n");

                    // Options Array
                    sb.append("                    options: [");
                    if (les.quiz.options != null) {
                        sb.append(les.quiz.options.stream()
                                .map(opt -> "\"" + escape(opt) + "\"")
                                .collect(Collectors.joining(", ")));
                    }
                    sb.append("],\n");

                    // Correct Index/Indices
                    if ("single".equals(les.quiz.type)) {
                        sb.append("                    correctIndex: ").append(les.quiz.correctIndex).append(",\n");
                    } else {
                        sb.append("                    correctIndices: ").append(les.quiz.correctIndices).append(",\n");
                    }

                    sb.append("                    hint: \"").append(escape(les.quiz.hint)).append("\",\n");
                    sb.append("                    maxAttempts: ").append(les.quiz.maxAttempts).append("\n");
                    sb.append("                }\n");
                } else {
                    sb.append("                quiz: null\n");
                }

                sb.append("            }");
                if (j < mod.lessons.size() - 1) sb.append(",");
                sb.append("\n");
            }

            sb.append("        ]\n");
            sb.append("    }");
            if (i < modules.size() - 1) sb.append(",");
            sb.append("\n");
        }

        sb.append("]");
        return sb.toString();
    }

    // Helper to escape double quotes for standard JS strings
    private String escape(String text) {
        if (text == null) return "";
        return text.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }
}