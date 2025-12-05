# Deployment Guide

## Backend Deployment (Heroku)

### Prerequisites
- Heroku CLI installed
- Git repository initialized
- Heroku account

### Steps

1. **Create Heroku App**
```bash
heroku create your-app-name
```

2. **Add PostgreSQL Database**
```bash
heroku addons:create heroku-postgresql:mini
```

3. **Set Environment Variables**
```bash
# JWT Secret (IMPORTANT: Use a strong secret!)
heroku config:set JWT_SECRET=your-super-secret-key-here-min-32-chars

# CORS Origins (Add your Vercel domain)
heroku config:set CORS_ORIGINS=https://your-app.vercel.app,https://www.your-app.vercel.app

# Spring Profile
heroku config:set SPRING_PROFILE=prod

# Get DATABASE_URL and convert to JDBC format
heroku config:get DATABASE_URL
# If DATABASE_URL is postgres://user:pass@host:port/db
# Set JDBC_DATABASE_URL as jdbc:postgresql://host:port/db
heroku config:set JDBC_DATABASE_URL=jdbc:postgresql://host:port/db
heroku config:set DB_USERNAME=user
heroku config:set DB_PASSWORD=pass
```

4. **Deploy**
```bash
git push heroku main
```

5. **Check Logs**
```bash
heroku logs --tail
```

### Database Migrations

Flyway migrations run automatically on startup:
- V1: Initial schema (users, user_friends, friend_requests)
- V2: Make email nullable
- V3: Add competitions tables

### Important Notes

- The app uses Java 21 (configured in system.properties)
- PostgreSQL is required for production
- Flyway manages database schema
- JWT_SECRET must be set as environment variable
- CORS_ORIGINS must include your frontend domain

## Frontend Deployment (Vercel)

### Prerequisites
- Vercel account
- GitHub repository

### Steps

1. **Connect Repository to Vercel**
   - Go to vercel.com
   - Click "New Project"
   - Import your GitHub repository

2. **Configure Build Settings**
   - Framework Preset: Create React App
   - Build Command: `npm run build`
   - Output Directory: `build`
   - Install Command: `npm install`

3. **Set Environment Variables**
```
REACT_APP_API_URL=https://your-heroku-app.herokuapp.com
```

4. **Deploy**
   - Vercel will automatically deploy on push to main/develop branch

### Important Notes

- Update CORS_ORIGINS in Heroku to include your Vercel domain
- Use HTTPS URLs for production
- Test the app after deployment

## Post-Deployment

1. **Test the API**
```bash
curl https://your-app.herokuapp.com/status
```

2. **Create Initial Users**
   - Register via the frontend
   - Or use API directly

3. **Monitor**
```bash
heroku logs --tail
```

## Troubleshooting

### Database Connection Issues
```bash
heroku pg:info
heroku config
```

### Application Crashes
```bash
heroku logs --tail
heroku restart
```

### Migration Issues
```bash
heroku run bash
./mvnw flyway:info
```

## Maintenance

### Database Backup
```bash
heroku pg:backups:capture
heroku pg:backups:download
```

### Update Dependencies
```bash
./mvnw clean install
git push heroku main
```
