CREATE TABLE OrderReturn (
    id           INTEGER      NOT NULL PRIMARY KEY AUTO_INCREMENT,
    order_id     INT          NOT NULL,
    returnNumber VARCHAR(50)  NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    reason       VARCHAR(50)  NOT NULL DEFAULT '',
    bankAccount  VARCHAR(100) NOT NULL DEFAULT '',
    adminNote    TEXT,
    createdAt    DATETIME     NOT NULL,
    url          VARCHAR(255),
    published    BOOLEAN      NOT NULL DEFAULT TRUE,
    FOREIGN KEY (order_id) REFERENCES ShopOrder (id)
);
