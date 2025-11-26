INSERT INTO modules (id, name, description, active) VALUES 
('FINANCEIRO', 'Gestão Financeira', 'Módulo para gestão financeira e controle de contas', true);

INSERT INTO module_allowed_departments (module_id, department) VALUES
('FINANCEIRO', 'TI'),
('FINANCEIRO', 'Financeiro');

