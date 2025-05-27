-- V2__add_balance_to_users.sql
-- Add balance column to users table

-- Добавляем поле balance со значением по умолчанию 0
ALTER TABLE users
ADD COLUMN balance DECIMAL(10, 2) NOT NULL DEFAULT 0.00;

-- Обновляем существующие записи
UPDATE users SET balance = 0.00 WHERE balance IS NULL;