CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TYPE user_role AS ENUM ('CUSTOMER', 'HELPER', 'ADMIN');
CREATE TYPE booking_status AS ENUM ('SEARCHING', 'ASSIGNED', 'EN_ROUTE', 'STARTED', 'COMPLETED', 'PAID', 'CANCELLED', 'NO_HELPER');

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    phone VARCHAR(20) UNIQUE NOT NULL,
    totp_secret VARCHAR(64),
    role VARCHAR(20) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE helper_locations (
    helper_id UUID PRIMARY KEY REFERENCES users(id),
    location GEOGRAPHY(Point, 4326) NOT NULL,
    is_online BOOLEAN DEFAULT FALSE,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) NOT NULL,
    hourly_rate NUMERIC(10, 2) NOT NULL,
    minimum_hours INT DEFAULT 1,
    commission_pct NUMERIC(5, 2) DEFAULT 15.00
);

CREATE TABLE bookings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id UUID REFERENCES users(id),
    helper_id UUID REFERENCES users(id),
    category_id UUID REFERENCES categories(id),
    status VARCHAR(30) NOT NULL,
    rate_snapshot NUMERIC(10,2) NOT NULL,
    commission_snapshot NUMERIC(5,2) NOT NULL,
    destination GEOGRAPHY(Point, 4326) NOT NULL,
    otp_start VARCHAR(6),
    started_at TIMESTAMP WITH TIME ZONE,
    ended_at TIMESTAMP WITH TIME ZONE,
    total_amount NUMERIC(10,2),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_helper_loc_geo ON helper_locations USING GIST (location);
