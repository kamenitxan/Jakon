CREATE TABLE PaymentTransaction
(
    status  VARCHAR(50) NOT NULL,
    user_id INTEGER     NOT NULL REFERENCES JakonUser (id) ON DELETE RESTRICT,
    amount  LONG        NOT NULL,

    id      INTEGER     NOT NULL REFERENCES JakonObject (id) ON DELETE CASCADE,
    PRIMARY KEY (id)
);

