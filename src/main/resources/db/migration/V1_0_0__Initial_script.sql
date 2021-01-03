CREATE TABLE "flats" (
    "name" character varying(255) NOT NULL,
    "area" bigint NOT NULL,
    "order" integer NOT NULL,
	CONSTRAINT "pk_flats" PRIMARY KEY ("name")
);


CREATE TABLE "tenants" (
    "id" bigserial NOT NULL,
    "first_name" character varying(255) NOT NULL,
    "last_name" character varying(255) NOT NULL,
    "street" character varying(255) NOT NULL,
    "house_number" integer NOT NULL,
    "zip_code" character varying(255) NOT NULL,
    "city" character varying(255) NOT NULL,
    "country" character varying(255) NULL,
    "hidden" boolean NOT NULL,
	CONSTRAINT "pk_tenants" PRIMARY KEY ("id")
);


CREATE TABLE "rentals" (
    "id" bigserial NOT NULL,
    "start" date NOT NULL,
    "end" date NULL,
    "persons" integer NOT NULL,
    "flat_name" character varying(255) NOT NULL,
    "tenant_id" bigint NOT NULL,
	CONSTRAINT "pk_rentals" PRIMARY KEY ("id"),
	CONSTRAINT "fk_rentals_flat_name" FOREIGN KEY ("flat_name") REFERENCES "flats"("name")
		ON UPDATE CASCADE ON DELETE NO ACTION,
	CONSTRAINT "fk_rentals_tenant_id" FOREIGN KEY ("tenant_id") REFERENCES "tenants"("id")
		ON UPDATE CASCADE ON DELETE NO ACTION
);


CREATE TABLE "invoices" (
    "id" bigserial NOT NULL,
    "type" character varying(20) NOT NULL,
    "description" character varying(255) NOT NULL,
    "order" integer NOT NULL,
    "start" date NOT NULL,
    "end" date NULL,
    "monetary_amount" numeric(20,2) NOT NULL,
    "split_algorithm" character varying(255) NULL,
    "rental_id" bigint NULL,
	CONSTRAINT "pk_invoices" PRIMARY KEY ("id"),
	CONSTRAINT "fk_invoices_rental_id" FOREIGN KEY ("rental_id") REFERENCES "rentals"("id")
		ON UPDATE CASCADE ON DELETE NO ACTION
);

