-- Update passwords with correct BCrypt hashes
UPDATE users SET password = '$2a$10$i7/NDxL91qK2OP0Ge2zNSOaMH45t0kXKXsJf2UDpxcfAsOEvqZqIu' WHERE email = 'Formateur@smartek.com';
UPDATE users SET password = '$2a$10$TmA.8npRnzmEk6gXI3MtOuDJogORO2/zIcgiONmh83pHQl/KWxIqC' WHERE email = 'Learner@smartek.com';

-- Verify
SELECT user_id, first_name, email, role, LEFT(password, 30) as password_start FROM users WHERE email IN ('Formateur@smartek.com', 'Learner@smartek.com');
