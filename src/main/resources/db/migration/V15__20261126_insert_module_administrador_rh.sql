INSERT INTO modules (id, name, description, active) VALUES 
('ADMINISTRADOR_RH', 'Administrador RH', 'Módulo administrativo completo de recursos humanos', true);

INSERT INTO module_allowed_departments (module_id, department) VALUES
('ADMINISTRADOR_RH', 'TI'),
('ADMINISTRADOR_RH', 'RH');

INSERT INTO module_incompatible_modules (module_id, incompatible_module_id) VALUES
('ADMINISTRADOR_RH', 'COLABORADOR_RH');

