-- Database initialization script for Full-Stack Java Application
-- This script sets up the initial database schema and sample data

-- Create database if not exists (handled by Docker)
-- CREATE DATABASE IF NOT EXISTS fullstackdb;

-- Use the database
-- \c fullstackdb;

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(20) DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN', 'MODERATOR')),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create tasks table
CREATE TABLE IF NOT EXISTS tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    priority VARCHAR(20) DEFAULT 'MEDIUM' CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    due_date TIMESTAMP,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_active ON users(is_active);
CREATE INDEX IF NOT EXISTS idx_tasks_user_id ON tasks(user_id);
CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);
CREATE INDEX IF NOT EXISTS idx_tasks_priority ON tasks(priority);
CREATE INDEX IF NOT EXISTS idx_tasks_due_date ON tasks(due_date);
CREATE INDEX IF NOT EXISTS idx_tasks_created_at ON tasks(created_at);

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers to automatically update updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_tasks_updated_at BEFORE UPDATE ON tasks
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert sample data
INSERT INTO users (username, email, password, first_name, last_name, role, is_active) VALUES
('admin', 'admin@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'Admin', 'User', 'ADMIN', TRUE),
('john.doe', 'john.doe@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'John', 'Doe', 'USER', TRUE),
('jane.smith', 'jane.smith@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'Jane', 'Smith', 'USER', TRUE),
('bob.wilson', 'bob.wilson@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'Bob', 'Wilson', 'MODERATOR', TRUE)
ON CONFLICT (username) DO NOTHING;

-- Insert sample tasks
INSERT INTO tasks (title, description, status, priority, due_date, user_id) VALUES
('Complete project documentation', 'Write comprehensive documentation for the full-stack application', 'PENDING', 'HIGH', CURRENT_TIMESTAMP + INTERVAL '7 days', 1),
('Implement user authentication', 'Add JWT-based authentication system', 'IN_PROGRESS', 'URGENT', CURRENT_TIMESTAMP + INTERVAL '3 days', 1),
('Design database schema', 'Create ERD and implement database tables', 'COMPLETED', 'MEDIUM', CURRENT_TIMESTAMP - INTERVAL '5 days', 2),
('Setup CI/CD pipeline', 'Configure automated testing and deployment', 'PENDING', 'HIGH', CURRENT_TIMESTAMP + INTERVAL '10 days', 2),
('Write unit tests', 'Create comprehensive test suite for all components', 'IN_PROGRESS', 'MEDIUM', CURRENT_TIMESTAMP + INTERVAL '5 days', 3),
('Code review and refactoring', 'Review code quality and refactor as needed', 'PENDING', 'LOW', CURRENT_TIMESTAMP + INTERVAL '14 days', 3),
('Performance optimization', 'Optimize application performance and database queries', 'PENDING', 'MEDIUM', CURRENT_TIMESTAMP + INTERVAL '21 days', 4),
('Security audit', 'Conduct security review and implement fixes', 'PENDING', 'HIGH', CURRENT_TIMESTAMP + INTERVAL '7 days', 4)
ON CONFLICT DO NOTHING;

-- Create views for common queries
CREATE OR REPLACE VIEW user_task_summary AS
SELECT 
    u.id as user_id,
    u.username,
    u.email,
    u.first_name,
    u.last_name,
    COUNT(t.id) as total_tasks,
    COUNT(CASE WHEN t.status = 'PENDING' THEN 1 END) as pending_tasks,
    COUNT(CASE WHEN t.status = 'IN_PROGRESS' THEN 1 END) as in_progress_tasks,
    COUNT(CASE WHEN t.status = 'COMPLETED' THEN 1 END) as completed_tasks,
    COUNT(CASE WHEN t.status = 'CANCELLED' THEN 1 END) as cancelled_tasks,
    COUNT(CASE WHEN t.due_date < CURRENT_TIMESTAMP AND t.status NOT IN ('COMPLETED', 'CANCELLED') THEN 1 END) as overdue_tasks
FROM users u
LEFT JOIN tasks t ON u.id = t.user_id
WHERE u.is_active = TRUE
GROUP BY u.id, u.username, u.email, u.first_name, u.last_name;

-- Create view for task statistics
CREATE OR REPLACE VIEW task_statistics AS
SELECT 
    DATE(created_at) as date,
    COUNT(*) as total_created,
    COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed,
    COUNT(CASE WHEN status = 'PENDING' THEN 1 END) as pending,
    COUNT(CASE WHEN status = 'IN_PROGRESS' THEN 1 END) as in_progress,
    COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) as cancelled
FROM tasks
WHERE created_at >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY DATE(created_at)
ORDER BY date DESC;

-- Grant permissions (if needed)
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO fullstack_user;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO fullstack_user;
-- GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO fullstack_user;

-- Display initialization message
DO $$
BEGIN
    RAISE NOTICE 'Full-Stack Java Application database initialized successfully!';
    RAISE NOTICE 'Sample data inserted: % users, % tasks', 
        (SELECT COUNT(*) FROM users), 
        (SELECT COUNT(*) FROM tasks);
END $$;
