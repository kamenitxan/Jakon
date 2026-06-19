CREATE TABLE OrderReturnItem (
    id              INTEGER      NOT NULL PRIMARY KEY AUTO_INCREMENT,
    orderReturn_id  INT          NOT NULL,
    orderItem_id    INT          NOT NULL,
    productName     VARCHAR(255) NOT NULL DEFAULT '',
    quantity        INT          NOT NULL DEFAULT 1,
    url             VARCHAR(255),
    published       BOOLEAN      NOT NULL DEFAULT TRUE,
    FOREIGN KEY (orderReturn_id) REFERENCES OrderReturn (id),
    FOREIGN KEY (orderItem_id)   REFERENCES ShopOrderItem (id)
);
