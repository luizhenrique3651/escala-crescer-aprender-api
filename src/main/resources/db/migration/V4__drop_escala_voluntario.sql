-- Flyway migration V4: Remove legacy join table escala_voluntario (used previously by Escala.voluntarios)
-- This migration is idempotent: it checks for existence before dropping.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'escala_voluntario') THEN
        -- Drop foreign key constraints if they exist
        -- The constraint names may vary, attempt to drop known patterns
        BEGIN
            ALTER TABLE IF EXISTS escala_voluntario DROP CONSTRAINT IF EXISTS fk_escala_voluntario_escala_id;
        EXCEPTION WHEN OTHERS THEN
            -- ignore
        END;
        BEGIN
            ALTER TABLE IF EXISTS escala_voluntario DROP CONSTRAINT IF EXISTS fk_escala_voluntario_voluntario_id;
        EXCEPTION WHEN OTHERS THEN
            -- ignore
        END;
        DROP TABLE IF EXISTS escala_voluntario;
    END IF;
END $$;

-- Also drop the old join table for escala_dia_voluntario if it's no longer needed
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'escala_dia_voluntario') THEN
        -- keep escala_dia_voluntario as it's now used by new model; only drop if it's legacy (safety: do nothing)
        -- No action here. If you want to drop, uncomment below lines.
        -- DROP TABLE IF EXISTS escala_dia_voluntario;
    END IF;
END $$;

