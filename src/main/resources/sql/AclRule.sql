create table AclRule (
  adminAllowed boolean,
  masterAdmin boolean,
  name varchar(255),
  id integer NOT NULL REFERENCES JakonObject(id)
);

