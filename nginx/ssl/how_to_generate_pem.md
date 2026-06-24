# Generating fullchain.pem and privkey.pem Files

There are several methods to generate SSL certificate files (fullchain.pem and privkey.pem) for your Media Server. Here are the most common approaches:

## Method 1: Using Let's Encrypt (Recommended for Production)

Let's Encrypt provides free, automated SSL certificates that are trusted by all major browsers.

### Prerequisites
- A registered domain name pointing to your server
- Port 80 open for verification (temporarily)

### Using Certbot (Standalone Mode)

1. Install Certbot:
   ```bash
   sudo apt update
   sudo apt install certbot
   ```

2. Generate certificates:
   ```bash
   sudo certbot certonly --standalone -d your-domain.com -d www.your-domain.com
   ```

3. Copy certificates to your Nginx directory:
   ```bash
   mkdir -p nginx/ssl
   sudo cp /etc/letsencrypt/live/your-domain.com/fullchain.pem nginx/ssl/
   sudo cp /etc/letsencrypt/live/your-domain.com/privkey.pem nginx/ssl/
   sudo chown $USER:$USER nginx/ssl/*
   ```

4. Set up automatic renewal:
   ```bash
   sudo systemctl enable certbot.timer
   sudo systemctl start certbot.timer
   ```

## Method 2: Using Certbot with Docker

If your server is already running with Docker:

```bash
docker run -it --rm \
  -v $PWD/nginx/ssl:/etc/letsencrypt \
  -v $PWD/certbot-webroot:/var/www/certbot \
  -p 80:80 \
  certbot/certbot certonly --standalone \
  --email your-email@example.com \
  --agree-tos \
  --no-eff-email \
  -d your-domain.com -d www.your-domain.com
```

## Method 3: Self-Signed Certificate (Development Only)

For development environments, you can create a self-signed certificate:

```bash
mkdir -p nginx/ssl
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout nginx/ssl/privkey.pem \
  -out nginx/ssl/fullchain.pem \
  -subj "/C=US/ST=State/L=City/O=Organization/CN=your-domain.com"
```

**Note**: Browsers will show a security warning when using self-signed certificates.

## Method 4: Using Existing Certificates

If you already have SSL certificates from another provider:

1. Convert certificates to PEM format if necessary:
   ```bash
   # Convert key to PEM
   openssl rsa -in your-certificate.key -out nginx/ssl/privkey.pem
   
   # Convert certificate to PEM
   openssl x509 -in your-certificate.crt -out nginx/ssl/fullchain.pem
   ```

2. If you have a certificate chain, concatenate them:
   ```bash
   cat your-certificate.crt intermediate.crt root.crt > nginx/ssl/fullchain.pem
   ```

## Testing Your SSL Configuration

After setting up your certificates, verify the Nginx configuration:

```bash
docker-compose exec nginx nginx -t
```

If everything is correctly configured, restart Nginx:

```bash
docker-compose restart nginx
```

## Security Considerations

- Keep your private key secure and restrict access permissions
- Regularly renew your certificates before expiration
- Use strong ciphers in your Nginx SSL configuration
- Enable HTTP Strict Transport Security (HSTS)

For production environments, Let's Encrypt is strongly recommended due to its automation, security, and browser trust.