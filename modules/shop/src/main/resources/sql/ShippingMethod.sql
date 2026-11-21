CREATE TABLE ShippingMethod (
    id                    INTEGER NOT NULL,
    name                  VARCHAR(255) NOT NULL,
    description           TEXT,
    price                 DECIMAL(19, 2) NOT NULL DEFAULT 0,
    enabled               BOOLEAN NOT NULL DEFAULT TRUE,
    displayOrder          INT     NOT NULL DEFAULT 0,
    icon                  VARCHAR(255),
    estimatedDeliveryDays INT     NOT NULL DEFAULT 0,
    url                   VARCHAR(255),
    published             BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    FOREIGN KEY (id) REFERENCES JakonObject (id) ON DELETE CASCADE
);
