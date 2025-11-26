INSERT INTO modules (id, name, description, active) VALUES 
('APROVADOR_FINANCEIRO', 'Aprovador Financeiro', 'Módulo para aprovação de solicitações financeiras', true);

INSERT INTO module_allowed_departments (module_id, department) VALUES
('APROVADOR_FINANCEIRO', 'TI'),
('APROVADOR_FINANCEIRO', 'Financeiro');

INSERT INTO module_incompatible_modules (module_id, incompatible_module_id) VALUES
('APROVADOR_FINANCEIRO', 'SOLICITANTE_FINANCEIRO');

