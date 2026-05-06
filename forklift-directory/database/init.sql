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
    forklift_id BIGINT NOT NULL REFERENCES forklifts(id) ON DELETE RESTRICT,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_downtimes_forklift_id ON downtimes(forklift_id);
CREATE INDEX idx_downtimes_start_time ON downtimes(start_time DESC);
CREATE INDEX idx_forklifts_number ON forklifts(number);

INSERT INTO forklifts (brand, number, load_capacity, modified_by) VALUES
('Amkodor', '45-05-NCH-1', 2.5, 'Ivanov I I'),
('Toyota', 'TO-23-AB-7', 3.2, 'Petrov P P'),
('Komatsu', 'KO-12-VG-3', 4.0, 'Sidorov S S');

INSERT INTO downtimes (forklift_id, start_time, end_time, description) VALUES
(1, '2024-04-16 09:00:00', '2024-04-16 15:47:00', 'Hydraulic system failure'),
(1, '2024-04-18 14:45:00', '2024-04-18 17:45:00', 'Engine problems');
