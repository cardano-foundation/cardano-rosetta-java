SET SCHEMA 'testnet';
ALTER TABLE failed_tx_out ALTER COLUMN multi_assets_descr TYPE text;
