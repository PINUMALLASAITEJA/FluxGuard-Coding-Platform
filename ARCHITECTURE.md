# FluxGuard - System Architecture

## Project Overview

The project consists of two independent Spring Boot applications.

1. Coding Platform
2. FluxGuard

The Coding Platform acts as the demonstration application.

FluxGuard acts as the API Governance Layer.

Eventually every API request generated from the Coding Platform will pass through FluxGuard.

---

# Overall Architecture

Browser

↓

Coding Platform Frontend

↓

FluxGuard

↓

Coding Platform Backend

↓

PostgreSQL

---

# Coding Platform Architecture

Client

↓

Controller

↓

Service

↓

Repository

↓

PostgreSQL

Every module follows the same layered architecture.

Business logic must exist only inside Services.

Repositories only interact with the database.

Controllers should remain thin.

---

# Project Modules

Authentication

Dashboard

Contest Management

Problem Management

Submission Management

Leaderboard

Profile

FluxGuard Monitor

---

# Authentication Flow

Register

↓

Registration Controller

↓

UserAccountService

↓

BCrypt Password Encoder

↓

UserAccount Repository

↓

PostgreSQL

--------------------------------

Login

↓

Spring Security

↓

UserDetailsService

↓

Authentication Provider

↓

Dashboard

---

# Dashboard Flow

Dashboard Request

↓

Dashboard Controller

↓

Dashboard Service

↓

Repository

↓

Database

---

# Contest Flow

Dashboard

↓

Join Contest

↓

Contest Controller

↓

Contest Service

↓

Contest Repository

↓

Database

Future API

POST /contest/join

---

# Problem Flow

Problems

↓

Problem Controller

↓

Problem Service

↓

Problem Repository

↓

Database

Future API

GET /problem/{id}

---

# Submission Flow

Submit Code

↓

Submission Controller

↓

Submission Service

↓

Submission Repository

↓

Database

Future API

POST /submission

---

# Leaderboard Flow

Leaderboard

↓

Leaderboard Controller

↓

Leaderboard Service

↓

Repository

↓

Database

Future API

GET /leaderboard

---

# Profile Flow

Profile

↓

Profile Controller

↓

Profile Service

↓

Repository

↓

Database

Future API

GET /profile

---

# FluxGuard Architecture

Future Request Flow

Client

↓

FluxGuard

↓

JWT Authentication

↓

Role Based Authorization

↓

Redis Rate Limiting

↓

Workflow Validation

↓

Request Logging

↓

Traffic Monitoring

↓

Round Robin Load Balancer

↓

Coding Platform Backend

↓

Response

---

# Database Entities

Current

UserAccount

Future

Contest

Problem

Submission

Leaderboard

Profile

RequestLog

---

# Package Responsibilities

controller

Receive HTTP Requests

Return Views / Responses

--------------------------------

service

Business Logic

--------------------------------

repository

Database Operations

--------------------------------

model

JPA Entities

--------------------------------

dto

Data Transfer Objects

--------------------------------

config

Spring Configuration

--------------------------------

security

Authentication

Authorization

---

# Development Principles

Follow SOLID Principles

Use Constructor Injection

Never place business logic inside Controllers

Never access Repository directly from Views

Keep Services reusable

Use DTOs between Controller and Service

Keep UI separated from backend logic

---

# Final Goal

Build a production-quality Coding Contest Platform whose APIs are completely governed by FluxGuard, demonstrating enterprise-level API authentication, authorization, rate limiting, workflow validation, monitoring, and traffic management.