package com.videogen.promptgen.repository;

import com.videogen.promptgen.model.PromptResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromptResultRepository extends JpaRepository<PromptResult, Long> {

    List<PromptResult> findByJobIdOrderBySegmentNumberAsc(Long jobId);
}
