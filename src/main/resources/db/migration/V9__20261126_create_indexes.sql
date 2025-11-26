CREATE INDEX IF NOT EXISTS idx_accesses_user_id ON accesses(user_id);
CREATE INDEX IF NOT EXISTS idx_accesses_module_id ON accesses(module_id);
CREATE INDEX IF NOT EXISTS idx_accesses_status ON accesses(status);
CREATE INDEX IF NOT EXISTS idx_accesses_request_protocol ON accesses(request_protocol);
CREATE INDEX IF NOT EXISTS idx_requests_user_id ON requests(user_id);
CREATE INDEX IF NOT EXISTS idx_requests_status ON requests(status);

