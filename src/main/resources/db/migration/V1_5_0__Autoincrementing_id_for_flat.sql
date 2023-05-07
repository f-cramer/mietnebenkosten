ALTER TABLE "flats"
    ADD COLUMN "flat_id" BIGSERIAL NOT NULL;

ALTER TABLE "contracts"
    DROP CONSTRAINT "fk_contracts_flat_name";

ALTER TABLE "flats"
    DROP CONSTRAINT "pk_flats";

ALTER TABLE "flats"
    ADD CONSTRAINT "pk_flats" PRIMARY KEY ("flat_id");

ALTER TABLE "contracts"
    ADD COLUMN "flat_id" BIGINT NULL;

ALTER TABLE "contracts"
    ADD CONSTRAINT "fk_contracts_flat_id" FOREIGN KEY ("flat_id") REFERENCES "flats" ("flat_id")
        ON UPDATE CASCADE ON DELETE NO ACTION;

UPDATE "contracts"
SET "flat_id" = (SELECT "flat_id" FROM "flats" WHERE "contracts"."flat_name" = "flats"."name");

ALTER TABLE "contracts"
    ALTER COLUMN "flat_id" SET NOT NULL;

ALTER TABLE "contracts"
    DROP COLUMN "flat_name";
