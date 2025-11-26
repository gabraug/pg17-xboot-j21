CREATE TABLE IF NOT EXISTS module_incompatible_modules (
    module_id VARCHAR(255) NOT NULL,
    incompatible_module_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (module_id, incompatible_module_id),
    FOREIGN KEY (module_id) REFERENCES modules(id) ON DELETE CASCADE
);

