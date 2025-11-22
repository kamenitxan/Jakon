CREATE TABLE Category
(
  name         varchar(255),
  objectOrder  double precision not null,
  showComments boolean,
  id           integer          NOT NULL REFERENCES JakonObject (id) ON DELETE CASCADE,
  primary key (id)
)

