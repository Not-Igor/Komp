# Heroku Deployment Guide

## Vereiste Environment Variables voor Heroku

Stel de volgende environment variables in via Heroku Dashboard of CLI:

### Kritieke Security Variables

```bash
# JWT Secret - genereer een sterke random string!
heroku config:set JWT_SECRET="your-super-secret-key-at-least-32-chars-long"

# JWT Expiration (in milliseconds, 86400000 = 24 uur)
heroku config:set JWT_EXPIRATION=86400000

# CORS Origins - pas aan naar je frontend URL
heroku config:set CORS_ORIGINS="https://your-frontend-app.herokuapp.com,http://localhost:3000"

# Database configuratie (Heroku PostgreSQL doet dit automatisch via DATABASE_URL)
# Security debug uitschakelen
heroku config:set SECURITY_DEBUG=false

# H2 Console uitschakelen in productie
heroku config:set H2_CONSOLE_ENABLED=false

# SQL logging uitschakelen in productie
heroku config:set SHOW_SQL=false

# DDL Auto op 'update' zetten voor productie (niet 'create-drop'!)
heroku config:set DDL_AUTO=update
```

### Voor PostgreSQL (aanbevolen voor productie)

Als je PostgreSQL gebruikt op Heroku:

```bash
# Heroku voorziet automatisch DATABASE_URL voor PostgreSQL
# Maar je moet mogelijk de dialect aanpassen:
heroku config:set HIBERNATE_DIALECT=org.hibernate.dialect.PostgreSQLDialect
```

## Heroku CLI Commands

```bash
# Login
heroku login

# Create app
heroku create your-app-name

# Add PostgreSQL (optioneel maar aanbevolen)
heroku addons:create heroku-postgresql:mini

# Deploy
git push heroku main

# View logs
heroku logs --tail

# Open app
heroku open
```

## Security Checklist voor Productie

- [ ] JWT_SECRET is een sterke, random gegenereerde string (minimaal 32 karakters)
- [ ] CORS_ORIGINS bevat alleen je echte frontend URL(s)
- [ ] H2_CONSOLE_ENABLED is false (of gebruik PostgreSQL)
- [ ] SECURITY_DEBUG is false
- [ ] SHOW_SQL is false (geen SQL queries in logs)
- [ ] DDL_AUTO is 'update' of 'validate' (niet 'create-drop')
- [ ] Database wachtwoorden staan in environment variables, niet in code
- [ ] .env files zijn in .gitignore

## Genereer sterke JWT Secret

```bash
# Met OpenSSL
openssl rand -base64 64

# Of met Python
python3 -c "import secrets; print(secrets.token_urlsafe(64))"
```
