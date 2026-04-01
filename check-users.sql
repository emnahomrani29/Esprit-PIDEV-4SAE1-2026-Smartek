-- Check if users exist in the database
SELECT 
    user_id, 
    first_name, 
    email, 
    role, 
    LEFT(password, 30) as password_hash_start,
    LENGTH(password) as password_length
FROM users 
WHERE email IN ('Formateur@smartek.com', 'Learner@smartek.com')
ORDER BY role DESC;

-- Check all users
SELECT 
    user_id, 
    first_name, 
    email, 
    role
FROM users
LIMIT 10;
