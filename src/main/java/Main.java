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
            testValidOrderCreation(dataRetriever);


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
}