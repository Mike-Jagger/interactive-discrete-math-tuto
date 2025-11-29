package org.dstgroup8.interactivediscretemathtuto.service;

import org.dstgroup8.interactivediscretemathtuto.model.CourseModels.*;
import org.dstgroup8.interactivediscretemathtuto.model.CourseModels;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContentService {

    private final String CONTENT_DIR = "content";
    private final Parser markdownParser;
    private final HtmlRenderer htmlRenderer;

    public ContentService() {
        this.markdownParser = Parser.builder().build();
        this.htmlRenderer = HtmlRenderer.builder().build();
    }

    public List<CourseModels.Module> loadCourseData() {
        List<CourseModels.Module> modules = new ArrayList<>();
        File root = new File(CONTENT_DIR);

        if (!root.exists() || !root.isDirectory()) {
            System.err.println("Content directory not found at: " + root.getAbsolutePath());
            return modules;
        }

        // 1. Scan for Module folders (mod1, mod2, etc.)
        File[] modDirs = root.listFiles(File::isDirectory);
        if (modDirs == null) return modules;

        // Sort by directory name (mod1, mod2...) logic
        Arrays.sort(modDirs, Comparator.comparing(File::getName));

        for (File modDir : modDirs) {
            CourseModels.Module module = parseModule(modDir);
            if (module != null) {
                modules.add(module);
            }
        }
        return modules;
    }

    private CourseModels.Module parseModule(File dir) {
        CourseModels.Module module = new CourseModels.Module();
        module.id = dir.getName();

        // 2. Parse Module Metadata (modX.txt)
        File metaFile = new File(dir, dir.getName() + ".txt");
        Map<String, String> meta = parseProperties(metaFile);
        module.title = meta.getOrDefault("title", "Untitled Module");
        module.description = meta.getOrDefault("description", "No description.");

        // 3. Find Lessons (les*.md)
        File[] lessonFiles = dir.listFiles((d, name) -> name.startsWith("les") && name.endsWith(".md"));
        if (lessonFiles != null) {
            // Sort lessons naturally (les1, les2...)
            Arrays.sort(lessonFiles, Comparator.comparing(File::getName));

            for (File lesFile : lessonFiles) {
                String baseName = lesFile.getName().replace(".md", "");
                Lesson lesson = parseLesson(dir, baseName);
                if (lesson != null) {
                    module.lessons.add(lesson);
                }
            }
        }
        return module;
    }

    private Lesson parseLesson(File dir, String baseName) {
        Lesson lesson = new Lesson();
        lesson.id = baseName;

        // Parse Lesson Metadata (lesX.txt)
        File metaFile = new File(dir, baseName + ".txt");
        Map<String, String> meta = parseProperties(metaFile);
        lesson.title = meta.getOrDefault("title", "Untitled Lesson");

        // Parse Lesson Content (lesX.md) -> HTML
        File contentFile = new File(dir, baseName + ".md");
        lesson.lessonHtml = renderMarkdown(contentFile);

        // 4. Parse Associated Quiz
        // Metadata must specify "quizId=qX", otherwise we assume no quiz or try to guess
        String quizId = meta.get("quizId");
        if (quizId != null) {
            lesson.quiz = parseQuiz(dir, quizId);
        }

        return lesson;
    }

    private Quiz parseQuiz(File dir, String quizId) {
        Quiz quiz = new Quiz();
        quiz.id = quizId;

        // Quiz Metadata (qX.txt)
        File metaFile = new File(dir, quizId + ".txt");
        Map<String, String> meta = parseProperties(metaFile);

        quiz.type = meta.getOrDefault("type", "single");
        quiz.hint = meta.getOrDefault("hint", "No hint available.");
        quiz.maxAttempts = Integer.parseInt(meta.getOrDefault("maxAttempts", "3"));

        // Quiz Question Text (qX.md) - Optional, can override metadata question
        File qContentFile = new File(dir, quizId + ".md");
        if (qContentFile.exists()) {
            // We strip <p> tags usually for questions, but keeping html is fine
            quiz.question = readFileContent(qContentFile);
        } else {
            quiz.question = meta.getOrDefault("question", "Question text missing.");
        }

        // Parse Options (option.0, option.1 OR options=A,B,C)
        // We will look for option.0, option.1, etc. in the properties map
        List<String> opts = new ArrayList<>();
        int i = 0;
        while (meta.containsKey("option." + i)) {
            opts.add(meta.get("option." + i));
            i++;
        }
        quiz.options = opts;

        // Parse Correct Answer(s)
        if ("single".equals(quiz.type)) {
            quiz.correctIndex = Integer.parseInt(meta.getOrDefault("correctIndex", "0"));
        } else {
            // Multiple choice: assume comma separated "0,2"
            String indices = meta.getOrDefault("correctIndices", "");
            List<Integer> list = new ArrayList<>();
            for (String s : indices.split(",")) {
                if (!s.trim().isEmpty()) list.add(Integer.parseInt(s.trim()));
            }
            quiz.correctIndices = list;
            // The frontend code expects "correctIndices" for multiple type
            // But we need to map it to the right JSON field based on how your frontend expects it.
            // Your frontend courseData: type="multiple" has "correctIndices": [0,1,2]
        }

        return quiz;
    }

    // --- Helpers ---

    private String renderMarkdown(File file) {
        String markdown = readFileContent(file);
        Node document = markdownParser.parse(markdown);
        return htmlRenderer.render(document);
    }

    private String readFileContent(File file) {
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            return "";
        }
    }

    private Map<String, String> parseProperties(File file) {
        Map<String, String> map = new HashMap<>();
        if (!file.exists()) return map;
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            for (String line : lines) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    map.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }
}