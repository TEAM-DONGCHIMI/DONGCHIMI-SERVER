ALTER TABLE default_product_thumbnails
    ADD COLUMN category VARCHAR(50) NOT NULL DEFAULT 'ETC';

CREATE INDEX idx_default_product_thumbnails_category ON default_product_thumbnails (category);
