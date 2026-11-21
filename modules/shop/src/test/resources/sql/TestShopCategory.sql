CREATE TABLE TestShopCategory (
    id INTEGER NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (id) REFERENCES ShopCategory (id) ON DELETE CASCADE
);

CREATE TABLE TestShopCategoryI18n (
    id        INTEGER     NOT NULL REFERENCES TestShopCategory (id) ON DELETE CASCADE,
    locale    VARCHAR(10) NOT NULL,
    printTime VARCHAR(100),
    PRIMARY KEY (id, locale)
);
