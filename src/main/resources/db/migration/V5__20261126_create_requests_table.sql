CREATE TABLE IF NOT EXISTS requests (
    protocol VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    user_department VARCHAR(255) NOT NULL,
    justification TEXT,
    urgent BOOLEAN NOT NULL DEFAULT false,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    denial_reason TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

