-- ============================================================
-- Migration V1 : Create departments table
-- ============================================================

CREATE TABLE IF NOT EXISTS departments (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(100)    NOT NULL UNIQUE,
    description VARCHAR(500),
    budget      DOUBLE PRECISION NOT NULL,
    location    VARCHAR(200),
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

-- Partial index: frequently searched by name
CREATE INDEX IF NOT EXISTS idx_departments_name ON departments (LOWER(name));

COMMENT ON TABLE departments IS 'Organizational departments; each employee belongs to at most one department.';
COMMENT ON COLUMN departments.budget IS 'Annual budget in USD.';
