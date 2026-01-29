// TestRestaurantTables.java
import models.*;
import db.DBConnection;
import services.DataRetriever;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class TestRestaurantTables {

    public static void main(String[] args) {
        System.out.println("=== Test des nouvelles fonctionnalités de gestion des tables ===\n");

        // Initialisation
        DBConnection dbConnection = new DBConnection();
        DataRetriever dataRetriever = new DataRetriever(dbConnection);

        try {
            // Test 1: Vérification de la disponibilité d'une table
            System.out.println("Test 1: Vérification de disponibilité des tables");
            testTableAvailability(dataRetriever);

            System.out.println("\n---\n");

            // Test 2: Création d'une commande avec table disponible
            System.out.println("Test 2: Création d'une commande avec table disponible");
            testSuccessfulOrderWithTable(dataRetriever);

            System.out.println("\n---\n");

            // Test 3: Tentative de création d'une commande avec table occupée
            System.out.println("Test 3: Tentative de création d'une commande avec table occupée");
            testFailedOrderWithOccupiedTable(dataRetriever);

            System.out.println("\n---\n");

            // Test 4: Message d'erreur avec suggestions de tables disponibles
            System.out.println("Test 4: Message d'erreur avec suggestions");
            testErrorWithAvailableSuggestions(dataRetriever);

            System.out.println("\n=== Tous les tests sont terminés ===");

        } catch (Exception e) {
            System.err.println("Erreur pendant les tests: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void testTableAvailability(DataRetriever dataRetriever) {
        try {
            Instant now = Instant.now();
            Instant arrival = now.plus(1, ChronoUnit.HOURS);
            Instant departure = arrival.plus(2, ChronoUnit.HOURS);

            // Tester les tables disponibles
            List<Integer> availableTables = dataRetriever.findAvailableTables(arrival, departure);
            System.out.println("Tables disponibles de " + arrival + " à " + departure + " : " + availableTables);

            // Tester une table spécifique
            boolean isTable1Available = dataRetriever.isTableAvailable(1, arrival, departure);
            System.out.println("Table 1 disponible ? " + isTable1Available);

            // Tester la méthode findTableById
            Table table2 = dataRetriever.findTableById(2);
            System.out.println("Table chargée: #" + table2.getNumber() + " (id=" + table2.getId() + ")");

        } catch (Exception e) {
            System.err.println("Erreur dans testTableAvailability: " + e.getMessage());
        }
    }

    private static void testSuccessfulOrderWithTable(DataRetriever dataRetriever) {
        try {
            // Charger un plat existant
            Dish dish = dataRetriever.findDishById(1); // Salade fraiche
            System.out.println("Plat chargé: " + dish.getName() + " (id=" + dish.getId() + ")");

            // Créer les DishOrders
            List<DishOrder> dishOrders = new ArrayList<>();
            dishOrders.add(new DishOrder(0, dish, 2)); // 2 salades fraiches

            // Créer la table et les horaires
            Table table = dataRetriever.findTableById(3); // Table #3
            Instant arrival = Instant.now().plus(3, ChronoUnit.HOURS);
            Instant departure = arrival.plus(1, ChronoUnit.HOURS);
            TableOrder tableOrder = new TableOrder(table, arrival, departure);

            // Créer la commande sans ID (sera généré automatiquement)
            Order newOrder = new Order(0, null, Instant.now(), dishOrders, tableOrder);

            // Sauvegarder la commande
            Order savedOrder = dataRetriever.saveOrder(newOrder);

            System.out.println("Commande créée avec succès!");
            System.out.println("Référence: " + savedOrder.getReference());
            System.out.println("Table: #" + savedOrder.getTable().getNumber());
            System.out.println("Arrivée: " + savedOrder.getArrivalDateTime());
            System.out.println("Départ: " + savedOrder.getDepartureDateTime());
            System.out.println("Montant total: " + savedOrder.getTotalAmountWithVAT() + " (avec TVA)");

            // Recharger pour vérifier
            Order loadedOrder = dataRetriever.findOrderByReference(savedOrder.getReference());
            System.out.println("Commande rechargée - Table: #" + loadedOrder.getTable().getNumber());

        } catch (Exception e) {
            System.err.println("Erreur dans testSuccessfulOrderWithTable: " + e.getMessage());
        }
    }

    private static void testFailedOrderWithOccupiedTable(DataRetriever dataRetriever) {
        try {
            // Première commande pour occuper la table 1
            System.out.println("Création d'une première commande pour table 1...");

            Dish dish = dataRetriever.findDishById(2); // Poulet grille
            Table table1 = dataRetriever.findTableById(1);

            Instant arrival1 = Instant.now().plus(4, ChronoUnit.HOURS);
            Instant departure1 = arrival1.plus(1, ChronoUnit.HOURS);

            List<DishOrder> dishOrders1 = new ArrayList<>();
            dishOrders1.add(new DishOrder(0, dish, 1));

            Order order1 = new Order(0, null, Instant.now(), dishOrders1,
                    new TableOrder(table1, arrival1, departure1));

            Order savedOrder1 = dataRetriever.saveOrder(order1);
            System.out.println("Première commande créée: " + savedOrder1.getReference());

            // Tentative de deuxième commande sur la même table aux mêmes horaires
            System.out.println("\nTentative de deuxième commande sur la même table...");

            Dish anotherDish = dataRetriever.findDishById(4); // Gateau au chocolat

            List<DishOrder> dishOrders2 = new ArrayList<>();
            dishOrders2.add(new DishOrder(0, anotherDish, 2));

            Order order2 = new Order(0, null, Instant.now(), dishOrders2,
                    new TableOrder(table1, arrival1.plus(30, ChronoUnit.MINUTES),
                            departure1.plus(30, ChronoUnit.MINUTES)));

            try {
                dataRetriever.saveOrder(order2);
                System.err.println("ERREUR: La commande aurait dû échouer (table occupée)!");
            } catch (RuntimeException e) {
                System.out.println("SUCCÈS: Exception attendue capturée");
                System.out.println("Message: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("Erreur dans testFailedOrderWithOccupiedTable: " + e.getMessage());
        }
    }

    private static void testErrorWithAvailableSuggestions(DataRetriever dataRetriever) {
        try {
            // Créer plusieurs commandes pour occuper des tables
            System.out.println("Occupation de plusieurs tables...");

            Table table1 = dataRetriever.findTableById(1);
            Table table2 = dataRetriever.findTableById(2);

            Instant baseTime = Instant.now().plus(5, ChronoUnit.HOURS);
            Dish dish = dataRetriever.findDishById(3); // Riz aux legumes

            // Occuper la table 1
            List<DishOrder> orders1 = new ArrayList<>();
            orders1.add(new DishOrder(0, dish, 1));

            Order order1 = new Order(0, null, Instant.now(), orders1,
                    new TableOrder(table1, baseTime, baseTime.plus(2, ChronoUnit.HOURS)));
            dataRetriever.saveOrder(order1);
            System.out.println("Table 1 occupée");

            // Occuper la table 2
            List<DishOrder> orders2 = new ArrayList<>();
            orders2.add(new DishOrder(0, dish, 2));

            Order order2 = new Order(0, null, Instant.now(), orders2,
                    new TableOrder(table2, baseTime, baseTime.plus(1, ChronoUnit.HOURS)));
            dataRetriever.saveOrder(order2);
            System.out.println("Table 2 occupée");

            // Tenter de réserver la table 1 (déjà occupée)
            System.out.println("\nTentative de réservation de la table 1 (occupée)...");

            List<DishOrder> newOrders = new ArrayList<>();
            newOrders.add(new DishOrder(0, dish, 1));

            Order conflictingOrder = new Order(0, null, Instant.now(), newOrders,
                    new TableOrder(table1, baseTime.plus(30, ChronoUnit.MINUTES),
                            baseTime.plus(3, ChronoUnit.HOURS)));

            try {
                dataRetriever.saveOrder(conflictingOrder);
                System.err.println("ERREUR: La commande aurait dû échouer!");
            } catch (RuntimeException e) {
                System.out.println("SUCCÈS: Exception capturée avec suggestions");
                System.out.println("Message d'erreur: " + e.getMessage());

                // Vérifier que le message contient des suggestions
                if (e.getMessage().contains("Available tables")) {
                    System.out.println("✓ Le message contient des suggestions de tables disponibles");
                } else {
                    System.out.println("✗ Le message ne contient pas de suggestions");
                }
            }

            // Tester le cas où aucune table n'est disponible
            System.out.println("\nTest du cas où aucune table n'est disponible...");

            // Occuper toutes les tables
            for (int i = 3; i <= 5; i++) {
                Table table = dataRetriever.findTableById(i);
                List<DishOrder> dishOrders = new ArrayList<>();
                dishOrders.add(new DishOrder(0, dish, 1));

                Order order = new Order(0, null, Instant.now(), dishOrders,
                        new TableOrder(table, baseTime, baseTime.plus(3, ChronoUnit.HOURS)));
                dataRetriever.saveOrder(order);
                System.out.println("Table " + i + " occupée");
            }

            // Essayer de créer une nouvelle commande
            Table table3 = dataRetriever.findTableById(3);
            List<DishOrder> finalOrders = new ArrayList<>();
            finalOrders.add(new DishOrder(0, dish, 1));

            Order finalOrder = new Order(0, null, Instant.now(), finalOrders,
                    new TableOrder(table3, baseTime.plus(1, ChronoUnit.HOURS),
                            baseTime.plus(4, ChronoUnit.HOURS)));

            try {
                dataRetriever.saveOrder(finalOrder);
                System.err.println("ERREUR: La commande aurait dû échouer!");
            } catch (RuntimeException e) {
                System.out.println("SUCCÈS: Exception capturée");
                System.out.println("Message: " + e.getMessage());

                if (e.getMessage().contains("No tables are available")) {
                    System.out.println("✓ Message correct pour aucune table disponible");
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur dans testErrorWithAvailableSuggestions: " + e.getMessage());
            e.printStackTrace();
        }
    }
}