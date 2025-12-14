-- Migration V3: cria tabela escala_dia e tabela de relacionamento escala_dia_voluntario

CREATE TABLE IF NOT EXISTS escala_dia (
  id BIGSERIAL PRIMARY KEY,
  data_escala_dia DATE,
  escala_id BIGINT,
  CONSTRAINT fk_escala_dia_escala FOREIGN KEY (escala_id) REFERENCES escala(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS escala_dia_voluntario (
  escala_dia_id BIGINT NOT NULL,
  voluntario_id BIGINT NOT NULL,
  PRIMARY KEY (escala_dia_id, voluntario_id),
  CONSTRAINT fk_edv_escala_dia FOREIGN KEY (escala_dia_id) REFERENCES escala_dia(id) ON DELETE CASCADE,
  CONSTRAINT fk_edv_voluntario FOREIGN KEY (voluntario_id) REFERENCES voluntario(id) ON DELETE CASCADE
);

