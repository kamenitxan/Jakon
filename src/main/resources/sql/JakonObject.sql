create table JakonObject
(
  id          integer not null primary key AUTO_INCREMENT,
  childClass  varchar(255),
  published   boolean,
  sectionName varchar(255),
  url         varchar(255)
);

