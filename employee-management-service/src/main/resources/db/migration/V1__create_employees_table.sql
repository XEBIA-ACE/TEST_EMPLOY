-- ============================================================
-- V1 — Initial schema: employees table
-- ============================================================

CREATE TABLE employees (
    id               BIGSERIAL       PRIMARY KEY,
    employee_number  VARCHAR(20)     NOT NULL UNIQUE,
    first_name       VARCHAR(100)    NOT NULL,
    last_name        VARCHAR(100)    NOT NULL,
    email            VARCHAR(255)    NOT NULL UNIQUE,
    phone            VARCHAR(30),
    department       VARCHAR(100)    NOT NULL,
    job_title        VARCHAR(150)    NOT NULL,
    employment_status VARCHAR(20)    NOT NULL
                     CHECK (employment_status IN ('ACTIVE', 'INACTIVE', 'ON_LEAVE', 'TERMINATED')),
    salary           NUMERIC(15, 2),
    hire_date        DATE            NOT NULL,
    date_of_birth    DATE,
    address          VARCHAR(500),
    manager_id       BIGINT,

    -- Audit columns
    created_at       TIMESTAMP       NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP,
    created_by       VARCHAR(100),
    updated_by       VARCHAR(100),

    CONSTRAINT fk_manager FOREIGN KEY (manager_id) REFERENCES employees(id)
        ON DELETE SET NULL
);

-- Indexes for common query patterns
CREATE INDEX idx_employees_department       ON employees(department);
CREATE INDEX idx_employees_employment_status ON employees(employment_status);
CREATE INDEX idx_employees_hire_date        ON employees(hire_date);
CREATE INDEX idx_employees_manager_id       ON employees(manager_id);

-- Composite index for full-name lookups
CREATE INDEX idx_employees_name ON employees(last_name, first_name);
