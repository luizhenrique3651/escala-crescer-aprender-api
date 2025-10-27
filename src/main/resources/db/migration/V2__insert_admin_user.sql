-- Flyway migration: insere usuário admin padrão (idempotente)
-- Versão: V2

INSERT INTO usuario (email, senha, role)
VALUES (
  'admin@email.com',
  '$2a$10$Abnv7Sc5MB242IgHxdsGnuTldUdo5T0B4G9Jny9jvYAtdrnkElFKC',
  'COORDENADOR'
)
ON CONFLICT (email) DO NOTHING;

