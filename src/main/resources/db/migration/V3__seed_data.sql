-- ============================================================
-- Migration V3 : Seed reference / demo data
-- Applied in all environments (including tests) to ensure a
-- known baseline. Adjust or remove data as needed.
-- ============================================================

INSERT INTO departments (name, description, budget, location) VALUES
    ('Engineering',       'Builds and maintains all software products',           1500000.00, 'San Francisco, CA'),
    ('Human Resources',   'Manages talent acquisition and employee relations',      350000.00, 'New York, NY'),
    ('Finance',           'Financial planning, reporting, and compliance',          500000.00, 'Chicago, IL'),
    ('Marketing',         'Brand strategy, digital marketing, and communications',  750000.00, 'Austin, TX'),
    ('Operations',        'Business operations, facilities, and logistics',         400000.00, 'Remote')
ON CONFLICT (name) DO NOTHING;

-- Seed a small set of demo employees (salaries in USD / year)
INSERT INTO employees (
    first_name, last_name, email, phone, job_title, salary,
    hire_date, status, employment_type, department_id
) VALUES
    ('Alice', 'Smith',   'alice.smith@example.com',   '+1-415-555-0101',
     'VP of Engineering',          145000.00, '2018-03-01', 'ACTIVE', 'FULL_TIME',
     (SELECT id FROM departments WHERE name = 'Engineering')),

    ('Bob',   'Johnson', 'bob.johnson@example.com',   '+1-415-555-0102',
     'Senior Software Engineer',   105000.00, '2020-07-15', 'ACTIVE', 'FULL_TIME',
     (SELECT id FROM departments WHERE name = 'Engineering')),

    ('Carol', 'Williams','carol.williams@example.com','+1-212-555-0103',
     'HR Manager',                  82000.00, '2019-01-10', 'ACTIVE', 'FULL_TIME',
     (SELECT id FROM departments WHERE name = 'Human Resources')),

    ('David', 'Brown',   'david.brown@example.com',   '+1-312-555-0104',
     'Financial Analyst',           75000.00, '2021-05-20', 'ACTIVE', 'FULL_TIME',
     (SELECT id FROM departments WHERE name = 'Finance')),

    ('Eva',   'Davis',   'eva.davis@example.com',     '+1-512-555-0105',
     'Marketing Specialist',        70000.00, '2022-02-28', 'ACTIVE', 'FULL_TIME',
     (SELECT id FROM departments WHERE name = 'Marketing'))
ON CONFLICT (email) DO NOTHING;

-- Wire Bob's manager to Alice
UPDATE employees
   SET manager_id = (SELECT id FROM employees WHERE email = 'alice.smith@example.com')
 WHERE email = 'bob.johnson@example.com';
