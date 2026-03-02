-- Smart Password Vault - MySQL setup script
-- Run in MySQL Workbench / XAMPP / CLI

CREATE DATABASE IF NOT EXISTS password_vault;
USE password_vault;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    master_password VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    phone_verified TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS vault_entries (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    site_name VARCHAR(100) NOT NULL,
    login_username VARCHAR(100) NOT NULL,
    encrypted_password VARCHAR(255) NOT NULL,
    category VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_accessed TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_site_login (user_id, site_name, login_username)
);
