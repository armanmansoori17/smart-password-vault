# Smart Password Vault (Java + JDBC + MySQL)

Console-based credential vault with **multi-user** support, **AES encryption**, and full CRUD, following the **DAO pattern**.

## Features

- **User registration & login** ? Master username/password + phone with **SMS OTP (Twilio)**
- **Store credentials** ? Site/App, username, password, category, notes
- **Encryption** ? Passwords never stored in plain text (AES)
- **CRUD** ? Add, view, search, update, delete credentials
- **Password strength checker** ? Weak / Medium / Strong
- **Auto-generated password** ? SecureRandom 12-character password
- **Duplicate detection** ? Prevents same site+username per user
- **Last access tracking** ? Audit when credentials are viewed
- **Login attempt limit** ? 3 failed attempts = temporary lock
- **Export vault** ? Encrypted backup to `vault_backup.txt`

## Prerequisites

- Java 17+
- Maven
- MySQL 8+

## Database Setup

Run the SQL script in MySQL Workbench / XAMPP / CLI:

```bash
mysql -u root -p < sql/setup.sql
```

Or run manually:

```sql
CREATE DATABASE IF NOT EXISTS password_vault;
USE password_vault;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    master_password VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    phone_verified TINYINT(1) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE vault_entries (
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
```

## Configuration

Edit `src/main/resources/application.properties`:

```
db.url=jdbc:mysql://localhost:3306/password_vault?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=your_password

# Twilio SMS (for OTP). Sign up at https://www.twilio.com/try-twilio
twilio.accountSid=ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
twilio.authToken=your_auth_token
twilio.fromNumber=+1234567890
```

### Twilio setup (SMS OTP)

1. Create a [Twilio](https://www.twilio.com/try-twilio) account.
2. In [Console](https://console.twilio.com): copy **Account SID** and **Auth Token** into `application.properties`.
3. Get a phone number: **Phone Numbers ? Manage ? Buy a number** (one with SMS capability).
4. Set `twilio.fromNumber` to that number in E.164 format (e.g. `+15551234567`).
5. When registering, enter the **recipient** phone in E.164 (e.g. `+919876543210` for India). You can omit the leading `+`; the app will add it.

## Build

```bash
mvn package
```

## Run

```bash
java -jar target/smart-password-vault-1.0.0.jar
```

Or run from IDE with main class `vault.Main`.

## Menu

**Unauthenticated:** 1. Login | 2. Register | 3. Exit

**Authenticated:** 1. Add Credential | 2. View All | 3. Search | 4. Update | 5. Delete | 6. Generate Password | 7. Export Vault | 8. Logout

## Architecture (DAO Pattern)

- **DAO layer** ? `UserDao`, `VaultEntryDao` (pure SQL, no business logic)
- **Service layer** ? `UserService`, `VaultService` (encryption, validation, business rules)
- **Presentation** ? `Main` (console menu)
