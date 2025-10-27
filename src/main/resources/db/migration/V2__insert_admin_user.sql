-- Flyway migration: insere usuário admin padrão (idempotente)
-- Versão: V2

INSERT INTO usuario (email, senha, role)
VALUES (
  'admin@email.com',
  '$2a$10$Y7GnkISD445n0aNG.b8QquKz8LwLFGCOCQCqffyuFZW7N.VxwVpAK',
  'COORDENADOR'
)
ON CONFLICT (email) DO NOTHING;

