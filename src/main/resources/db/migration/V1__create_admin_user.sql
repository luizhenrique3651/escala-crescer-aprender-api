-- Flyway migration: cria schema/tabelas principais para a aplicação
-- Versão: V1

-- Tabela de usuários
CREATE TABLE IF NOT EXISTS usuario (
  id BIGSERIAL PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  senha VARCHAR(255) NOT NULL,
  role VARCHAR(50) NOT NULL
);

-- Tabela de voluntários
CREATE TABLE IF NOT EXISTS voluntario (
  id BIGSERIAL PRIMARY KEY,
  NOM_USUARIO VARCHAR(255),
  USUARIO_ID BIGINT,
  CONSTRAINT fk_vol_usuario FOREIGN KEY (USUARIO_ID) REFERENCES usuario(id)
);

-- Tabela de escalas
CREATE TABLE IF NOT EXISTS escala (
  id BIGSERIAL PRIMARY KEY,
  MES_ESCALA INTEGER,
  ANO_ESCALA BIGINT
);

-- Tabela de relacionamento many-to-many entre escala e voluntario (join table)
CREATE TABLE IF NOT EXISTS escala_voluntario (
  escala_id BIGINT NOT NULL,
  voluntario_id BIGINT NOT NULL,
  CONSTRAINT pk_esc_vol PRIMARY KEY (escala_id, voluntario_id),
  CONSTRAINT fk_ev_esc FOREIGN KEY (escala_id) REFERENCES escala(id) ON DELETE CASCADE,
  CONSTRAINT fk_ev_vol FOREIGN KEY (voluntario_id) REFERENCES voluntario(id) ON DELETE CASCADE
);

-- Element collection: datas da escala
CREATE TABLE IF NOT EXISTS escala_datas (
  escala_id BIGINT NOT NULL,
  DATAS_ESCALA DATE,
  CONSTRAINT fk_sc_esc FOREIGN KEY (escala_id) REFERENCES escala(id) ON DELETE CASCADE
);

-- Element collection: datas disponíveis do voluntário
CREATE TABLE IF NOT EXISTS voluntario_datas_disponiveis (
  voluntario_id BIGINT NOT NULL,
  DATAS_DISPONIVEIS DATE,
  CONSTRAINT fk_vd_vol FOREIGN KEY (voluntario_id) REFERENCES voluntario(id) ON DELETE CASCADE
);

-- Índices auxiliares
CREATE INDEX IF NOT EXISTS idx_usuario_email ON usuario(email);
CREATE INDEX IF NOT EXISTS idx_escala_mes_ano ON escala(MES_ESCALA, ANO_ESCALA);
