CREATE TABLE JakonUser
(
  email          varchar(255),
  emailConfirmed boolean,
  enabled        boolean,
  firstName      varchar(255),
  lastName       varchar(255),
  password       varchar(255),
  username       varchar(255),
  id             integer NOT NULL REFERENCES JakonObject (id) ON DELETE CASCADE,
  acl_id         integer,
  primary key (id)
)

