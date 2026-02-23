-- =============================================================================
-- V2 - Seed sample employee data (non-production environments only)
-- =============================================================================

INSERT INTO employees (first_name, last_name, email, phone, department, job_title, salary, hire_date, status)
VALUES
    ('Alice',  'Johnson',  'alice.johnson@company.com',  '+1-555-100-0001', 'Engineering',  'Senior Software Engineer',  105000.00, '2020-03-01', 'ACTIVE'),
    ('Bob',    'Williams', 'bob.williams@company.com',   '+1-555-100-0002', 'Engineering',  'Software Engineer',          82000.00, '2021-06-15', 'ACTIVE'),
    ('Carol',  'Martinez', 'carol.martinez@company.com', '+1-555-100-0003', 'Product',      'Product Manager',            98000.00, '2019-11-20', 'ACTIVE'),
    ('David',  'Lee',      'david.lee@company.com',      '+1-555-100-0004', 'Design',       'UX Designer',                78000.00, '2022-01-10', 'ACTIVE'),
    ('Emily',  'Chen',     'emily.chen@company.com',     '+1-555-100-0005', 'Engineering',  'Staff Engineer',            130000.00, '2018-04-05', 'ACTIVE'),
    ('Frank',  'Brown',    'frank.brown@company.com',    '+1-555-100-0006', 'HR',           'HR Specialist',              65000.00, '2021-09-01', 'ACTIVE'),
    ('Grace',  'Kim',      'grace.kim@company.com',      '+1-555-100-0007', 'Finance',      'Financial Analyst',          72000.00, '2020-07-20', 'ACTIVE'),
    ('Henry',  'Taylor',   'henry.taylor@company.com',   '+1-555-100-0008', 'Engineering',  'DevOps Engineer',            95000.00, '2019-02-28', 'ON_LEAVE'),
    ('Iris',   'Davis',    'iris.davis@company.com',     '+1-555-100-0009', 'Marketing',    'Marketing Manager',          88000.00, '2017-12-01', 'ACTIVE'),
    ('Jack',   'Wilson',   'jack.wilson@company.com',    '+1-555-100-0010', 'Sales',        'Account Executive',          70000.00, '2023-03-15', 'INACTIVE');
