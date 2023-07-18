--DROP TABLE IF EXISTS STORAGES CASCADE;
--DROP SEQUENCE IF EXISTS STORAGES_SEQUENCE CASCADE;
CREATE SEQUENCE IF NOT EXISTS STORAGES_SEQUENCE as bigint;

CREATE TABLE IF NOT EXISTS STORAGES(
    "id" bigint PRIMARY KEY DEFAULT nextval('STORAGES_SEQUENCE'),
    "type" varchar(50) NOT NULL,
    "bucket" varchar(63) NOT NULL UNIQUE,
    "path" varchar(100) NOT NULL,
    "last_modified_date" TIMESTAMP,
    "created_date" TIMESTAMP NOT NULL
);

ALTER SEQUENCE STORAGES_SEQUENCE OWNED BY STORAGES."id";