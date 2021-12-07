CREATE TABLE ResetPasswordEmailEntity
(
  expirationDate datetime,
  secret         VARCHAR(255),
  token          VARCHAR(255),
  user           INTEGER NOT NULL REFERENCES JakonUser(id) ON DELETE CASCADE,
  id             INTEGER NOT NULL REFERENCES JakonObject(id) ON DELETE CASCADE,
  PRIMARY KEY (id)
)

