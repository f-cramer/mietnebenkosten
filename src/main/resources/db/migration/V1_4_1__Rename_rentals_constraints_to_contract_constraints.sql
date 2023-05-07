ALTER TABLE "contracts"
    RENAME CONSTRAINT "pk_rentals" TO "pk_contracts";

ALTER TABLE "contracts"
    RENAME CONSTRAINT "fk_rentals_flat_name" TO "fk_contracts_flat_name";

ALTER TABLE "contracts"
    RENAME CONSTRAINT "fk_rentals_tenant_id" TO "fk_contracts_tenant_id"
