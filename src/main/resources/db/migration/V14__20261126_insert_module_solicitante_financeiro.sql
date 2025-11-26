INSERT INTO modules (id, name, description, active) VALUES 
('SOLICITANTE_FINANCEIRO', 'Solicitante Financeiro', 'Módulo para solicitação de aprovações financeiras', true);

INSERT INTO module_allowed_departments (module_id, department) VALUES
('SOLICITANTE_FINANCEIRO', 'TI'),
('SOLICITANTE_FINANCEIRO', 'Financeiro');

INSERT INTO module_incompatible_modules (module_id, incompatible_module_id) VALUES
('SOLICITANTE_FINANCEIRO', 'APROVADOR_FINANCEIRO');

