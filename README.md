# EVNTR App

- An Android mobile application for creating, managing, and participating in events of all types.
- This project demonstrates integration between a native Android frontend and a Flask RESTful API backend, with data persistence via PostgreSQL.

## Overview

EventManager allows users to:
- Browse, filter, and view available events
- Create and edit events with title, description, date, capacity, and price
- Register or unregister for events
- Manage their personal account and event history
- View a list of participants (for event creators)
- Navigate through a clean, responsive UI in both portrait and landscape modes

All user data and event information is stored in a PostgreSQL database, accessed via a REST API hosted on Vercel.

## Tech Stack

- Frontend: Android (Java + XML)
- Backend: Python (Flask)
- Database: PostgreSQL
- API Hosting: Vercel
- Networking: Retrofit (REST client)
- Authentication: JWT (token-based authentication)
- Persistence: SharedPreferences (local), PostgreSQL (remote)

## Features

### User
- Register, log in, update, or delete account
- Encrypted password handling
- Session persistence via SharedPreferences

### Event Management
- Create, edit, or delete your own events
- Enforced validation rules (e.g., future dates, available spots)
- Filter events by type, registration status, or ownership
- View event details and register/unregister accordingly
- View registered participants (if you're the creator)

### UI and Usability
- Modern, native Android components
- Responsive layouts (portrait & landscape)
- Data validation on both client and server side

## REST API
- Developed in Flask
- JWT-protected routes
- Supports all CRUD operations for users and events
- Deployed via Vercel with environment variable configuration

## Database Schema

- `users`: stores user credentials and metadata
- `events`: stores all event data (title, description, date, etc.)
- `event_booked`: pivot table linking users and events (many-to-many)

Entity relationships are designed to ensure referential integrity and prevent invalid operations (e.g., deleting events with active bookings).

## Project Status

- This project began as a personal hobby to explore event management systems.
- Development was paused after evaluating the project's relevance and shifting focus toward **QuickFix**, a more impactful and practical solution (also available on my GitHub).

## Folder Structure

- `/android_app/` — Android source code (Java/XML)
- `/flask_api/` — Flask REST API backend
- `/docs/` — ERD, API documentation, user manual, and deployment guide

## License

Developed by Gabriel Lopes.
