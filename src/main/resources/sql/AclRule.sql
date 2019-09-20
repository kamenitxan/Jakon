CREATE TABLE AclRule
(
    adminAllowed            boolean,
    masterAdmin             boolean,
    name                    varchar(255),
    allowedControllers      text,
    allowedFrontendPrefixes text,
    id                      integer NOT NULL REFERENCES JakonObject (id) ON DELETE CASCADE,
    primary key (id)
);

