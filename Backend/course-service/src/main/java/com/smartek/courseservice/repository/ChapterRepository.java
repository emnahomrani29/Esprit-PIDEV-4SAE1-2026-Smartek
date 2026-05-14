package com.smartek.courseservice.repository;

import com.smartek.courseservice.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    List<Chapter> findByCourse_CourseIdOrderByOrderIndexAsc(Long courseId);
}
