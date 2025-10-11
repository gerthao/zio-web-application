create database reviewboard;
\c reviewboard;

create table if not exists companies (
    id bigserial primary key ,
    slug text unique not null,
    name text unique not null,
    url text unique not null,
    location text,
    country text,
    industry text,
    image text,
    tags text[]
);

CREATE TABLE IF NOT EXISTS reviews (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    user_id BIGINT NOT NULl,
    management INT NOT NULL,
    culture INT NOT NULL,
    salary INT NOT NULL,
    benefits INT NOT NULL,
    would_recommend INT NOT NULL,
    review TEXT NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT now(),
    updated TIMESTAMP NOT NULL DEFAULT now()
);