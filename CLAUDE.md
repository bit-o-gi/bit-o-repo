# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 3.5.6 application named "concert" (org.bito.concert) using Java 21. The project is built with Gradle and follows standard Spring Boot conventions.

## Build and Development Commands

### Building the Project
```bash
./gradlew build
```

On Windows:
```bash
gradlew.bat build
```

### Running the Application
```bash
./gradlew bootRun
```

On Windows:
```bash
gradlew.bat bootRun
```

### Running Tests
```bash
# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests org.bito.concert.ConcertApplicationTests

# Run with verbose output
./gradlew test --info
```

### Cleaning Build Artifacts
```bash
./gradlew clean
```

## Architecture

- **Package Structure**: `org.bito.concert`
- **Main Application**: `ConcertApplication.java` - Standard Spring Boot entry point with `@SpringBootApplication`
- **Java Version**: Java 21 (configured via toolchain)
- **Dependencies**:
  - Spring Boot Starter Web (REST API support)
  - Spring Boot Starter Test (JUnit 5 platform)
- **Test Framework**: JUnit 5 (JUnit Platform)

## Project Configuration

- **Application Name**: "concert" (defined in application.properties)
- **Spring Boot Version**: 3.5.6
- **Dependency Management**: Spring's dependency management plugin
- **Build Output**: `build/` directory (standard Gradle layout)