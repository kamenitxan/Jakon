CREATE TABLE ProductVariantValue (
    id          INTEGER      NOT NULL REFERENCES JakonObject (id) ON DELETE CASCADE,
    variant_id  INT          NOT NULL,
    value       VARCHAR(255) NOT NULL,
    objectOrder DOUBLE         NOT NULL,

    PRIMARY KEY (id),
    FOREIGN KEY (variant_id) REFERENCES ProductVariant (id) ON DELETE CASCADE
);
