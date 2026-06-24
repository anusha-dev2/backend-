#!/bin/bash

# Media Server Deployment Script
# This script deploys the Media Server application using Docker Compose

# Configuration
APP_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd )"
LOG_FILE="/var/log/media-server/deploy.log"
ENV_FILE="$APP_DIR/.env"

# Ensure log directory exists
mkdir -p "$(dirname "$LOG_FILE")"

echo "Starting deployment: $(date)" | tee -a "$LOG_FILE"
echo "Working directory: $APP_DIR" | tee -a "$LOG_FILE"

# Function to check if Docker and Docker Compose are installed
check_dependencies() {
    echo "Checking dependencies..." | tee -a "$LOG_FILE"
    
    if ! command -v docker &> /dev/null; then
        echo "Docker is not installed. Please install Docker first." | tee -a "$LOG_FILE"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        echo "Docker Compose is not installed. Please install Docker Compose first." | tee -a "$LOG_FILE"
        exit 1
    fi
    
    echo "All dependencies are installed." | tee -a "$LOG_FILE"
}

# Function to check if .env file exists
check_env_file() {
    echo "Checking environment file..." | tee -a "$LOG_FILE"
    
    if [ ! -f "$ENV_FILE" ]; then
        echo "Environment file (.env) not found. Creating a sample one..." | tee -a "$LOG_FILE"
        
        # Create a sample .env file
        cat > "$ENV_FILE" << EOF
# MongoDB
MONGO_ROOT_PASSWORD=change_this_password

# JWT
APP_JWT_SECRET=change_this_to_a_secure_random_string

# Stripe
STRIPE_API_KEY=your_stripe_api_key
STRIPE_WEBHOOK_SECRET=your_stripe_webhook_secret

# OAuth2
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
GITHUB_CLIENT_ID=your_github_client_id
GITHUB_CLIENT_SECRET=your_github_client_secret

# Domain
APP_DOMAIN=https://your-domain.com
EOF
        
        echo "Please edit the .env file with your actual configuration values." | tee -a "$LOG_FILE"
        exit 1
    fi
    
    echo "Environment file found." | tee -a "$LOG_FILE"
}

# Function to create required directories
create_directories() {
    echo "Creating required directories..." | tee -a "$LOG_FILE"
    
    # Create directories for content, logs, etc.
    mkdir -p /var/media-server/content
    mkdir -p /var/log/media-server
    mkdir -p /var/backups/media-server
    
    # Set proper permissions
    chown -R $(whoami) /var/media-server
    chown -R $(whoami) /var/log/media-server
    chown -R $(whoami) /var/backups/media-server
    
    echo "Directories created successfully." | tee -a "$LOG_FILE"
}

# Function to pull the latest code (if using git)
pull_latest_code() {
    echo "Pulling latest code..." | tee -a "$LOG_FILE"
    
    # If using git, uncomment the following lines
    # cd "$APP_DIR"
    # git pull origin main
    
    echo "Code updated successfully." | tee -a "$LOG_FILE"
}

# Function to build and deploy the application
deploy_application() {
    echo "Building and deploying application..." | tee -a "$LOG_FILE"
    
    cd "$APP_DIR"
    
    # Stop any running containers
    docker-compose down
    
    # Build the containers
    docker-compose build
    
    # Start the application in detached mode
    docker-compose up -d
    
    if [ $? -eq 0 ]; then
        echo "Application deployed successfully." | tee -a "$LOG_FILE"
    else
        echo "Failed to deploy application. Check the logs for more details." | tee -a "$LOG_FILE"
        exit 1
    fi
}

# Function to verify the deployment
verify_deployment() {
    echo "Verifying deployment..." | tee -a "$LOG_FILE"
    
    # Wait for a few seconds to let the containers start
    sleep 10
    
    # Check if the containers are running
    if docker-compose ps | grep -q "Up"; then
        echo "Containers are running." | tee -a "$LOG_FILE"
    else
        echo "Some containers are not running. Check the logs for more details." | tee -a "$LOG_FILE"
        exit 1
    fi
    
    # Optional: Add a health check to verify the API is responding
    # if curl -s http://localhost:9000/api/actuator/health | grep -q "UP"; then
    #     echo "API is healthy." | tee -a "$LOG_FILE"
    # else
    #     echo "API is not responding. Check the logs for more details." | tee -a "$LOG_FILE"
    #     exit 1
    # fi
    
    echo "Deployment verified successfully." | tee -a "$LOG_FILE"
}

# Main script execution
check_dependencies
check_env_file
create_directories
pull_latest_code
deploy_application
verify_deployment

echo "Deployment completed: $(date)" | tee -a "$LOG_FILE"
echo "-----------------------------------------" | tee -a "$LOG_FILE"

# Show the status of the containers
docker-compose ps
