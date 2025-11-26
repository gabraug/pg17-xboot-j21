INSERT INTO modules (id, name, description, active) VALUES 
('COLABORADOR_RH', 'Colaborador RH', 'Módulo básico para colaboradores do departamento de RH', true);

INSERT INTO module_allowed_departments (module_id, department) VALUES
('COLABORADOR_RH', 'TI'),
('COLABORADOR_RH', 'RH');

INSERT INTO module_incompatible_modules (module_id, incompatible_module_id) VALUES
('COLABORADOR_RH', 'ADMINISTRADOR_RH');

