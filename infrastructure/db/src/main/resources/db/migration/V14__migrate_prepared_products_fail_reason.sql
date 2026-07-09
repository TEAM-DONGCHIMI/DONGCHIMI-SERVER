UPDATE prepared_products
SET fail_reason = CASE
        WHEN thumbnail_url IS NULL THEN 'THUMBNAIL_MISSING'
        WHEN category IS NULL THEN 'CATEGORY_MISSING'
        WHEN name IS NULL THEN 'NAME_MISSING'
        WHEN original_price IS NULL OR discounted_price IS NULL THEN 'PRICE_MISSING'
        WHEN discount_start_date IS NULL OR discount_end_date IS NULL THEN 'DISCOUNT_PERIOD_MISSING'
        ELSE NULL
    END,
    draft_status = CASE
        WHEN thumbnail_url IS NULL OR category IS NULL OR name IS NULL
            OR original_price IS NULL OR discounted_price IS NULL
            OR discount_start_date IS NULL OR discount_end_date IS NULL THEN 'FAIL'
        ELSE 'SUCCESS'
    END
WHERE deleted_at IS NULL;
