package org.dstgroup8.interactivediscretemathtuto.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Section {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 5000) // Allow long text for tutorials
    private String content;

    // Types: LESSON, TRUTH_TABLE_GAME, QUIZ
    private String type;

    private String logicExpression; // For games (e.g., "P && Q")
    private int pointValue;
}