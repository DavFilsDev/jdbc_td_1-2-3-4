import db.DBConnection;
import models.*;
import services.DataRetriever;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== TEST DE GESTION DES TABLES ===");

        DBConnection dbConnection = new DBConnection();
        DataRetriever dataRetriever = new DataRetriever(dbConnection);

        try {

            System.out.println("\n--- Test 1: Création de commande valide ---");
            testValidOrderCreation(dataRetriever);

            System.out.println("\n--- Test 2: Création avec table indisponible ---");
            testUnavailableTable(dataRetriever);

            System.out.println("\n--- Test 3: Création avec table 2 ---");
            testSecondTableOrder(dataRetriever);

            System.out.println("\n--- Test 4: Recherche de commande ---");
            testFindOrderByReference(dataRetriever);

            System.out.println("\n--- Test 5: Toutes les tables occupées ---");
            testAllTablesOccupied(dataRetriever);

            System.out.println("\n--- Test 6: Méthodes de base ---");
            testBasicMethods(dataRetriever);

        } catch (Exception e) {
            System.err.println("Erreur pendant les tests: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testValidOrderCreation(DataRetriever dataRetriever) {
        try {
            Dish dish = dataRetriever.findDishById(1);
            System.out.println("Plat trouvé: " + dish.getName() + " - Prix: " + dish.getPrice());

            List<DishOrder> dishOrders = new ArrayList<>();
            dishOrders.add(new DishOrder(0, dish, 2)); // 2 salades fraiches

            Table table = dataRetriever.findTableById(1);
            System.out.println("Table trouvée: numéro " + table.getNumber());

            Instant arrival = Instant.now().plus(2, ChronoUnit.HOURS);
            Instant departure = arrival.plus(3, ChronoUnit.HOURS);
            TableOrder tableOrder = new TableOrder(table, arrival, departure);

            Order order = new Order(0, null, Instant.now(), dishOrders, tableOrder);

            Order savedOrder = dataRetriever.saveOrder(order);
            System.out.println("Commande créée avec succès!");
            System.out.println("Référence: " + savedOrder.getReference());
            System.out.println("Table: " + savedOrder.getTable().getNumber());
            System.out.println("Arrivée: " + savedOrder.getArrivalDateTime());
            System.out.println("Départ: " + savedOrder.getDepartureDateTime());
            System.out.println("Montant HT: " + savedOrder.getTotalAmountWithoutVAT());
            System.out.println("Montant TTC: " + savedOrder.getTotalAmountWithVAT());

        } catch (Exception e) {
            System.err.println("Échec du test 1: " + e.getMessage());
        }
    }

    private static void testUnavailableTable(DataRetriever dataRetriever) {
        try {
            Dish dish = dataRetriever.findDishById(2);

            List<DishOrder> dishOrders = new ArrayList<>();
            dishOrders.add(new DishOrder(0, dish, 1));

            Table table = dataRetriever.findTableById(1);

            Instant arrival = Instant.now().plus(2, ChronoUnit.HOURS);
            Instant departure = arrival.plus(3, ChronoUnit.HOURS);
            TableOrder tableOrder = new TableOrder(table, arrival, departure);

            Order order = new Order(0, null, Instant.now(), dishOrders, tableOrder);

            dataRetriever.saveOrder(order);
            System.err.println("ERREUR: L'exception n'a pas été lancée!");

        } catch (RuntimeException e) {
            System.out.println("Exception attendue capturée: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur inattendue: " + e.getMessage());
        }
    }

    private static void testSecondTableOrder(DataRetriever dataRetriever) {
        try {
            Dish dish = dataRetriever.findDishById(4);
            System.out.println("Plat: " + dish.getName());

            List<DishOrder> dishOrders = new ArrayList<>();
            dishOrders.add(new DishOrder(0, dish, 3)); // 3 gateaux

            Table table = dataRetriever.findTableById(2);
            System.out.println("Table: numéro " + table.getNumber());

            Instant arrival = Instant.now().plus(4, ChronoUnit.HOURS);
            Instant departure = arrival.plus(2, ChronoUnit.HOURS);
            TableOrder tableOrder = new TableOrder(table, arrival, departure);

            Order order = new Order(0, "TEST123", Instant.now(), dishOrders, tableOrder);

            Order savedOrder = dataRetriever.saveOrder(order);
            System.out.println("Commande table 2 créée avec succès!");
            System.out.println("Référence: " + savedOrder.getReference());

        } catch (Exception e) {
            System.err.println("Échec du test 3: " + e.getMessage());
        }
    }

    private static void testFindOrderByReference(DataRetriever dataRetriever) {
        try {
            System.out.println("Recherche de la commande ORD00001...");
            Order order = dataRetriever.findOrderByReference("ORD00001");

            if (order != null) {
                System.out.println("Commande trouvée:");
                System.out.println("- Référence: " + order.getReference());
                System.out.println("- Table: " + order.getTable().getNumber());
                System.out.println("- Nombre de plats: " + order.getDishOrders().size());
                System.out.println("- Montant TTC: " + order.getTotalAmountWithVAT());
            } else {
                System.out.println("Commande non trouvée");
            }

        } catch (Exception e) {
            System.out.println("Commande non trouvée (normal si pas encore créée): " + e.getMessage());
        }
    }

    private static void testAllTablesOccupied(DataRetriever dataRetriever) {
        try {
            Dish dish = dataRetriever.findDishById(3);
            List<DishOrder> dishOrders = new ArrayList<>();
            dishOrders.add(new DishOrder(0, dish, 2));

            Table table = dataRetriever.findTableById(3);

            Instant arrival = Instant.now();
            Instant departure = arrival.plus(10, ChronoUnit.HOURS);
            TableOrder tableOrder = new TableOrder(table, arrival, departure);

            Order order = new Order(0, null, Instant.now(), dishOrders, tableOrder);

            dataRetriever.saveOrder(order);
            System.out.println("Commande créée avec table 3");

            System.out.println("\nEssai de double réservation table 3...");
            Table table3 = dataRetriever.findTableById(3);
            TableOrder tableOrder3 = new TableOrder(table3, arrival.plus(1, ChronoUnit.HOURS),
                    departure.minus(1, ChronoUnit.HOURS));
            Order order2 = new Order(0, null, Instant.now(), dishOrders, tableOrder3);

            dataRetriever.saveOrder(order2);
            System.err.println("ERREUR: L'exception n'a pas été lancée pour double réservation!");

        } catch (RuntimeException e) {
            System.out.println("Exception attendue pour double réservation: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
        }
    }

    private static void testBasicMethods(DataRetriever dataRetriever) {
        try {
            System.out.println("\nTest findDishById:");
            Dish dish = dataRetriever.findDishById(1);
            System.out.println("- Plat ID 1: " + dish.getName() + " (" + dish.getDishType() + ")");

            System.out.println("\nTest findIngredientById:");
            Ingredient ingredient = dataRetriever.findIngredientById(1);
            System.out.println("- Ingrédient ID 1: " + ingredient.getName() +
                    " - Prix: " + ingredient.getPrice() +
                    " - Catégorie: " + ingredient.getCategory());

            System.out.println("\nTest findTableById:");
            Table table = dataRetriever.findTableById(1);
            System.out.println("- Table ID 1: numéro " + table.getNumber());

            System.out.println("\nTest saveDish:");
            Dish newDish = new Dish(0, "Nouveau plat test", DishTypeEnum.MAIN, 15000.0);
            Dish savedDish = dataRetriever.saveDish(newDish);
            System.out.println("- Plat sauvegardé: " + savedDish.getName() + " (ID: " + savedDish.getId() + ")");

            System.out.println("\nTest saveIngredient:");
            Ingredient newIngredient = new Ingredient(0, "Nouvel ingrédient", 1000.0, CategoryEnum.OTHER);
            Ingredient savedIngredient = dataRetriever.saveIngredient(newIngredient);
            System.out.println("- Ingrédient sauvegardé: " + savedIngredient.getName() +
                    " (ID: " + savedIngredient.getId() + ")");

        } catch (Exception e) {
            System.err.println("Erreur dans les tests de base: " + e.getMessage());
        }
    }
}