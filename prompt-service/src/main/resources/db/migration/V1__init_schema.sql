CREATE TABLE prompt_jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    style VARCHAR(50) NOT NULL,
    custom_style_description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE prompt_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT NOT NULL,
    segment_number INT NOT NULL,
    segment_text TEXT NOT NULL,
    generated_prompt TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prompt_results_job
        FOREIGN KEY (job_id) REFERENCES prompt_jobs(id) ON DELETE CASCADE
);

CREATE INDEX idx_prompt_results_job_id ON prompt_results(job_id);
