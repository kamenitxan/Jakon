CREATE TABLE ShopCategory (
                              id INTEGER NOT NULL REFERENCES JakonObject (id) ON DELETE CASCADE,
                              name VARCHAR(255) NOT NULL,
                              description TEXT,
                              image VARCHAR(512),
                              displayOrder INT NOT NULL DEFAULT 0,
                              parentCategory_id INT NULL,

                              PRIMARY KEY (id),
                              CONSTRAINT fk_shopcategory_parent
                                  FOREIGN KEY (parentCategory_id) REFERENCES ShopCategory(id)

);