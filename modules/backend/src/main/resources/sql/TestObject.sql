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
    time           VARCHAR(8),
    localDate      DATETIME,
    localDateTime  DATETIME,
    self_id        INTEGER REFERENCES TestObject (id) ON DELETE CASCADE,
    enum           VARCHAR(255),
    map            TEXT,
    mapNoConverter TEXT,
    embedded_string VARCHAR(255),
    embedded_int    INTEGER,
    oneToMany       TEXT,
    jakonFile_id    INTEGER REFERENCES JakonFile(id) ON DELETE SET NULL,

    PRIMARY KEY (id)
);
CREATE TABLE TestObjectI18n (
    id INTEGER NOT NULL REFERENCES TestObject (id) ON DELETE CASCADE,
    locale VARCHAR(10) NOT NULL,
    name VARCHAR(50),
    description VARCHAR(100),

    PRIMARY KEY (id, locale)
);
