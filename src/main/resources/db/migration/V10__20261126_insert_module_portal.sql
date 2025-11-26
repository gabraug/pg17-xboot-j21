INSERT INTO modules (id, name, description, active) VALUES 
('PORTAL', 'Portal do Colaborador', 'Portal de acesso geral ao sistema para todos os colaboradores', true);

INSERT INTO module_allowed_departments (module_id, department) VALUES
('PORTAL', 'TI'),
('PORTAL', 'Financeiro'),
('PORTAL', 'RH'),
('PORTAL', 'Operações'),
('PORTAL', 'Outros');

