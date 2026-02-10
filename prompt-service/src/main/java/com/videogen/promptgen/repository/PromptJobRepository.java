package com.videogen.promptgen.repository;

import com.videogen.promptgen.model.PromptJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromptJobRepository extends JpaRepository<PromptJob, Long> {
}
