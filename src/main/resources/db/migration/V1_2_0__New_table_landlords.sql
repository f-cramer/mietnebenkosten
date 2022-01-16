CREATE TABLE "landlords" (
    "id" bigserial NOT NULL,
    "first_name" character varying(255) NOT NULL,
    "last_name" character varying(255) NOT NULL,
    "street" character varying(255) NOT NULL,
    "house_number" integer NOT NULL,
    "zip_code" character varying(255) NOT NULL,
    "city" character varying(255) NOT NULL,
    "country" character varying(255) NULL,
    "iban" character varying(255) NOT NULL,
    "start" smallint NOT NULL,
    "end" smallint NULL,
    CONSTRAINT "pk_landlords" PRIMARY KEY ("id")
);
