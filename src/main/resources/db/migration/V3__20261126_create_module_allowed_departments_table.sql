CREATE TABLE IF NOT EXISTS module_allowed_departments (
    module_id VARCHAR(255) NOT NULL,
    department VARCHAR(255) NOT NULL,
    PRIMARY KEY (module_id, department),
    FOREIGN KEY (module_id) REFERENCES modules(id) ON DELETE CASCADE
);

