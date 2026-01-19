package restaurantManagement;

public class Main {
    public static void main(String[] args) {
        restaurantManagement.DataRetriever dataRetriever = new restaurantManagement.DataRetriever();

        System.out.println("=== TEST 1 : Récupération de plats et calcul de marge ===");
        System.out.println();

        try {
            System.out.println("1a) Test avec Salade fraîche (ID=1, prix=2000):");
            Dish dish1 = dataRetriever.findDishById(1);
            System.out.println("   Plat récupéré: " + dish1.getName());
            System.out.println("   Type: " + dish1.getDishType());
            System.out.println("   Prix de vente: " + (dish1.getPrice() != null ? dish1.getPrice() + " FCFA" : "Non défini"));
            System.out.println("   Nombre d'ingrédients: " + dish1.getIngredients().size());
            System.out.println("   Coût des ingrédients (getDishCost): " + dish1.getDishCost() + " FCFA");
            System.out.println("   Marge brute (getGrossMargin): " + dish1.getGrossMargin() + " FCFA");
            System.out.println();
        } catch (RuntimeException e) {
            System.out.println("   Erreur: " + e.getMessage());
            System.out.println();
        }

        try {
            System.out.println("1b) Test avec Poulet grillé (ID=2, prix=6000):");
            Dish dish2 = dataRetriever.findDishById(2);
            System.out.println("   Plat récupéré: " + dish2.getName());
            System.out.println("   Prix de vente: " + dish2.getPrice() + " FCFA");
            System.out.println("   Coût des ingrédients: " + dish2.getDishCost() + " FCFA");
            System.out.println("   Marge brute: " + dish2.getGrossMargin() + " FCFA");
            System.out.println();
        } catch (RuntimeException e) {
            System.out.println("   Erreur: " + e.getMessage());
            System.out.println();
        }


        try {
            System.out.println("1c) Test avec Gâteau au chocolat (ID=4, prix=null):");
            Dish dish4 = dataRetriever.findDishById(4);
            System.out.println("   Plat récupéré: " + dish4.getName());
            System.out.println("   Prix de vente: " + (dish4.getPrice() != null ? dish4.getPrice() + " FCFA" : "Non défini"));
            System.out.println("   Coût des ingrédients: " + dish4.getDishCost() + " FCFA");

            System.out.println("   Tentative de calcul de marge...");
            Double margin = dish4.getGrossMargin();
            System.out.println("   Marge brute: " + margin + " FCFA");
            System.out.println("   ERREUR: L'exception aurait dû être levée!");
            System.out.println();
        } catch (RuntimeException e) {
            System.out.println("   ✓ Exception levée comme attendu: " + e.getMessage());
            System.out.println();
        }

        try {
            System.out.println("1d) Test avec Riz aux légumes (ID=3, prix=null):");
            Dish dish3 = dataRetriever.findDishById(3);
            System.out.println("   Plat récupéré: " + dish3.getName());
            System.out.println("   Prix de vente: " + (dish3.getPrice() != null ? dish3.getPrice() + " FCFA" : "Non défini"));

            Double margin = dish3.getGrossMargin();
            System.out.println("   ERREUR: L'exception aurait dû être levée!");
            System.out.println();
        } catch (RuntimeException e) {
            System.out.println("   ✓ Exception levée comme attendu: " + e.getMessage());
            System.out.println();
        }

        System.out.println("=== TEST 2 : Création d'un nouveau plat ===");
        System.out.println();


        try {
            System.out.println("2) Création d'un nouveau plat 'Soupe à l'oignon':");
            Dish newDish = new Dish(6, "Soupe à l'oignon", DishTypeEnum.START, 1800.0);
            System.out.println("   Avant sauvegarde - ID: " + newDish.getId());
            System.out.println("   Avant sauvegarde - Nom: " + newDish.getName());
            System.out.println("   Avant sauvegarde - Prix: " + newDish.getPrice() + " FCFA");

            Dish savedDish = dataRetriever.saveDish(newDish);
            System.out.println("   ✓ Plat sauvegardé avec succès");
            System.out.println("   Après sauvegarde - ID: " + savedDish.getId());
            System.out.println("   Après sauvegarde - Nom: " + savedDish.getName());
            System.out.println("   Après sauvegarde - Prix: " + savedDish.getPrice() + " FCFA");
            System.out.println();
        } catch (RuntimeException e) {
            System.out.println("   Erreur lors de la création: " + e.getMessage());
            System.out.println();
        }

        System.out.println("=== TEST 3 : Mise à jour d'un plat existant ===");
        System.out.println();

        try {
            System.out.println("3) Mise à jour du prix du 'Riz aux légumes' (ID=3):");

            Dish dishToUpdate = dataRetriever.findDishById(3);
            System.out.println("   Avant mise à jour:");
            System.out.println("     Nom: " + dishToUpdate.getName());
            System.out.println("     Prix: " + (dishToUpdate.getPrice() != null ? dishToUpdate.getPrice() + " FCFA" : "Non défini"));
            System.out.println("     Coût ingrédients: " + dishToUpdate.getDishCost() + " FCFA");

            dishToUpdate.setPrice(3200.0);
            System.out.println("   Prix modifié à: " + dishToUpdate.getPrice() + " FCFA");

            Dish updatedDish = dataRetriever.saveDish(dishToUpdate);
            System.out.println("   ✓ Plat mis à jour avec succès");

            System.out.println("   Après mise à jour:");
            System.out.println("     Prix: " + updatedDish.getPrice() + " FCFA");
            System.out.println("     Coût ingrédients: " + updatedDish.getDishCost() + " FCFA");
            System.out.println("     Marge brute: " + updatedDish.getGrossMargin() + " FCFA");
            System.out.println();
        } catch (RuntimeException e) {
            System.out.println("   Erreur lors de la mise à jour: " + e.getMessage());
            System.out.println();
        }

        System.out.println("=== TEST 4 : Vérification de la persistance ===");
        System.out.println();

        try {
            System.out.println("4) Vérification de la persistance des modifications:");

            System.out.println("   a) Vérification du plat créé (ID=6):");
            Dish retrievedDish6 = dataRetriever.findDishById(6);
            if (retrievedDish6 != null) {
                System.out.println("     ✓ Plat trouvé: " + retrievedDish6.getName());
                System.out.println("     Prix: " + retrievedDish6.getPrice() + " FCFA");
            } else {
                System.out.println("     ✗ Plat non trouvé!");
            }
            System.out.println();

            System.out.println("   b) Vérification du plat mis à jour (ID=3):");
            Dish retrievedDish3 = dataRetriever.findDishById(3);
            if (retrievedDish3 != null) {
                System.out.println("     ✓ Plat trouvé: " + retrievedDish3.getName());
                System.out.println("     Prix: " + retrievedDish3.getPrice() + " FCFA");
                System.out.println("     Coût ingrédients: " + retrievedDish3.getDishCost() + " FCFA");

                try {
                    System.out.println("     Marge brute: " + retrievedDish3.getGrossMargin() + " FCFA");
                } catch (RuntimeException e) {
                    System.out.println("     Erreur calcul marge: " + e.getMessage());
                }
            } else {
                System.out.println("     ✗ Plat non trouvé!");
            }
            System.out.println();

            System.out.println("   c) Test d'un plat inexistant (ID=999):");
            try {
                Dish nonExistent = dataRetriever.findDishById(999);
                System.out.println("     ✗ ERREUR: Le plat aurait dû lever une exception!");
            } catch (RuntimeException e) {
                System.out.println("     ✓ Exception levée comme attendu: " + e.getMessage());
            }
            System.out.println();

        } catch (RuntimeException e) {
            System.out.println("   Erreur lors de la vérification: " + e.getMessage());
            System.out.println();
        }

        System.out.println("=== TEST 5 : Test complet avec tous les plats ===");
        System.out.println();

        System.out.println("5) Liste de tous les plats avec leurs informations:");
        for (int i = 1; i <= 6; i++) {
            try {
                Dish dish = dataRetriever.findDishById(i);
                System.out.println("   Plat ID=" + i + ": " + dish.getName());
                System.out.println("     Type: " + dish.getDishType());
                System.out.println("     Prix vente: " + (dish.getPrice() != null ? dish.getPrice() + " FCFA" : "Non défini"));
                System.out.println("     Coût ingrédients: " + dish.getDishCost() + " FCFA");
                System.out.println("     Nombre ingrédients: " + dish.getIngredients().size());

                try {
                    Double margin = dish.getGrossMargin();
                    System.out.println("     Marge brute: " + margin + " FCFA");
                    if (margin < 0) {
                        System.out.println("    ATTENTION: Marge négative!");
                    }
                } catch (RuntimeException e) {
                    System.out.println("     Marge brute: Impossible à calculer (prix non défini)");
                }

                if (i < 6) System.out.println();

            } catch (RuntimeException e) {
                // Ignorer les plats qui n'existent pas
            }
        }

        System.out.println();
        System.out.println("=== TESTS TERMINÉS ===");
        System.out.println("Vérifiez que:");
        System.out.println("1. La marge est calculée correctement quand le prix existe");
        System.out.println("2. Une exception est levée quand le prix est null");
        System.out.println("3. La sauvegarde fonctionne pour création et mise à jour");
        System.out.println("4. Les données sont correctement persistées en base");
    }
}