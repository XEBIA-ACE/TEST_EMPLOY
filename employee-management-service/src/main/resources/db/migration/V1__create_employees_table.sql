-- =============================================================================
-- V1 - Create employees table
-- =============================================================================

CREATE SEQUENCE IF NOT EXISTS employees_id_seq
    START WITH 1
    INCREMENT BY 50;

CREATE TABLE IF NOT EXISTS employees (
    id          BIGINT          NOT NULL DEFAULT nextval('employees_id_seq'),
    first_name  VARCHAR(100)    NOT NULL,
    last_name   VARCHAR(100)    NOT NULL,
    email       VARCHAR(255)    NOT NULL,
    phone       VARCHAR(30),
    department  VARCHAR(100)    NOT NULL,
    job_title   VARCHAR(150)    NOT NULL,
    salary      NUMERIC(12, 2),
    hire_date   DATE,
    status      VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_employees     PRIMARY KEY (id),
    CONSTRAINT uq_employees_email UNIQUE (email),
    CONSTRAINT chk_employee_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'ON_LEAVE', 'TERMINATED')),
    CONSTRAINT chk_employee_salary CHECK (salary IS NULL OR salary >= 0)
);

-- Indexes for common query patterns
CREATE INDEX idx_employees_email      ON employees (email);
CREATE INDEX idx_employees_department ON employees (department);
CREATE INDEX idx_employees_status     ON employees (status);
CREATE INDEX idx_employees_hire_date  ON employees (hire_date);

COMMENT ON TABLE  employees             IS 'Stores employee records for the organisation';
COMMENT ON COLUMN employees.id          IS 'Surrogate primary key';
COMMENT ON COLUMN employees.email       IS 'Corporate email address — must be unique across all employees';
COMMENT ON COLUMN employees.status      IS 'One of: ACTIVE, INACTIVE, ON_LEAVE, TERMINATED';
COMMENT ON COLUMN employees.created_at  IS 'Record creation timestamp (UTC)';
COMMENT ON COLUMN employees.updated_at  IS 'Record last-modified timestamp (UTC)';
