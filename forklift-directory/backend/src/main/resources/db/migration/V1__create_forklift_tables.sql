CREATE TABLE IF NOT EXISTS forklifts (
    id BIGSERIAL PRIMARY KEY,
    brand VARCHAR(100) NOT NULL,
    number VARCHAR(50) NOT NULL UNIQUE,
    load_capacity DECIMAL(10,3) NOT NULL,
    is_active BOOLEAN DEFAULT true,
    last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_by VARCHAR(255)
    );

CREATE TABLE IF NOT EXISTS downtimes (
    id BIGSERIAL PRIMARY KEY,
    forklift_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_downtimes_forklift FOREIGN KEY (forklift_id) REFERENCES forklifts(id) ON DELETE RESTRICT
    );

CREATE INDEX idx_downtimes_forklift_id ON downtimes(forklift_id);
CREATE INDEX idx_downtimes_start_time ON downtimes(start_time DESC);
CREATE INDEX idx_forklifts_number ON forklifts(number);