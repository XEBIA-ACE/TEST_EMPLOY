-- ============================================================
-- V2 — Sample seed data (dev / staging only)
-- ============================================================

INSERT INTO employees
    (employee_number, first_name, last_name, email, phone, department,
     job_title, employment_status, salary, hire_date, date_of_birth,
     created_at, created_by)
VALUES
    ('EMP-000001', 'Alice',   'Smith',   'alice.smith@example.com',   '+1-555-100-0001', 'Engineering',
     'VP of Engineering',       'ACTIVE', 175000.00, '2018-03-01', '1975-07-14', now(), 'system'),
    ('EMP-000002', 'Bob',     'Johnson', 'bob.johnson@example.com',   '+1-555-100-0002', 'Engineering',
     'Senior Software Engineer', 'ACTIVE',  95000.00, '2020-06-15', '1988-11-03', now(), 'system'),
    ('EMP-000003', 'Carol',   'Williams','carol.williams@example.com','+1-555-100-0003', 'HR',
     'HR Manager',              'ACTIVE',  85000.00, '2019-01-20', '1982-04-22', now(), 'system'),
    ('EMP-000004', 'David',   'Brown',   'david.brown@example.com',   '+1-555-100-0004', 'Finance',
     'Financial Analyst',       'ACTIVE',  78000.00, '2021-09-01', '1991-12-30', now(), 'system'),
    ('EMP-000005', 'Eva',     'Davis',   'eva.davis@example.com',     '+1-555-100-0005', 'Marketing',
     'Marketing Specialist',    'ON_LEAVE',65000.00, '2022-02-14', '1995-06-08', now(), 'system');

-- Set manager references (Bob and Carol report to Alice)
UPDATE employees SET manager_id = (SELECT id FROM employees WHERE employee_number = 'EMP-000001')
WHERE employee_number IN ('EMP-000002', 'EMP-000003');
