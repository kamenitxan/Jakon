CREATE TABLE Post
(
  content      varchar(255),
  date         datetime,
  perex        varchar(255),
  showComments boolean,
  title        varchar(255),
  id           integer NOT NULL REFERENCES JakonObject (id) ON DELETE CASCADE,
  category_id  integer,
  primary key (id)
)

