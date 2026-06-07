CREATE TABLE CartItem (
    id        INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
    cart_id    INT     NOT NULL,
    product_id INT     NOT NULL,
    quantity  INT     NOT NULL DEFAULT 1,

    FOREIGN KEY (cart_id)    REFERENCES Cart (id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES ShopProduct (id) ON DELETE RESTRICT
);
