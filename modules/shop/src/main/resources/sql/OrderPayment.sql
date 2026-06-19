CREATE TABLE OrderPayment (
    id                INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
    order_id          INT         NOT NULL,
    provider          VARCHAR(50) NOT NULL,
    externalPaymentId VARCHAR(255) NOT NULL DEFAULT '',
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    amount            DECIMAL(19, 2) NOT NULL DEFAULT 0,
    currency          VARCHAR(10) NOT NULL DEFAULT '',
    createdAt         DATETIME    NOT NULL,
    updatedAt         DATETIME    NOT NULL,
    url               VARCHAR(255),
    published         BOOLEAN     NOT NULL DEFAULT TRUE,
    FOREIGN KEY (order_id) REFERENCES ShopOrder (id)
);
