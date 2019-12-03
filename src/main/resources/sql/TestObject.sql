CREATE TABLE TestObject
(
    id             INTEGER NOT NULL REFERENCES JakonObject (id) ON DELETE CASCADE,
    string         VARCHAR(255),
    boolean        BOOLEAN,
    `double`       DOUBLE,
    `integer`      INTEGER,
    user_id        INTEGER NOT NULL REFERENCES JakonUser (id) ON DELETE CASCADE,
    date           DATETIME,
    localDate      DATETIME,
    self_id        INTEGER NOT NULL REFERENCES AclRule (id) ON DELETE CASCADE,
    enum           VARCHAR(255),
    map            TEXT,
    mapNoConverter TEXT,
    PRIMARY KEY (id)
);

