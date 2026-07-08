CREATE UNIQUE INDEX uq_markets_owner_id_name_active ON markets (owner_id, name) WHERE deleted_at IS NULL;
