# FluxGuard - Coding Platform Development Guidelines

## Project Name

Coding Platform (Demo Application for FluxGuard)

---

# Main Objective

The Coding Platform is NOT the main project.

It exists only to generate realistic API traffic that will later be governed by FluxGuard.

Every future API must eventually pass through FluxGuard.

---

# Technology Stack

Backend

- Java
- Spring Boot 4.1
- Spring Security
- Spring Data JPA
- PostgreSQL

Frontend

- Thymeleaf
- HTML5
- CSS3
- JavaScript

Build Tool

- Maven

IDE

- VS Code

---

# Coding Standards

Always follow Layered Architecture.

```
Controller

↓

Service

↓

Repository

↓

Database
```

Never place business logic inside Controllers.

Controllers only receive requests and return responses.

Business logic belongs inside Services.

Repositories only interact with PostgreSQL.

---

# Authentication

Already Completed.

Implemented

- User Registration
- Login
- Logout
- BCrypt Password Encryption
- Spring Security
- DTO Pattern

Do NOT redesign authentication.

Reuse existing implementation.

---

# UI Theme

Inspired by

- LeetCode
- Modern Coding Platforms

Theme

- White
- Blue Accent
- Minimal
- Professional

Avoid

- Glassmorphism
- Heavy Animations
- Dark Theme

---

# Folder Structure

```
controller/

HomeController

AuthController

DashboardController

ContestController

ProblemController

SubmissionController

LeaderboardController

ProfileController

FluxGuardController
```

```
service/

DashboardService

ContestService

ProblemService

SubmissionService

LeaderboardService

ProfileService
```

```
repository/

ContestRepository

ProblemRepository

SubmissionRepository
```

```
templates/

dashboard.html

contests.html

problems.html

leaderboard.html

profile.html

fluxguard.html
```

```
static/css/

dashboard.css

contest.css

problem.css

profile.css

fluxguard.css
```

---

# Development Order

Never change this order.

Authentication ✅

↓

Dashboard

↓

Contest Module

↓

Problem Module

↓

Submission Module

↓

Leaderboard

↓

Profile

↓

FluxGuard Monitor

---

# Dashboard Requirements

Professional Navbar

Logo

Dashboard

Contests

Problems

Leaderboard

Profile

FluxGuard

Logout

Hero Section

Statistics Cards

Recent Activity

Upcoming Contests

Active Contest

Responsive Design

---

# Contest Module

Users should

View Contests

Join Contest

View Contest Details

Future API

POST /contest/join

---

# Problem Module

Users should

View Problems

Open Problem

Read Description

Future API

GET /problem/{id}

---

# Submission Module

Users should

Submit Code

View Submission Status

Submission History

Future API

POST /submission

---

# Leaderboard

Users should

View Rankings

Sort Rankings

Future API

GET /leaderboard

---

# Profile

Users should

View Profile

Update Profile

Statistics

Future API

GET /profile

---

# FluxGuard

DO NOT implement FluxGuard logic here.

Only create UI navigation.

Later FluxGuard will display

JWT Validation

Authorization

Redis Rate Limiting

Workflow Validation

Request Logs

Traffic Monitoring

Load Balancing

Blocked Requests

API Analytics

---

# Coding Rules

Always explain before generating code.

Generate only ONE file at a time.

Wait for confirmation.

Never overwrite working code unless necessary.

When modifying files,
always generate the COMPLETE updated file.

Use constructor injection.

Use DTOs.

Use SOLID principles.

Use enterprise coding practices.

No deprecated Spring APIs.

---

# Final Goal

Build a production-quality Coding Contest Platform that later integrates seamlessly with FluxGuard.

Every future feature should be designed considering future API interception by FluxGuard.