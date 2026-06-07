CREATE TABLE Cart (
    id                       INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
    token                    VARCHAR(36) NOT NULL UNIQUE,
    selectedShippingMethod_id INT NULL,
    selectedPaymentMethod_id  INT NULL,
    createdAt                DATETIME   NOT NULL,
    updatedAt                DATETIME   NOT NULL,
    url                      VARCHAR(255),
    published                BOOLEAN     NOT NULL DEFAULT TRUE,
    FOREIGN KEY (selectedShippingMethod_id) REFERENCES ShippingMethod (id) ON DELETE SET NULL,
    FOREIGN KEY (selectedPaymentMethod_id) REFERENCES PaymentMethod (id) ON DELETE SET NULL
);
