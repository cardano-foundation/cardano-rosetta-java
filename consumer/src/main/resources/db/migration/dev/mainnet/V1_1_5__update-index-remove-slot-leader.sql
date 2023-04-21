CREATE UNIQUE INDEX IF NOT EXISTS multi_asset_fingerprint_uindex ON multi_asset (fingerprint);
CREATE UNIQUE INDEX IF NOT EXISTS datum_hash_uindex ON datum (hash);
CREATE INDEX IF NOT EXISTS ma_tx_mint_ident_index ON ma_tx_mint(ident);
CREATE INDEX IF NOT EXISTS extra_key_witness_hash_index ON extra_key_witness(hash);
CREATE INDEX IF NOT EXISTS ma_tx_out_ident_index ON ma_tx_out(ident);
CREATE UNIQUE INDEX IF NOT EXISTS pool_hash_hash_raw_index ON pool_hash(hash_raw);
CREATE INDEX IF NOT EXISTS pool_offline_data_pool_id_index ON pool_offline_data(pool_id);
CREATE INDEX IF NOT EXISTS redeemer_data_hash_index ON redeemer_data(hash);
CREATE UNIQUE INDEX IF NOT EXISTS script_hash_uindex ON script(hash);
CREATE INDEX IF NOT EXISTS slot_leader_hash_index ON slot_leader(hash);

DELETE FROM slot_leader WHERE id=2;
