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


INSERT INTO flats ("name", "area", "order") VALUES
	('Erdgeschoss', 110, 0),
	('1. Obergeschoss', 110, 1),
	('Dachgeschoss', 80, 2);

INSERT INTO "tenants" ("first_name", "last_name", "street", "house_number", "zip_code", "city", "country", "hidden") VALUES
	('Uta', 'Krüger', 'Bayreuther Straße', 76, '67659', 'Kaiserslautern', null, false),
	('Max', 'Propst', 'Neuer Jungfernstieg', 96, '84126', 'Dingolfing', null, false),
	('Sara', 'Weiss', 'Fugger Straße', 22, '14403', 'Potsdam', null, false);

INSERT INTO "rentals" ("start", "end", "persons", "flat_name", "tenant_id") VALUES
	('2017-01-01', null, 2, 'Erdgeschoss', 1),
	('2019-12-01', null, 1, '1. Obergeschoss', 2),
	('2018-11-01', null, 1, 'Dachgeschoss', 3);

INSERT INTO "invoices" ("type", "description", "order", "start", "end", "monetary_amount", "split_algorithm", "rental_id") VALUES
	('general', 'Wasser', 0, '2020-01-01', '2020-12-31', 0.00, '{"@c":".ByAreaSplitAlgorithm"}', null),
	('general', 'Abwasser', 0, '2020-01-01', '2020-12-31', 0.00, '{"@c":".ByAreaSplitAlgorithm"}', null);
