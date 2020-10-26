CREATE TABLE TestExtUser
(
    id           INTEGER      NOT NULL REFERENCES JakonObject (id) ON DELETE CASCADE,
    someStuff    VARCHAR(255) NULL DEFAULT NULL,

    primary key (id)
);

