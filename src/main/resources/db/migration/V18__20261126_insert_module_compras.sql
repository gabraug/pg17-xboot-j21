INSERT INTO modules (id, name, description, active) VALUES 
('COMPRAS', 'Compras', 'Módulo para gestão de compras e fornecedores', true);

INSERT INTO module_allowed_departments (module_id, department) VALUES
('COMPRAS', 'TI'),
('COMPRAS', 'Operações');

