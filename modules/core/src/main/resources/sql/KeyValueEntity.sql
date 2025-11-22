CREATE TABLE KeyValueEntity
(
    id    integer NOT NULL REFERENCES JakonObject (id) ON DELETE CASCADE,
    name  varchar(255),
    value varchar(255),

    PRIMARY KEY (id),
    CONSTRAINT uq_name UNIQUE (name)
);

