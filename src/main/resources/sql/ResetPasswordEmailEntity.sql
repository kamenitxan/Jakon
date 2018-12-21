CREATE TABLE ResetPasswordEmailEntity
(
  expirationDate datetime,
  objectSettings blob,
  secret         varchar(255),
  token          varchar(255),
  user           blob,
  id             integer NOT NULL REFERENCES JakonObject(id) ON DELETE CASCADE,
  primary key (id)
)

