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