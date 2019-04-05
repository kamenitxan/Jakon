CREATE TABLE EmailTemplateEntity
(
  addressFrom    varchar(255),
  name           varchar(255),
  objectSettings blob,
  subject        varchar(255),
  template       varchar(255),
  id             integer NOT NULL REFERENCES JakonObject (id) ON DELETE CASCADE,
  primary key (id)
)

