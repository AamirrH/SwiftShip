-- Data Seeding
INSERT INTO warehouse (
    warehouse_name,
    location,
    city,
    capacity,
    active
)
VALUES
    (
        'Pune Koregaon Park Hub',
        ST_SetSRID(ST_MakePoint(73.8939, 18.5362), 4326)::geography,
        'Pune',
        500,
        true
    ),
    (
        'Pune Hinjewadi Hub',
        ST_SetSRID(ST_MakePoint(73.7389, 18.5912), 4326)::geography,
        'Pune',
        800,
        true
    ),
    (
        'Pune Kothrud Hub',
        ST_SetSRID(ST_MakePoint(73.8077, 18.5074), 4326)::geography,
        'Pune',
        400,
        true
    ),
    (
        'Mumbai Andheri Hub',
        ST_SetSRID(ST_MakePoint(72.8697, 19.1136), 4326)::geography,
        'Mumbai',
        600,
        true
    ),
    (
        'Mumbai Bandra Hub',
        ST_SetSRID(ST_MakePoint(72.8295, 19.0596), 4326)::geography,
        'Mumbai',
        450,
        true
    );