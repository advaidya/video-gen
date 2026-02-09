CREATE TABLE narration_scripts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    raw_text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE script_segments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    script_id BIGINT NOT NULL,
    segment_number INT NOT NULL,
    segment_text TEXT NOT NULL,
    estimated_duration_seconds DOUBLE NOT NULL,
    word_count INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_script_segments_script
        FOREIGN KEY (script_id) REFERENCES narration_scripts(id) ON DELETE CASCADE
);

CREATE INDEX idx_segments_script_id ON script_segments(script_id);
