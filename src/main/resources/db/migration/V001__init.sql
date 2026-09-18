CREATE SCHEMA IF NOT EXISTS cache;

CREATE TABLE IF NOT EXISTS cache.openlr (
    id bigint NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    openlr_base64 character varying NOT NULL,
    real_length integer NOT NULL,
    last_decoded timestamp with time zone,
    json_result jsonb,
    CONSTRAINT openlr_cache_uniq_base64_real_length UNIQUE (openlr_base64, real_length)
);
