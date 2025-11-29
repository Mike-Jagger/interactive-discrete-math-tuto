package org.dstgroup8.interactivediscretemathtuto.model;

import java.util.ArrayList;
import java.util.List;

// Simple POJOs to represent the JSON structure required by the frontend
public class CourseModels {

    public static class Module {
        public String id;
        public String title;
        public String description;
        public List<Lesson> lessons = new ArrayList<>();
    }

    public static class Lesson {
        public String id;
        public String title;
        public String lessonHtml;
        public Quiz quiz;
    }

    public static class Quiz {
        public String id;
        public String type; // "single" or "multiple"
        public String question;
        public List<String> options = new ArrayList<>();
        public Object correctIndex; // Can be Integer (single) or List<Integer> (multiple)
        // Helper for JSON serialization logic (not sent to front directly if we sanitize, but needed here)
        public List<Integer> correctIndices;
        public String hint;
        public int maxAttempts;
    }
}