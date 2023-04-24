ALTER TABLE "rentals"
    RENAME TO "contracts";

UPDATE "invoices"
    SET "type" = 'contract'
WHERE "type" = 'rental';

ALTER TABLE "invoices"
    DROP CONSTRAINT "fk_invoices_rental_id";
ALTER TABLE "invoices"
    RENAME COLUMN "rental_id" TO "contract_id";
ALTER TABLE "invoices"
    ADD CONSTRAINT "fk_invoices_contract_id" FOREIGN KEY ("contract_id") REFERENCES "contracts"("id")
        ON UPDATE CASCADE ON DELETE NO ACTION;
