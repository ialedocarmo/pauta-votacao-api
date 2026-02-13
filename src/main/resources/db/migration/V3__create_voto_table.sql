CREATE TABLE voto (
  id BIGSERIAL PRIMARY KEY,
  pauta_id BIGINT NOT NULL,
  associado_id VARCHAR(100) NOT NULL,
  voto VARCHAR(3) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL,
  CONSTRAINT fk_voto_pauta FOREIGN KEY (pauta_id) REFERENCES pauta (id),
  CONSTRAINT uq_voto_pauta_associado UNIQUE (pauta_id, associado_id),
  CONSTRAINT ck_voto_valido CHECK (voto IN ('SIM', 'NAO'))
);

CREATE INDEX idx_voto_pauta_id ON voto (pauta_id);
