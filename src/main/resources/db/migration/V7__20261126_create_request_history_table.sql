CREATE TABLE IF NOT EXISTS request_history (
    id BIGSERIAL PRIMARY KEY,
    request_protocol VARCHAR(255) NOT NULL,
    date TIMESTAMP NOT NULL,
    action TEXT NOT NULL,
    FOREIGN KEY (request_protocol) REFERENCES requests(protocol) ON DELETE CASCADE
);

