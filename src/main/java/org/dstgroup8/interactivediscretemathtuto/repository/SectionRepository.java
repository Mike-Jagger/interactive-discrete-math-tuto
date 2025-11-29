/*
 * THIS FILE WAS MOSTLY USED TO EXPERIMENT WITH DIFFERENT FEATURES
 * OF SPRING BOOT AND OTHER LIBRARIES AND THEREFORE IS NOT PART OF THE
 * FINAL IMPLEMENTATION
 * */

package org.dstgroup8.interactivediscretemathtuto.repository;

import org.dstgroup8.interactivediscretemathtuto.model.Section;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SectionRepository extends JpaRepository<Section, Long> {
}