CREATE TABLE TestObject
(
    id             INTEGER NOT NULL REFERENCES JakonObject (id) ON DELETE CASCADE,
    string         VARCHAR(255),
    boolean        BOOLEAN,
    `double`       DOUBLE,
    `float`        FLOAT,
    `integer`      INTEGER,
    user_id        INTEGER REFERENCES JakonUser (id) ON DELETE CASCADE,
    date           DATETIME,
    localDate      DATETIME,
    localDateTime  DATETIME,
    self_id        INTEGER REFERENCES TestObject (id) ON DELETE CASCADE,
    enum           VARCHAR(255),
    map            TEXT,
    mapNoConverter TEXT,
    embedded_string VARCHAR(255),
    embedded_int    INTEGER,

    PRIMARY KEY (id)
);

