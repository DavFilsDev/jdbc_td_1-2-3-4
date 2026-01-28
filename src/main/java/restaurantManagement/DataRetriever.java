package restaurantManagement;

import db.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    public Dish findDishById(Integer id) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getDBConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                    select id, name, dish_type, price
                    from dish
                    where id = ?""");
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                Double price = resultSet.getDouble("price");
                if (resultSet.wasNull()) {
                    price = null;
                }

                Dish dish = new Dish(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        DishTypeEnum.valueOf(resultSet.getString("dish_type")),
                        price
                );

                List<Ingredient> ingredients = findIngredientsByDishId(dish.getId());
                dish.setIngredients(ingredients);

                dbConnection.close(connection);
                return dish;
            }
            dbConnection.close(connection);
            throw new RuntimeException("Not found dish(id=" + id + ")");
        } catch (SQLException e) {
            dbConnection.close(connection);
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> findIngredients(int page, int size) {
        List<Ingredient> ingredients = new ArrayList<>();
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getDBConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                    select i.id, i.name, i.price, i.category, i.id_dish, d.name as dish_name, d.price as dish_price
                    from ingredient i
                    left join dish d on i.id_dish = d.id
                    order by i.id
                    limit ? offset ?""");
            preparedStatement.setInt(1, size);
            preparedStatement.setInt(2, (page - 1) * size);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Dish dish = null;
                if (resultSet.getInt("id_dish") > 0) {
                    Double dishPrice = resultSet.getDouble("dish_price");
                    if (resultSet.wasNull()) {
                        dishPrice = null;
                    }
                    dish = new Dish(
                            resultSet.getInt("id_dish"),
                            resultSet.getString("dish_name"),
                            null,
                            dishPrice
                    );
                }

                Ingredient ingredient = new Ingredient(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getDouble("price"),
                        CategoryEnum.valueOf(resultSet.getString("category")),
                        dish
                );
                ingredients.add(ingredient);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        dbConnection.close(connection);
        return ingredients;
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        if (newIngredients == null || newIngredients.isEmpty()) {
            return new ArrayList<>();
        }

        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getDBConnection();

        try {
            connection.setAutoCommit(false);

            for (Ingredient ingredient : newIngredients) {
                PreparedStatement checkStmt = connection.prepareStatement(
                        "select count(*) from ingredient where name = ?");
                checkStmt.setString(1, ingredient.getName());
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    connection.rollback();
                    connection.setAutoCommit(true);
                    dbConnection.close(connection);
                    throw new RuntimeException("Ingredient already exists: " + ingredient.getName());
                }
            }

            PreparedStatement insertStmt = connection.prepareStatement(
                    "insert into ingredient (name, price, category, id_dish) values (?, ?, ?::ingredient_category, ?)");

            for (Ingredient ingredient : newIngredients) {
                insertStmt.setString(1, ingredient.getName());
                insertStmt.setDouble(2, ingredient.getPrice());
                insertStmt.setString(3, ingredient.getCategory().name());
                if (ingredient.getDish() != null) {
                    insertStmt.setInt(4, ingredient.getDish().getId());
                } else {
                    insertStmt.setNull(4, java.sql.Types.INTEGER);
                }
                insertStmt.addBatch();
            }

            insertStmt.executeBatch();
            connection.commit();

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                throw new RuntimeException(rollbackEx);
            }
            throw new RuntimeException(e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            dbConnection.close(connection);
        }

        return newIngredients;
    }

    public Dish saveDish(Dish dishToSave) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getDBConnection();

        try {
            connection.setAutoCommit(false);

            PreparedStatement checkStmt = connection.prepareStatement(
                    "select id from dish where id = ?");
            checkStmt.setInt(1, dishToSave.getId());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                PreparedStatement updateStmt = connection.prepareStatement(
                        "update dish set name = ?, dish_type = ?, price = ? where id = ?");
                updateStmt.setString(1, dishToSave.getName());
                updateStmt.setString(2, dishToSave.getDishType().name());
                if (dishToSave.getPrice() != null) {
                    updateStmt.setDouble(3, dishToSave.getPrice());
                } else {
                    updateStmt.setNull(3, java.sql.Types.NUMERIC);
                }
                updateStmt.setInt(4, dishToSave.getId());
                updateStmt.executeUpdate();
            } else {
                PreparedStatement insertStmt = connection.prepareStatement(
                        "insert into dish (id, name, dish_type, price) values (?, ?, ?::dish_type_enum, ?)");
                insertStmt.setInt(1, dishToSave.getId());
                insertStmt.setString(2, dishToSave.getName());
                insertStmt.setString(3, dishToSave.getDishType().name());
                if (dishToSave.getPrice() != null) {
                    insertStmt.setDouble(4, dishToSave.getPrice());
                } else {
                    insertStmt.setNull(4, java.sql.Types.NUMERIC);
                }
                insertStmt.executeUpdate();
            }

            PreparedStatement dissociateStmt = connection.prepareStatement(
                    "update ingredient set id_dish = null where id_dish = ?");
            dissociateStmt.setInt(1, dishToSave.getId());
            dissociateStmt.executeUpdate();

            if (dishToSave.getIngredients() != null && !dishToSave.getIngredients().isEmpty()) {
                for (Ingredient ingredient : dishToSave.getIngredients()) {
                    PreparedStatement associateStmt = connection.prepareStatement(
                            "update ingredient set id_dish = ? where id = ?");
                    associateStmt.setInt(1, dishToSave.getId());
                    associateStmt.setInt(2, ingredient.getId());
                    associateStmt.executeUpdate();
                }
            }

            connection.commit();

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                throw new RuntimeException(rollbackEx);
            }
            throw new RuntimeException(e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            dbConnection.close(connection);
        }

        return dishToSave;
    }

    public List<Dish> findDishesByIngredientName(String ingredientName) {
        List<Dish> dishes = new ArrayList<>();
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getDBConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                    select distinct d.id, d.name, d.dish_type, d.price
                    from dish d
                    inner join ingredient i on d.id = i.id_dish
                    where lower(i.name) like lower(?)""");
            preparedStatement.setString(1, "%" + ingredientName + "%");

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Double price = resultSet.getDouble("price");
                if (resultSet.wasNull()) {
                    price = null;
                }

                Dish dish = new Dish(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        DishTypeEnum.valueOf(resultSet.getString("dish_type")),
                        price
                );

                List<Ingredient> ingredients = findIngredientsByDishId(dish.getId());
                dish.setIngredients(ingredients);
                dishes.add(dish);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        dbConnection.close(connection);
        return dishes;
    }

    public List<Ingredient> findIngredientsByCriteria(String ingredientName,
                                                      CategoryEnum category,
                                                      String dishName,
                                                      int page,
                                                      int size) {
        List<Ingredient> ingredients = new ArrayList<>();
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getDBConnection();

        try {
            StringBuilder sql = new StringBuilder(
                    """
                    select i.id, i.name, i.price, i.category, i.id_dish, 
                           d.name as dish_name, d.price as dish_price
                    from ingredient i
                    left join dish d on i.id_dish = d.id
                    where 1=1""");

            List<Object> params = new ArrayList<>();

            if (ingredientName != null && !ingredientName.trim().isEmpty()) {
                sql.append(" and lower(i.name) like lower(?)");
                params.add("%" + ingredientName + "%");
            }

            if (category != null) {
                sql.append(" and i.category = ?::ingredient_category");
                params.add(category.name());
            }

            if (dishName != null && !dishName.trim().isEmpty()) {
                sql.append(" and lower(d.name) like lower(?)");
                params.add("%" + dishName + "%");
            }

            sql.append(" order by i.id");
            sql.append(" limit ? offset ?");

            PreparedStatement preparedStatement = connection.prepareStatement(sql.toString());

            for (int i = 0; i < params.size(); i++) {
                preparedStatement.setObject(i + 1, params.get(i));
            }

            preparedStatement.setInt(params.size() + 1, size);
            preparedStatement.setInt(params.size() + 2, (page - 1) * size);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Dish dish = null;
                if (resultSet.getInt("id_dish") > 0) {
                    Double dishPrice = resultSet.getDouble("dish_price");
                    if (resultSet.wasNull()) {
                        dishPrice = null;
                    }
                    dish = new Dish(
                            resultSet.getInt("id_dish"),
                            resultSet.getString("dish_name"),
                            null,
                            dishPrice
                    );
                }

                Ingredient ingredient = new Ingredient(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getDouble("price"),
                        CategoryEnum.valueOf(resultSet.getString("category")),
                        dish
                );
                ingredients.add(ingredient);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        dbConnection.close(connection);
        return ingredients;
    }

    private List<Ingredient> findIngredientsByDishId(Integer dishId) {
        List<Ingredient> ingredients = new ArrayList<>();
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getDBConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    """
                    select id, name, price, category
                    from ingredient
                    where id_dish = ?""");
            preparedStatement.setInt(1, dishId);
            ResultSet resultSet = preparedStatement.executeQuery();

            Dish dish = new Dish(dishId, "", null);

            while (resultSet.next()) {
                Ingredient ingredient = new Ingredient(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        resultSet.getDouble("price"),
                        CategoryEnum.valueOf(resultSet.getString("category")),
                        dish
                );
                ingredients.add(ingredient);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        dbConnection.close(connection);
        return ingredients;
    }
}