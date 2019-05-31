CREATE TABLE ConfirmEmailEntity
(
    id             INTEGER NOT NULL REFERENCES JakonObject (id) ON DELETE CASCADE,
    user_id        INTEGER NOT NULL REFERENCES JakonUser (id) ON DELETE CASCADE,
    token          VARCHAR(255),
    secret         VARCHAR(255),
    expirationDate DATETIME,

    PRIMARY KEY (id)
)

