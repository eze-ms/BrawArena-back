# Brawl Arena — Plataforma de Combate y Montaje de Personajes Modulares

![Status](https://img.shields.io/badge/status-live-success?style=flat-square)
![MongoDB](https://img.shields.io/badge/database-MongoDB-brightgreen?style=flat-square)
![MySQL](https://img.shields.io/badge/database-MySQL-brightgreen?style=flat-square)
![Backend](https://img.shields.io/badge/backend-Java%2017-orange?style=flat-square)
![Framework](https://img.shields.io/badge/framework-Spring%20WebFlux-6db33f?style=flat-square)
![Tests](https://img.shields.io/badge/tests-JUnit%205-blue?style=flat-square)

---

## 📄 Descripción

**Brawl Arena** es una plataforma backend desarrollada con Java y Spring Boot (WebFlux), que implementa un backend reactivo con enfoque modular. Gestiona toda la lógica de una aplicación competitiva donde los jugadores ensamblan personajes 3D tipo LEGO, activan poderes estratégicos según sus piezas y compiten por precisión. El sistema incluye autenticación con JWT, control de builds y tokens, galería pública y panel administrativo, todo estructurado sobre una arquitectura escalable, segura y orientada a componentes.

---


## 🌐 Demo

🔗 [brawl-arena-vercel.app](https://brawl-arena-front.vercel.app/)

---

## ✨ Funcionalidades
La lógica del backend está desarrollada con Spring Boot y WebFlux, aplicando un enfoque reactivo, modular y desacoplado, que permite una gestión eficiente del estado, validaciones y control de flujo asincrónico. La arquitectura separa handlers, servicios, validadores y repositorios para mantener un sistema limpio, testeable y escalable.

### Jugadores
- Autenticación segura mediante JWT, validada con filtros personalizados.
- Registro/login mediante nickname, validaciones y control de errores consistentes.
- Montaje de personajes con validación de piezas falsas/especiales mediante servicios y validadores desacoplados.
- Activación dinámica de poderes, calculada por lógica central en ScoreCalculator.
- Gestión de tokens para desbloqueo de personajes con control de saldo reactivo.
- Compartición de modelos en MongoDB con normalización y soporte de imagen.
- Consulta de historial de builds y recuperación de builds pendientes.

### Administradores
- Destacar modelos en la galería pública (con lógica protegida por rol).
- Otorgar tokens a usuarios desde endpoints seguros y cacheados.
- Eliminación lógica de personajes y piezas asociadas.
- Acceso a modelos compartidos filtrados por personaje o usuario desde MongoDB.
---

## 💻 Tecnologías Utilizadas

### Backend
- **Java 17**
- **Spring Boot + WebFlux** (programación reactiva)
- **MongoDB** (colecciones: builds, personajes, piezas, modelos compartidos)
- **MySQL** (usuarios y autenticación)
- **JWT** para autenticación y autorización
- **Swagger/OpenAPI** para documentación
- **JUnit 5 + Mockito** para testing unitario

### Frontend (proyecto complementario)
- **React + TypeScript**
- **TailwindCSS**
- **Vite**
- **Zustand, React Query**

---

## 📋 Requisitos

- **Java 17** o superior
- **Node.js 18** o superior (para el frontend)
- **MongoDB Atlas** o local
- **MySQL 8+** con esquema preconfigurado
- **IDE compatible con Spring (recomendado: IntelliJ IDEA)**

---

## 🧱 Estructura del Proyecto

```bash
brawlarena_back/
├── common/              # Constantes, lógica de poderes y validadores
├── config/              # CORS, OpenAPI, puntuación reactiva
├── exception/           # Excepciones personalizadas y handler global
├── mongodb/
│   ├── entity, dto, handler, repository, routers, service
│   └── piezas, personajes, builds, modelos compartidos
├── mysql/
│   ├── entity, dto, handler, repository, router, service
│   └── usuarios y autenticación
├── security/            # JWT, filtros y configuración
├── test/                # Cobertura exhaustiva de lógica y controladores
│   ├── unitarios        # Auth, Users, Characters, Gallery, Build
│   └── integración      # Seguridad y flujo JWT
└── BrawlarenaApplication.java
```
---

## 🧪 Testing
El backend incluye testing unitario y de integración cubriendo:

- Casos exitosos, errores esperados y validaciones.
- Handlers: Auth, User, Character, Gallery, Build.
- Servicios: lógica de negocio en UserService, CharacterService, etc.
- Seguridad: validación del filtro JWT, roles y acceso.

Frameworks utilizados:

- JUnit 5
- Mockito
- Spring Test

---

## 🛠️ Instalación
### Backend

```bash
git clone https://github.com/eze-ms/BrawlArena-back.git
```
#### Iniciar Spring Boot

```bash
./mvn spring-boot:run

```

### Frontend
```bash
git clone https://github.com/eze-ms/BrawlArena-front.git

```

#### Instalar dependencias 
```bash
npm install
```

#### Iniciar servidor
```bash
npm run dev
```
---

© 2025. Proyecto desarrollado por Ezequiel Macchi Seoane