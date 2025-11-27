package org.dstgroup8.interactivediscretemathtuto.controller;

import com.logic.model.Section;
import com.logic.repository.SectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class CourseController {

    @Autowired
    private SectionRepository sectionRepository;

    // --- STUDENT VIEW ---
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("sections", sectionRepository.findAll());
        return "index";
    }

    @GetMapping("/lesson/{id}")
    public String lesson(@PathVariable Long id, Model model) {
        Section section = sectionRepository.findById(id).orElseThrow();
        model.addAttribute("section", section);

        // Dynamic Logic for Games
        if ("TRUTH_TABLE_GAME".equals(section.getType())) {
            model.addAttribute("truthTable", generateTruthTable(section.getLogicExpression()));
        }
        return "lesson";
    }

    // --- INSTRUCTOR EDITOR ---
    @GetMapping("/editor")
    public String editor(Model model) {
        model.addAttribute("section", new Section());
        return "editor";
    }

    @PostMapping("/saveSection")
    public String saveSection(@ModelAttribute Section section) {
        sectionRepository.save(section);
        return "redirect:/";
    }

    // --- LOGIC ENGINE HELPER ---
    // Generates a simple truth table for P and Q
    private String[][] generateTruthTable(String operation) {
        //

[Image of Truth Table structure]
        - We generate this array for the frontend
        String[][] table = new String[5][3];
        table[0] = new String[]{"P", "Q", "Result"};

        boolean[] bools = {true, false};
        int row = 1;

        for (boolean p : bools) {
            for (boolean q : bools) {
                boolean result = evaluateLogic(p, q, operation);
                table[row][0] = String.valueOf(p);
                table[row][1] = String.valueOf(q);
                table[row][2] = String.valueOf(result);
                row++;
            }
        }
        return table;
    }

    private boolean evaluateLogic(boolean p, boolean q, String op) {
        if (op == null) return false;
        switch (op) {
            case "AND": return p && q;
            case "OR": return p || q;
            case "IMPLIES": return !p || q; // P -> Q is equivalent to !P or Q
            default: return false;
        }
    }
}