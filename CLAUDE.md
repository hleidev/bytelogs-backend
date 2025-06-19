# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ByteLogs is a forum backend system built with Spring Boot 3.2.3/3.5.0, using a clean multi-module architecture. The project uses JDK 21, MyBatis-Plus for ORM, Redis for caching, and MySQL 8.0+ for data persistence.

## Module Architecture

The project follows a layered multi-module structure:

- **bytelogs-api**: Contains DTOs, VOs, enums, request/response models, and shared interfaces
- **bytelogs-core**: Houses core utilities, configurations, security, context management, and infrastructure components
- **bytelogs-service**: Implements business logic, data access layer (DAO/Repository), services, and entity mappings
- **bytelogs-web**: Contains REST controllers, filters, security configurations, and web layer components

**Dependency Flow**: bytelogs-web → bytelogs-service → bytelogs-core ← bytelogs-api

## Development Commands

### Build and Run
```bash
# Clean build (skip tests)
mvn clean install -DskipTests

# Run application
java -jar bytelogs-web/target/bytelogs-web-0.0.1-SNAPSHOT.jar

# Or run from IDE: main class is QuickWebApplication in bytelogs-web module
```

### Testing
```bash
# Run all tests
mvn test

# Run specific module tests
mvn test -pl bytelogs-service
```

### Environment Profiles
- **dev** (default): Uses local MySQL/Redis, configuration in `bytelogs-web/src/main/resources-env/dev/`
- **prod**: Production configuration

## Key Technologies & Patterns

### Core Stack
- **Spring Boot 3.5.0** with JDK 21
- **MyBatis-Plus 3.5.12** for ORM with automatic CRUD
- **MapStruct 1.5.5** for entity/DTO mapping (see `*StructMapper` classes)
- **JWT** for authentication (jjwt 0.11.5)
- **SpringDoc OpenAPI 3** for API documentation
- **Flexmark** for Markdown processing

### Architecture Patterns
- **Repository Pattern**: Each entity has DAO (data access) and Service layers
- **DTO/VO Separation**: Request/Response models in bytelogs-api, entities in bytelogs-service
- **MapStruct Converters**: Automatic mapping between entities and DTOs (e.g., `ArticleStructMapper`)
- **Custom Annotations**: `@RequiresLogin`, `@RequiresAdmin` for security
- **Context Management**: `ReqInfoContext` for request-scoped data

### Database Schema
Core entities: `user_account`, `user_info`, `article`, `article_detail`, `comment`, `category`, `tag`
- Uses soft deletes (`deleted` field)
- Follows `create_time`/`update_time` audit pattern
- Composite primary keys with auto-increment IDs

## Code Conventions

### Naming Patterns
- **Entities**: `*DO` classes in `repository/entity/`
- **DTOs**: `*DTO` classes in `api/model/vo/*/dto/`
- **VOs**: `*VO` classes in `api/model/vo/*/vo/`
- **Requests**: `*Req` classes in `api/model/vo/*/req/`
- **Services**: Interface in `service/` package, implementation in `service/impl/`
- **DAOs**: `*DAO` classes in `repository/dao/`
- **Mappers**: MyBatis `*Mapper` interfaces in `repository/mapper/`

### Package Structure
```
top.harrylei.forum.{module}.{domain}/{layer}/
- {domain}: article, user, auth, etc.
- {layer}: service, repository, converted, etc.
```

### Security & Validation
- Use `@RequiresLogin` for authenticated endpoints
- Use `@RequiresAdmin` for admin-only endpoints  
- Validate requests with `@Valid` and Jakarta validation annotations
- JWT tokens handled by `JwtAuthenticationFilter`

## Configuration

### Application Configuration
- Main config: `bytelogs-web/src/main/resources/application.yml`
- Environment-specific: `bytelogs-web/src/main/resources-env/{profile}/application-dal.yml`
- Database name configured via `${database.name}` placeholder
- JWT settings: issuer, secret, expiration in application.yml

### Database Connection
- Default database: `byte_logs`
- Connection configured in `application-dal.yml` per environment
- Redis used for caching with connection pool configuration

## Common Development Tasks

### Adding New Domain Entity
1. Create entity DO in `bytelogs-service/repository/entity/`
2. Create DTO/VO in `bytelogs-api/model/vo/{domain}/`
3. Create DAO in `bytelogs-service/repository/dao/`
4. Create MyBatis Mapper in `bytelogs-service/repository/mapper/`
5. Create MapStruct converter in `bytelogs-service/converted/`
6. Implement service interface and implementation
7. Create controller in `bytelogs-web/{domain}/`

### Running the Application
- Ensure MySQL and Redis are running locally
- Database connection: `localhost:3306/byte_logs`
- Redis connection: `localhost:6379/12`
- Default port: 8080
- API documentation available at `/swagger-ui.html`

### Authentication Flow
- Login via `AuthController` generates JWT token
- Token required for endpoints marked with `@RequiresLogin`
- User context available via `ReqInfoContext.getReqInfo()`
- Admin operations require `@RequiresAdmin` annotation