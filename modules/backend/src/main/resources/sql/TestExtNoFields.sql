CREATE TABLE TestExtNoFields
(
    id           INTEGER      NOT NULL REFERENCES JakonObject (id) ON DELETE CASCADE,

    primary key (id)
);

