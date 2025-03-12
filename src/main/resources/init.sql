CREATE TABLE Organization(
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    token VARCHAR(511) UNIQUE NOT NULL,
    registration_datetime TIMESTAMP DEFAULT NOW()
);

CREATE INDEX organization_token_index ON Organization(token);

CREATE TABLE Person(
    id SERIAL PRIMARY KEY,
    org_id INTEGER REFERENCES Organization(id) ON DELETE CASCADE NOT NULL,
    name VARCHAR(255) NOT NULL,
    token VARCHAR(511) UNIQUE NOT NULL,
    registration_datetime TIMESTAMP DEFAULT NOW()
);

CREATE INDEX person_org_id_index ON Person(org_id);
CREATE INDEX person_token_index ON Person(token);

CREATE TABLE Wardrobe(
    id SERIAL PRIMARY KEY,
    org_id INTEGER REFERENCES Organization(id) ON DELETE CASCADE NOT NULL,
    name VARCHAR(255) NOT NULL,
    hooks_count INTEGER NOT NULL CHECK (hooks_count > 0),
    token VARCHAR(511) UNIQUE NOT NULL,
    registration_datetime TIMESTAMP DEFAULT NOW()
);

CREATE INDEX wardrobe_org_id_index ON Wardrobe(org_id);
CREATE INDEX wardrobe_token_index ON Wardrobe(token);

CREATE TABLE Hook(
    id SERIAL PRIMARY KEY,
    wr_id INTEGER REFERENCES Wardrobe(id) ON DELETE CASCADE NOT NULL,
    real_number INTEGER NOT NULL CHECK (real_number > 0),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    is_free BOOLEAN NOT NULL DEFAULT TRUE,
    occupied_by INTEGER REFERENCES Person(id) DEFAULT NULL
);

CREATE INDEX hook_wr_id_index ON Hook(wr_id);
