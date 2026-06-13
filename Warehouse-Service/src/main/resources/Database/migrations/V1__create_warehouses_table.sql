-- Installing UUID Extension
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Create the table
CREATE TABLE warehouse (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    warehouse_name VARCHAR(255) NOT NULL,
    location GEOGRAPHY(Point, 4326) NOT NULL,
    city VARCHAR(100) NOT NULL,
    capacity INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP
);

-- Create the index on the warehouse table
CREATE INDEX IDX_GIST_LOCATION ON warehouse
USING GIST(location);
