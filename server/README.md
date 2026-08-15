# Notification Server

Sends FCM push notifications for new messages and room listings.

## Setup

### 1. Get Firebase Service Account Key

1. Go to [Firebase Console](https://console.firebase.google.com) → Project Settings → Service Accounts
2. Click "Generate New Private Key" → Download as `service-account.json`
3. Place the file in this `server/` directory

### 2. Install Dependencies

```bash
cd server
npm install
```

### 3. Run Locally

```bash
npm start
```

## Deploy to Render (Free)

1. Push this repo to GitHub
2. Go to [Render](https://render.com) → New → Web Service
3. Connect your GitHub repo
4. Settings:
   - **Name**: `room-sharing-notify`
   - **Root Directory**: `server`
   - **Build Command**: `npm install`
   - **Start Command**: `node index.js`
   - **Plan**: Free
5. Add Environment Variable:
   - Key: `GOOGLE_APPLICATION_CREDENTIALS`
   - Value: (paste the entire service-account.json content as a single string, or upload the file via Render's "Secret Files")
6. Deploy

The free tier keeps the server alive but may spin down after inactivity. It wakes up on the next request — but since this server uses a persistent Firebase listener (not HTTP), Render's free tier may not be ideal.

## Alternative: Deploy to Fly.io (Better for persistent listeners)

```bash
# Install flyctl
# cd server
# fly launch
# fly deploy
```

Fly.io's free tier keeps apps running 24/7 without spinning down.
