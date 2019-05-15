CREATE TABLE EmailEntity
(
  emailType      varchar(255),
  params         varchar(255),
  sent           boolean default false,
  sentDate       datetime,
  subject        varchar(255),
  template       varchar(255),
  addressTo      varchar(255),
  lang           varchar(30),
  id             integer NOT NULL REFERENCES JakonObject (id) ON DELETE CASCADE,
  primary key (id)
)

