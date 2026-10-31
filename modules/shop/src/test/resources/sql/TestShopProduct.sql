CREATE TABLE TestShopProduct (
    id       INTEGER NOT NULL,
    subtitle VARCHAR(255),
    PRIMARY KEY (id),
    FOREIGN KEY (id) REFERENCES ShopProduct (id) ON DELETE CASCADE
);

CREATE TABLE TestShopProductI18n (
    id          INTEGER     NOT NULL REFERENCES TestShopProduct (id) ON DELETE CASCADE,
    locale      VARCHAR(10) NOT NULL,
    description TEXT,
    PRIMARY KEY (id, locale)
);
