CREATE TABLE Page
(
  content      varchar(255),
  objectOrder  double precision not null,
  showComments boolean,
  title        varchar(255),
  id           integer          NOT NULL REFERENCES JakonObject (id) ON DELETE CASCADE,
  parent_id    integer,
  primary key (id)
)