-- Preflight: fail early if existing data exceeds the new limits.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM notifications
        WHERE char_length(coalesce(titulo, '')) > 80
    ) THEN
        RAISE EXCEPTION 'Existing notifications contain titulo values longer than 80 characters.';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM notifications
        WHERE char_length(coalesce(mensagem, '')) > 500
    ) THEN
        RAISE EXCEPTION 'Existing notifications contain mensagem values longer than 500 characters.';
    END IF;
END $$;

ALTER TABLE notifications
    ALTER COLUMN titulo TYPE varchar(80),
    ALTER COLUMN titulo SET NOT NULL,
    ALTER COLUMN mensagem TYPE varchar(500),
    ALTER COLUMN mensagem SET NOT NULL;
