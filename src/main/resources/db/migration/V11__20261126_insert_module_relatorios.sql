INSERT INTO modules (id, name, description, active) VALUES 
('RELATORIOS', 'Relatórios Gerenciais', 'Módulo para geração e visualização de relatórios gerenciais', true);

INSERT INTO module_allowed_departments (module_id, department) VALUES
('RELATORIOS', 'TI'),
('RELATORIOS', 'Financeiro'),
('RELATORIOS', 'RH'),
('RELATORIOS', 'Operações'),
('RELATORIOS', 'Outros');

