package com.videogen.segmentation.repository;

import com.videogen.segmentation.model.NarrationScript;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NarrationScriptRepository extends JpaRepository<NarrationScript, Long> {
}
