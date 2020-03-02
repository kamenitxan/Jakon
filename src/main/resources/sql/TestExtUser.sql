CREATE TABLE TestExtUser
(
    id           INTEGER      NOT NULL REFERENCES JakonObject (id) ON DELETE CASCADE,
    jakonUser_id INTEGER      NOT NULL REFERENCES JakonUser (id) ON DELETE CASCADE,
    someStuff    VARCHAR(255) NULL DEFAULT NULL,

    primary key (id)
);

