CREATE TABLE ProductVariant (
    id           INTEGER      NOT NULL REFERENCES JakonObject (id) ON DELETE CASCADE,
    name         VARCHAR(255) NOT NULL,
    objectOrder  DOUBLE          NOT NULL,

    PRIMARY KEY (id)
);
