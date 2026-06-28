CREATE TABLE Customer (
    id              INTEGER NOT NULL,
    phone           VARCHAR(50),
    company         VARCHAR(255),
    ico             VARCHAR(20),
    dic             VARCHAR(20),
    street          VARCHAR(255),
    city            VARCHAR(255),
    zip             VARCHAR(20),
    country         VARCHAR(100),
    deliveryStreet  VARCHAR(255),
    deliveryCity    VARCHAR(255),
    deliveryZip     VARCHAR(20),
    deliveryCountry VARCHAR(100),
    PRIMARY KEY (id),
    FOREIGN KEY (id) REFERENCES JakonUser (id) ON DELETE CASCADE
);
