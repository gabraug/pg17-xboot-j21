CREATE TABLE IF NOT EXISTS accesses (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    module_id VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    granted_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    request_protocol VARCHAR(255) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (module_id) REFERENCES modules(id),
    FOREIGN KEY (request_protocol) REFERENCES requests(protocol)
);

