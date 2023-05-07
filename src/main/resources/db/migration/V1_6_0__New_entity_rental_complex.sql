CREATE TABLE "rental_complexes"
(
    "rental_complex_id" BIGSERIAL NOT NULL,
    "name"              VARCHAR   NOT NULL,
    CONSTRAINT "pk_rental_complexes" PRIMARY KEY ("rental_complex_id")
);

CREATE TABLE "users_rental_complexes"
(
    "username"          VARCHAR(255) NOT NULL,
    "rental_complex_id" BIGINT       NOT NULL,
    CONSTRAINT "pk_users_rental_complexes" PRIMARY KEY ("username", "rental_complex_id"),
    CONSTRAINT "fk_users_rental_complexes_username" FOREIGN KEY ("username") REFERENCES "users" ("username")
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT "fk_users_rental_complexes_rental_complex_id" FOREIGN KEY ("rental_complex_id") REFERENCES "rental_complexes" ("rental_complex_id")
        ON UPDATE CASCADE ON DELETE CASCADE
);

INSERT INTO "rental_complexes" ("name")
SELECT DISTINCT 'Standard'
FROM "flats",
     "tenants",
     "users";

INSERT INTO "users_rental_complexes" ("username", "rental_complex_id")
SELECT "username", "rental_complex_id"
FROM "users",
     "rental_complexes";


ALTER TABLE "flats"
    ADD COLUMN "rental_complex_id" BIGINT NULL;
ALTER TABLE "flats"
    ADD CONSTRAINT "fk_flats_rental_complex_id" FOREIGN KEY ("rental_complex_id") REFERENCES "rental_complexes" ("rental_complex_id")
        ON UPDATE CASCADE ON DELETE CASCADE;

UPDATE "flats"
SET "rental_complex_id" = r."rental_complex_id"
FROM "rental_complexes" r;

ALTER TABLE "flats"
    ALTER COLUMN "rental_complex_id" SET NOT NULL;


ALTER TABLE "tenants"
    ADD COLUMN "rental_complex_id" BIGINT NULL;
ALTER TABLE "tenants"
    ADD CONSTRAINT "fk_tenants_rental_complex_id" FOREIGN KEY ("rental_complex_id") REFERENCES "rental_complexes" ("rental_complex_id")
        ON UPDATE CASCADE ON DELETE CASCADE;

UPDATE "tenants"
SET "rental_complex_id" = r."rental_complex_id"
FROM "rental_complexes" r;

ALTER TABLE "tenants"
    ALTER COLUMN "rental_complex_id" SET NOT NULL;


ALTER TABLE "landlords"
    ADD COLUMN "rental_complex_id" BIGINT NULL;
ALTER TABLE "landlords"
    ADD CONSTRAINT "fk_landlords_rental_complex_id" FOREIGN KEY ("rental_complex_id") REFERENCES "rental_complexes" ("rental_complex_id")
        ON UPDATE CASCADE ON DELETE CASCADE;

UPDATE "landlords"
SET "rental_complex_id" = r."rental_complex_id"
FROM "rental_complexes" r;

ALTER TABLE "landlords"
    ALTER COLUMN "rental_complex_id" SET NOT NULL;


ALTER TABLE "invoices"
    ADD COLUMN "rental_complex_id" BIGINT NULL;
ALTER TABLE "invoices"
    ADD CONSTRAINT "fk_invoices_rental_complex_id" FOREIGN KEY ("rental_complex_id") REFERENCES "rental_complexes" ("rental_complex_id")
        ON UPDATE CASCADE ON DELETE CASCADE;

UPDATE "invoices"
SET "rental_complex_id" = r."rental_complex_id"
FROM "rental_complexes" r;

ALTER TABLE "invoices"
    ALTER COLUMN "rental_complex_id" SET NOT NULL;
