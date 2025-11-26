INSERT INTO modules (id, name, description, active) VALUES 
('ESTOQUE', 'Gestão de Estoque', 'Módulo para controle de estoque e inventário', true);

INSERT INTO module_allowed_departments (module_id, department) VALUES
('ESTOQUE', 'TI'),
('ESTOQUE', 'Operações');

