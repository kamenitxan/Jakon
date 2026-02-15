CREATE TABLE ShopProduct (
    id               ROWID,
    name             VARCHAR(255)   NOT NULL,
    description      TEXT,
    shortDescription VARCHAR(255),
    price            DECIMAL(19, 2) NOT NULL,
    discountPrice    DECIMAL(19, 2),
    stockQuantity    INT            NOT NULL DEFAULT 0,
    sku              VARCHAR(255)   NOT NULL,
    image            VARCHAR(255),
    images           VARCHAR(255),
    category_id      INT            NOT NULL,
    featured         BOOLEAN        NOT NULL DEFAULT FALSE,
    displayOrder     INT,
    url              VARCHAR(255),
    published        BOOLEAN        NOT NULL DEFAULT FALSE,
    FOREIGN KEY (category_id) REFERENCES ShopCategory (id)
);