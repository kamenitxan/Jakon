CREATE TABLE OrderItem (
    id          INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
    order_id    INT     NOT NULL,
    product_id  INT     NULL,
    productName VARCHAR(255),
    quantity    INT     NOT NULL DEFAULT 1,
    unitPrice   DECIMAL(19, 2),
    totalPrice  DECIMAL(19, 2),
    note        TEXT,
    url         VARCHAR(255),
    published   BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (order_id) REFERENCES `Order` (id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES ShopProduct (id) ON DELETE RESTRICT 
);
