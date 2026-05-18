CREATE TABLE IF NOT EXISTS zona_viatura_cidade (
    id BIGSERIAL PRIMARY KEY,
    zona_id BIGINT NOT NULL REFERENCES zona_viatura(id),
    cidade VARCHAR(255) NOT NULL,
    ordem INTEGER NOT NULL
);

ALTER TABLE viatura
    ADD COLUMN IF NOT EXISTS zona_cidade_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_viatura_zona_cidade'
    ) THEN
        ALTER TABLE viatura
            ADD CONSTRAINT fk_viatura_zona_cidade
            FOREIGN KEY (zona_cidade_id)
            REFERENCES zona_viatura_cidade(id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_zona_viatura_cidade_zona_id
    ON zona_viatura_cidade (zona_id);

CREATE INDEX IF NOT EXISTS idx_viatura_zona_cidade_id
    ON viatura (zona_cidade_id);

WITH zonas_consolidadas AS (
    SELECT
        MIN(id) AS zona_canonica_id,
        batalhao_id,
        nome
    FROM zona_viatura
    GROUP BY batalhao_id, nome
),
cidades_distintas AS (
    SELECT DISTINCT
        zc.zona_canonica_id,
        zv.cidade
    FROM zona_viatura zv
    JOIN zonas_consolidadas zc
      ON zc.batalhao_id = zv.batalhao_id
     AND zc.nome = zv.nome
    WHERE zv.cidade IS NOT NULL
      AND trim(zv.cidade) <> ''
),
cidades_ordenadas AS (
    SELECT
        zona_canonica_id,
        cidade,
        ROW_NUMBER() OVER (PARTITION BY zona_canonica_id ORDER BY cidade) AS ordem
    FROM cidades_distintas
)
INSERT INTO zona_viatura_cidade (zona_id, cidade, ordem)
SELECT
    co.zona_canonica_id,
    co.cidade,
    co.ordem
FROM cidades_ordenadas co
WHERE NOT EXISTS (
    SELECT 1
    FROM zona_viatura_cidade zvc
    WHERE zvc.zona_id = co.zona_canonica_id
      AND lower(zvc.cidade) = lower(co.cidade)
);

WITH zonas_consolidadas AS (
    SELECT
        MIN(id) AS zona_canonica_id,
        batalhao_id,
        nome
    FROM zona_viatura
    GROUP BY batalhao_id, nome
)
UPDATE viatura v
SET
    zona_id = zc.zona_canonica_id,
    zona_cidade_id = zvc.id
FROM zona_viatura zv
JOIN zonas_consolidadas zc
  ON zc.batalhao_id = zv.batalhao_id
 AND zc.nome = zv.nome
LEFT JOIN zona_viatura_cidade zvc
  ON zvc.zona_id = zc.zona_canonica_id
 AND lower(zvc.cidade) = lower(zv.cidade)
WHERE v.zona_id = zv.id;

WITH zonas_consolidadas AS (
    SELECT
        MIN(id) AS zona_canonica_id,
        batalhao_id,
        nome
    FROM zona_viatura
    GROUP BY batalhao_id, nome
)
DELETE FROM zona_viatura zv
USING zonas_consolidadas zc
WHERE zv.batalhao_id = zc.batalhao_id
  AND zv.nome = zc.nome
  AND zv.id <> zc.zona_canonica_id;
