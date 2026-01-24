-- Flyway migration: insere voluntário Administrador vinculado ao usuário admin@email.com (idempotente)
-- Versão: V5

-- Insere um voluntário com nome 'Administrador' vinculado ao usuário existente com email especificado,
-- mas somente se ainda não existir um voluntário associado a esse usuário.

INSERT INTO voluntario (NOM_USUARIO, USUARIO_ID)
SELECT 'Administrador', u.id
FROM usuario u
WHERE u.email = 'admin@email.com'
  AND NOT EXISTS (
    SELECT 1 FROM voluntario v WHERE v.USUARIO_ID = u.id
  );

