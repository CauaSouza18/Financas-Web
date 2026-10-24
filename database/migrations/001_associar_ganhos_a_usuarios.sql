-- Execute uma vez no banco app_financas antes de usar a versão multiusuário.
ALTER TABLE ganhos
    ADD COLUMN usuario_id INT NULL;

-- Migra o histórico automaticamente somente quando existe uma única conta.
-- Com várias contas, os registros antigos ficam sem dono até serem associados manualmente.
UPDATE ganhos
SET usuario_id = (SELECT MIN(id) FROM usuarios)
WHERE usuario_id IS NULL
  AND (SELECT COUNT(*) FROM usuarios) = 1;

CREATE INDEX idx_ganhos_usuario_periodo
    ON ganhos (usuario_id, Cadastrado_Em, id);

SELECT COUNT(*) AS ganhos_sem_usuario
FROM ganhos
WHERE usuario_id IS NULL;
