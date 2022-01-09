create or replace function update_iu_datetime() RETURNS TRIGGER AS $BODY$
    BEGIN
        NEW.iu_datetime := now();
        return NEW;
    END;
$BODY$ LANGUAGE plpgsql volatile;

CREATE trigger billing_remarks_iu_datetime_trigger
	before insert or update on billing_remarks
	for each row
	execute procedure update_iu_datetime();

CREATE trigger dispense_iu_datetime_trigger
	before insert or update on dispense
	for each row
	execute procedure update_iu_datetime();

CREATE trigger dispense_return_iu_datetime_trigger
	before insert or update on dispense_return
	for each row
	execute procedure update_iu_datetime();

CREATE trigger inventory_adjustment_iu_datetime_trigger
	before insert or update on inventory_adjustment
	for each row
	execute procedure update_iu_datetime();

CREATE trigger invoice_iu_datetime_trigger
	before insert or update on invoice
	for each row
	execute procedure update_iu_datetime();

CREATE trigger invoice_receipt_iu_datetime_trigger
	before insert or update on invoice_receipt
	for each row
	execute procedure update_iu_datetime();

CREATE trigger item_store_stock_view_iu_datetime_trigger
	before insert or update on item_store_stock_view
	for each row
	execute procedure update_iu_datetime();

CREATE trigger item_barcode_iu_datetime_trigger
	before insert or update on item_barcode
	for each row
	execute procedure update_iu_datetime();

CREATE trigger medication_order_iu_datetime_trigger
	before insert or update on medication_order
	for each row
	execute procedure update_iu_datetime();

CREATE trigger medication_request_iu_datetime_trigger
	before insert or update on medication_request
	for each row
	execute procedure update_iu_datetime();

CREATE trigger receipt_iu_datetime_trigger
	before insert or update on receipt
	for each row
	execute procedure update_iu_datetime();

CREATE trigger refund_iu_datetime_trigger
	before insert or update on refund
	for each row
	execute procedure update_iu_datetime();

CREATE trigger saved_audit_criterias_iu_datetime_trigger
	before insert or update on saved_audit_criterias
	for each row
	execute procedure update_iu_datetime();

CREATE trigger sponsor_invoice_iu_datetime_trigger
	before insert or update on sponsor_invoice
	for each row
	execute procedure update_iu_datetime();

CREATE trigger stock_audit_iu_datetime_trigger
	before insert or update on stock_audit
	for each row
	execute procedure update_iu_datetime();

CREATE trigger stock_consumption_iu_datetime_trigger
	before insert or update on stock_consumption
	for each row
	execute procedure update_iu_datetime();

CREATE trigger stock_correction_iu_datetime_trigger
	before insert or update on stock_correction
	for each row
	execute procedure update_iu_datetime();

CREATE trigger stock_issue_iu_datetime_trigger
	before insert or update on stock_issue
	for each row
	execute procedure update_iu_datetime();

CREATE trigger stock_indent_iu_datetime_trigger
	before insert or update on stock_indent
	for each row
	execute procedure update_iu_datetime();

CREATE trigger stock_reversal_iu_datetime_trigger
	before insert or update on stock_reversal
	for each row
	execute procedure update_iu_datetime();

CREATE trigger stock_audit_plan_iu_datetime_trigger
	before insert or update on stock_audit_plan
	for each row
	execute procedure update_iu_datetime();

CREATE trigger stock_receipt_iu_datetime_trigger
	before insert or update on stock_receipt
	for each row
	execute procedure update_iu_datetime();

CREATE trigger stock_source_header_iu_datetime_trigger
	before insert or update on stock_source_header
	for each row
	execute procedure update_iu_datetime();
