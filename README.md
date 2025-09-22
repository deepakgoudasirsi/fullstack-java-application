# Full-Stack Java Application

A comprehensive full-stack application built with Java Spring Boot backend and JavaScript frontend, demonstrating modern software engineering practices, scalable architecture, and complete software development lifecycle.

## 🚀 Features

### Backend (Java Spring Boot)
- **RESTful API** with comprehensive CRUD operations
- **JPA/Hibernate** for database operations with H2 in-memory database
- **Spring Security** with password encoding and CORS configuration
- **Comprehensive validation** using Bean Validation annotations
- **Modular architecture** with proper separation of concerns
- **Exception handling** with custom error responses
- **Audit trails** with created/updated timestamps

### Frontend (JavaScript)
- **Modern responsive UI** built with Bootstrap 5
- **Single Page Application** with dynamic content loading
- **Real-time data updates** with AJAX API calls
- **Interactive dashboards** with statistics and charts
- **Form validation** and user feedback
- **Mobile-responsive design** with modern UX patterns

### Testing
- **Unit tests** for service layer business logic
- **Integration tests** for REST API endpoints
- **Mock-based testing** with Mockito framework
- **Test profiles** with separate configuration
- **Comprehensive coverage** of all major functionality

## 🏗️ Architecture

### Backend Architecture
```
src/main/java/com/fullstack/
├── config/          # Configuration classes (Security, etc.)
├── controller/      # REST API controllers
├── model/          # JPA entities and enums
├── repository/     # Data access layer
├── service/        # Business logic layer
└── FullStackJavaApplication.java
```

### Frontend Architecture
```
src/main/resources/static/
├── index.html      # Main application page
├── styles.css      # Custom CSS styling
└── app.js         # JavaScript application logic
```

## 🛠️ Technologies Used

### Backend
- **Java 17** - Modern Java features and performance
- **Spring Boot 3.2.0** - Rapid application development framework
- **Spring Data JPA** - Data persistence abstraction
- **Spring Security** - Authentication and authorization
- **H2 Database** - In-memory database for development
- **Maven** - Dependency management and build tool
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework for tests

### Frontend
- **HTML5** - Semantic markup
- **CSS3** - Modern styling with custom properties
- **JavaScript ES6+** - Modern JavaScript features
- **Bootstrap 5** - Responsive UI framework
- **Font Awesome** - Icon library
- **Fetch API** - Modern HTTP client

## 📋 Prerequisites

- **Java 17** or higher
- **Maven 3.6** or higher
- **Web browser** with modern JavaScript support

## 🚀 Getting Started

### 1. Clone the Repository
```bash
git clone <repository-url>
cd Full-Stack\ Java\ Application
```

### 2. Build the Application
```bash
mvn clean install
```

### 3. Run the Application
```bash
mvn spring-boot:run
```

### 4. Access the Application
- **Web Interface**: http://localhost:8080
- **H2 Database Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa`
  - Password: `password`

## 🧪 Running Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Classes
```bash
mvn test -Dtest=UserServiceTest
mvn test -Dtest=TaskControllerTest
```

### Generate Test Coverage Report
```bash
mvn jacoco:report
```

## 📊 API Documentation

### User Management

#### Create User
```http
POST /api/users
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### Get All Users
```http
GET /api/users
```

#### Get User by ID
```http
GET /api/users/{id}
```

#### Update User
```http
PUT /api/users/{id}
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### Deactivate User
```http
DELETE /api/users/{id}
```

### Task Management

#### Create Task
```http
POST /api/tasks
Content-Type: application/json

{
  "userId": 1,
  "title": "Complete project",
  "description": "Finish the full-stack application",
  "priority": "HIGH",
  "dueDate": "2024-12-31T23:59:59"
}
```

#### Get Tasks by User
```http
GET /api/tasks/user/{userId}
```

#### Update Task Status
```http
PUT /api/tasks/{id}/status
Content-Type: application/json

{
  "status": "COMPLETED"
}
```

#### Get Task Statistics
```http
GET /api/tasks/user/{userId}/statistics
```

## 🗄️ Database Schema

### Users Table
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary key |
| username | VARCHAR(50) | Unique username |
| email | VARCHAR(255) | Unique email address |
| password | VARCHAR(255) | Encrypted password |
| first_name | VARCHAR(100) | User's first name |
| last_name | VARCHAR(100) | User's last name |
| role | VARCHAR(20) | User role (USER, ADMIN, MODERATOR) |
| is_active | BOOLEAN | Account status |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update timestamp |

### Tasks Table
| Column | Type | Description |
|--------|------|-------------|
| id | BIGINT | Primary key |
| title | VARCHAR(200) | Task title |
| description | TEXT | Task description |
| status | VARCHAR(20) | Task status (PENDING, IN_PROGRESS, COMPLETED, CANCELLED) |
| priority | VARCHAR(20) | Task priority (LOW, MEDIUM, HIGH, URGENT) |
| due_date | TIMESTAMP | Task due date |
| user_id | BIGINT | Foreign key to users table |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update timestamp |
| completed_at | TIMESTAMP | Completion timestamp |

## 🔧 Configuration

### Application Properties
The application uses Spring Boot's configuration system with the following key properties:

```properties
# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.username=sa
spring.datasource.password=password

# JPA Configuration
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# Security Configuration
spring.security.user.name=admin
spring.security.user.password=admin
```

## 🚀 Deployment

### Development Environment
The application is configured for development with:
- H2 in-memory database
- Detailed logging
- H2 console enabled
- CORS enabled for all origins

### Production Considerations
For production deployment, consider:
- Using a persistent database (PostgreSQL, MySQL)
- Configuring proper security settings
- Setting up logging levels
- Using environment-specific profiles
- Implementing proper error handling
- Setting up monitoring and health checks

## 📈 Performance Considerations

### Backend Optimizations
- **Lazy loading** for JPA relationships
- **Pagination** for large datasets
- **Caching** for frequently accessed data
- **Connection pooling** for database connections
- **Async processing** for long-running operations

### Frontend Optimizations
- **Minification** of CSS and JavaScript
- **Image optimization** and lazy loading
- **CDN usage** for static assets
- **Browser caching** strategies
- **Progressive Web App** features

## 🔒 Security Features

- **Password encryption** using BCrypt
- **CORS configuration** for cross-origin requests
- **Input validation** on all endpoints
- **SQL injection prevention** through JPA
- **XSS protection** through proper encoding
- **CSRF protection** (configurable)

## 🧪 Testing Strategy

### Unit Tests
- **Service layer** business logic testing
- **Repository layer** data access testing
- **Model validation** testing
- **Utility function** testing

### Integration Tests
- **REST API** endpoint testing
- **Database integration** testing
- **Security configuration** testing
- **End-to-end workflow** testing

### Test Coverage
- **Line coverage** > 80%
- **Branch coverage** > 70%
- **Method coverage** > 90%

## 📝 Development Guidelines

### Code Style
- Follow Java naming conventions
- Use meaningful variable and method names
- Add comprehensive JavaDoc comments
- Maintain consistent indentation and formatting

### Git Workflow
- Use feature branches for new development
- Write descriptive commit messages
- Create pull requests for code review
- Maintain a clean commit history

### Error Handling
- Use appropriate HTTP status codes
- Provide meaningful error messages
- Log errors for debugging
- Handle edge cases gracefully

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Bootstrap team for the responsive UI components
- Font Awesome for the icon library
- H2 Database for the in-memory database solution
