CREATE TYPE unit_type AS ENUM ('PCS', 'KG', 'L');

CREATE TABLE dish_ingredient
(
    id                 serial primary key,
    id_dish            int references dish (id),
    id_ingredient      int references ingredient (id),
    quantity_required  numeric(10, 3),
    unit               unit_type
);

ALTER TABLE ingredient
    DROP COLUMN IF EXISTS id_dish;

--insert data into the dish_ingredient table
INSERT INTO dish_ingredient (id, id_dish, id_ingredient, quantity_required, unit)
VALUES
    (1, 1, 1, 0.20, 'KG'),
    (2, 1, 2, 0.15, 'KG'),
    (3, 2, 3, 1.00, 'KG'),
    (4, 4, 4, 0.30, 'KG'),
    (5, 4, 5, 0.20, 'KG');

--update the data inside the dish (change the value of the new price)
UPDATE dish SET price = 3500.00 WHERE id = 1 AND name = 'Salade fraîche';
UPDATE dish SET price = 12000.00 WHERE id = 2 AND name = 'Poulet grillé';
UPDATE dish SET price = 8000.00 WHERE id = 4 AND name = 'Gâteau au chocolat';

--rename the column price into selling_price in the dish table
ALTER TABLE dish RENAME COLUMN price TO selling_price;

-- remove the constraint of the price column into dish to add a new constraint to make a default value NULL
ALTER TABLE dish
    DROP CONSTRAINT IF EXISTS dish_price_check;

-- add the new constraint to price column into dish
ALTER TABLE dish
    ADD CONSTRAINT dish_price_check CHECK (price IS NULL OR price >= 0);