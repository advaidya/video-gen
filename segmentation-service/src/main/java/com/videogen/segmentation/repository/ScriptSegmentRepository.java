package com.videogen.segmentation.repository;

import com.videogen.segmentation.model.ScriptSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScriptSegmentRepository extends JpaRepository<ScriptSegment, Long> {

    List<ScriptSegment> findByScriptIdOrderBySegmentNumberAsc(Long scriptId);

    void deleteByScriptId(Long scriptId);
}
