# Keycloak OAuth2 Configuration Guide

This document outlines the steps to configure Keycloak for OAuth 2.0 authentication within the Saga Pattern Spring Boot project.

## 1. Access the Admin Console
- **URL**: [http://localhost:9091](http://localhost:9091)
- **Log in**:
    - **Username**: `admin`
    - **Password**: `admin`

## 2. Create the Realm
1. Hover over the realm selector in the top-left corner and click **Create Realm**.
2. **Realm Name**: `saga-realm`  
   > [!IMPORTANT]
   > The name must be exactly `saga-realm` to match the `issuer-uri` configured in the microservices.
3. Click **Create**.

## 3. Configure the Client
1. Navigate to **Clients** > **Create client**.
2. **Settings**:
    - **Client ID**: `saga-client`
    - **Client Type**: `OpenID Connect`
3. Click **Next**.
4. **Capability Config**:
    - Enable **Standard Flow** (for browser-based login).
    - Enable **Direct Access Grants** (allows getting tokens via password in Postman).
5. Click **Save**.
6. **Access Settings**:
    - **Valid redirect URIs**: `*` (for local development testing).
    - **Web origins**: `*`
7. Click **Save**.

## 4. Create a Test User
1. Navigate to **Users** > **Add user**.
2. **Username**: `test-user`.
3. Click **Create**.
4. Go to the **Credentials** tab and click **Set password**.
5. Set password to `password123`.
6. Toggle **Temporary** to **OFF**.
7. Click **Save**.

## 5. Testing the Configuration
You can verify the setup by fetching a JWT token using Postman or `curl`.

### Token Request (POST)
- **Endpoint**: `http://localhost:9091/realms/saga-realm/protocol/openid-connect/token`
- **Body (x-www-form-urlencoded)**:
    - `client_id`: `saga-client`
    - `username`: `test-user`
    - `password`: `password123`
    - `grant_type`: `password`

### Using the Token
Copy the `access_token` from the response and use it as a **Bearer Token** to call the API Gateway:
- **Header**: `Authorization: Bearer <YOUR_TOKEN>`
- **Gateway URL**: `http://localhost:8080/users/...`
