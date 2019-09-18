CREATE TABLE JakonFile
(
    name      VARCHAR(255),
    path      VARCHAR(255),
    author_id INTEGER NULL REFERENCES JakonUser (id) ON DELETE SET NULL,
    created   DATETIME,
    fileType  VARCHAR(10),
    id        INTEGER NOT NULL REFERENCES JakonObject (id) ON DELETE CASCADE,
    PRIMARY KEY (id),
    CONSTRAINT UC_file UNIQUE (name,path)
);

