# Heroku Deployment Instructies

## ⚠️ BELANGRIJK: Productie Data Behouden

De applicatie is nu geconfigureerd om **GEEN data te verwijderen** in productie.

### Development vs Production

| Mode | Profile | DbInitializer | Database Reset |
|------|---------|---------------|----------------|
| **Development** | `dev` | ✅ Actief | ✅ Reset bij elke start |
| **Production** | `prod` | ❌ Inactief | ❌ Data blijft behouden |

## Heroku Config Vars Instellen

### Verplichte Environment Variables:

```bash
# Set profile to production
heroku config:set SPRING_PROFILE=prod --app kompapp-backend

# Database wordt automatisch ingesteld door Heroku PostgreSQL addon
# DATABASE_URL wordt automatisch toegevoegd

# CORS origins (voeg je Vercel frontend URL toe)
heroku config:set CORS_ORIGINS=https://kompapp-frontend.vercel.app,https://kompapp-backend-e3cd8ff5eeb6.herokuapp.com --app kompapp-backend

# JWT Secret (gebruik een veilige random string!)
heroku config:set JWT_SECRET=your-very-secure-random-string-here --app kompapp-backend
```

### Controleer je config:
```bash
heroku config --app kompapp-backend
```

## Deployment

```bash
# Push naar main branch (Heroku deploy)
git push heroku develop:main

# Of als je auto-deploy hebt ingesteld via GitHub:
git push origin main
```

## Database Migratie

Als je de database structuur wijzigt:

1. **Development**: Wijzigingen worden automatisch toegepast (ddl-auto: update)
2. **Production**: Gebruik database migrations (Flyway/Liquibase) of handmatige updates

## Eerste Keer Productie Setup

Na eerste deployment naar Heroku:

1. ✅ Data is LEEG (geen test users)
2. ✅ Registreer je eerste admin user via frontend
3. ✅ Alle nieuwe registraties blijven behouden
4. ✅ Bij herstart blijft alle data intact

## Troubleshooting

### "Geen users in database na deployment"
Dit is normaal! DbInitializer draait NIET in productie. Registreer users via de frontend.

### "Data verdwijnt na herstart"
Check of `SPRING_PROFILE=prod` is ingesteld in Heroku config vars.

### "Database connection errors"
Check of Heroku PostgreSQL addon correct is toegevoegd en DATABASE_URL is ingesteld.
