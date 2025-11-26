CREATE TABLE IF NOT EXISTS request_modules (
    request_protocol VARCHAR(255) NOT NULL,
    module_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (request_protocol, module_id),
    FOREIGN KEY (request_protocol) REFERENCES requests(protocol) ON DELETE CASCADE
);

