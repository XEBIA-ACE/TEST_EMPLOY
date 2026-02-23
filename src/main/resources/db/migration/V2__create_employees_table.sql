-- ============================================================
-- Migration V2 : Create employees table
-- ============================================================

CREATE TABLE IF NOT EXISTS employees (
    id              BIGSERIAL       PRIMARY KEY,
    first_name      VARCHAR(100)    NOT NULL,
    last_name       VARCHAR(100)    NOT NULL,
    email           VARCHAR(255)    NOT NULL UNIQUE,
    phone           VARCHAR(20),
    job_title       VARCHAR(150)    NOT NULL,
    salary          NUMERIC(12, 2)  NOT NULL,
    hire_date       DATE            NOT NULL,
    date_of_birth   DATE,
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE'
                        CHECK (status IN ('ACTIVE', 'INACTIVE', 'ON_LEAVE', 'TERMINATED')),
    employment_type VARCHAR(20)     NOT NULL DEFAULT 'FULL_TIME'
                        CHECK (employment_type IN ('FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERN')),
    department_id   BIGINT          REFERENCES departments(id) ON DELETE SET NULL,
    manager_id      BIGINT          REFERENCES employees(id)   ON DELETE SET NULL,
    address         VARCHAR(500),
    city            VARCHAR(100),
    country         VARCHAR(100),
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

-- Composite indexes for the most common query patterns
CREATE INDEX IF NOT EXISTS idx_employees_email      ON employees (LOWER(email));
CREATE INDEX IF NOT EXISTS idx_employees_department ON employees (department_id);
CREATE INDEX IF NOT EXISTS idx_employees_manager    ON employees (manager_id);
CREATE INDEX IF NOT EXISTS idx_employees_status     ON employees (status);
CREATE INDEX IF NOT EXISTS idx_employees_name       ON employees (last_name, first_name);

COMMENT ON TABLE employees IS 'Core employee records for the organisation.';
COMMENT ON COLUMN employees.status IS 'ACTIVE | INACTIVE | ON_LEAVE | TERMINATED';
COMMENT ON COLUMN employees.employment_type IS 'FULL_TIME | PART_TIME | CONTRACT | INTERN';
COMMENT ON COLUMN employees.manager_id IS 'Self-referential FK to the employee''s direct manager.';
