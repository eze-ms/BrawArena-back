# Brawl Arena — Plataforma de Combate y Montaje de Personajes Modulares

![Status](https://img.shields.io/badge/status-live-success?style=flat-square)
![MongoDB](https://img.shields.io/badge/database-MongoDB-brightgreen?style=flat-square)
![MySQL](https://img.shields.io/badge/database-MySQL-brightgreen?style=flat-square)
![Backend](https://img.shields.io/badge/backend-Java%2017-orange?style=flat-square)
![Framework](https://img.shields.io/badge/framework-Spring%20WebFlux-6db33f?style=flat-square)
![Tests](https://img.shields.io/badge/tests-JUnit%205-blue?style=flat-square)

---

## 📄 Descripción

**Brawl Arena** es una aplicación web competitiva en la que los jugadores ensamblan personajes 3D modulares tipo LEGO, activan poderes especiales según sus piezas, y compiten por tiempo y precisión. El sistema incluye desbloqueo de personajes mediante tokens, una galería pública de modelos compartidos y un completo panel de administración.

---

## ✨ Funcionalidades

### Jugadores
- Registro e inicio de sesión mediante nickname y contraseña.
- Montaje de personajes con piezas modulares (regulares, falsas y especiales).
- Activación de poderes basada en las piezas colocadas.
- Desbloqueo de personajes usando un sistema de tokens.
- Compartición de modelos en la galería pública.
- Consulta de builds previos y builds pendientes.

### Administradores
- Destacar modelos en la galería ("Jugador de la Semana").
- Otorgar tokens a jugadores manualmente.
- Eliminar personajes de la base de datos.
- Ver modelos compartidos por usuario o personaje.

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

#### Instalar dependencias del backend
```bash
npm install
```

#### Iniciar servidor
```bash
npm run dev
```
---



© 2024. Proyecto desarrollado por Ezequiel Macchi Seoane