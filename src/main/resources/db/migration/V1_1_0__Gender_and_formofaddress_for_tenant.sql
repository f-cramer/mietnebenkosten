ALTER TABLE "tenants"
    ADD "gender" VARCHAR(10) NULL;
ALTER TABLE "tenants"
    ADD "form_of_address" VARCHAR(20) NULL;

UPDATE "tenants"
    SET "gender" = 'FEMALE',
        "form_of_address" = 'FORMAL';

ALTER TABLE "tenants"
    ALTER "gender" SET NOT NULL;
ALTER TABLE "tenants"
    ALTER "form_of_address" SET NOT NULL;
