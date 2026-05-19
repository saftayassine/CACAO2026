package abstraction.eq9Distributeur2;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.general.Journal;

/**

 */
public class EQ9_GestionnaireMarques {
    
    private Journal journal;
    
    // Marques par transformateur
    private Map<String, MarqueTransformateur> marques = new HashMap<>();
    
    // Stock par marque (en kg)
    private Map<String, Double> stockParMarque = new HashMap<>();
    
    // Prix de vente par marque (en €/T)
    private Map<String, Double> prixParMarque = new HashMap<>();
    
    // Demande estimée par marque (en kg)
    private Map<String, Double> demandeParMarque = new HashMap<>();
    
    public EQ9_GestionnaireMarques(Journal j) {
        this.journal = j;
        initialiserMarques();
    }
    
    // Initialise les marques de transformateurs connues
    private void initialiserMarques() {
        // EQ4 - Prontella
        marques.put("Prontella", new MarqueTransformateur(
            "Prontella",
            "EQ4",
            0.20  // 20% de la demande marché
        ));
        
        // EQ5 - ChocolatBrand
        marques.put("ChocolatBrand", new MarqueTransformateur(
            "ChocolatBrand",
            "EQ5",
            0.30  // 30%
        ));
        
        // EQ6 - (à définir)
        marques.put("Marque_EQ6", new MarqueTransformateur(
            "Marque_EQ6",
            "EQ6",
            0.15  // 15%
        ));
        
        // EQ7 - (à définir)
        marques.put("Marque_EQ7", new MarqueTransformateur(
            "Marque_EQ7",
            "EQ7",
            0.10  // 10%
        ));
        
        // EQ9 - NOTRE MARQUE
        marques.put("EQ9", new MarqueTransformateur(
            "EQ9",
            "EQ9",
            0.25  // 25% - notre part du marché
        ));
        
        journal.ajouter("Gestionnaire marques initialisé : " + marques.size() + " marques");
    }
    

    //marques disponibles
    public List<String> obtenirNomMarques() {
        return new ArrayList<>(marques.keySet());
    }
    

    //maj stock par marque

    public void mettreAJourStock(String nomMarque, double quantiteKg) {
        if (marques.containsKey(nomMarque)) {
            stockParMarque.put(nomMarque, quantiteKg);
        } else {
            journal.ajouter("Marque inconnue : " + nomMarque);
        }
    }
    
    // maj prix par marque
    public void mettreAJourPrix(String nomMarque, double prixEuroParTonne) {
        if (marques.containsKey(nomMarque)) {
            prixParMarque.put(nomMarque, prixEuroParTonne);
        }
    }
    
    // demande par marque
    public void mettreAJourDemande(String nomMarque, double demandeEstimeeKg) {
        if (marques.containsKey(nomMarque)) {
            demandeParMarque.put(nomMarque, demandeEstimeeKg);
        }
    }
    
    // stock par marque
    public double obtenirStock(String nomMarque) {
        return stockParMarque.getOrDefault(nomMarque, 0.0);
    }
    
    // demande par marque
    public double obtenirDemande(String nomMarque) {
        return demandeParMarque.getOrDefault(nomMarque, 0.0);
    }
    
    // part de marché par marque    
    public double obtenirPartMarche(String nomMarque) {
        MarqueTransformateur marque = marques.get(nomMarque);
        return (marque != null) ? marque.partMarchePercent : 0;
    }
    
    // stock total toutes marques
    public double obtenirStockTotal() {
        return stockParMarque.values().stream()
            .mapToDouble(Double::doubleValue)
            .sum();
    }
    
    // prix moyen toutes marques (pondéré par stock)
    public double obtenirPrixMoyen() {
        double stockTotal = obtenirStockTotal();
        if (stockTotal == 0) return 0;
        
        double prixPondere = 0;
        for (String marque : marques.keySet()) {
            double stock = obtenirStock(marque);
            double prix = prixParMarque.getOrDefault(marque, 150.0);
            prixPondere += (stock / stockTotal) * prix;
        }
        return prixPondere;
    }
    
    //composition stock par marque
    public void afficherCompositionStock() {
        double total = obtenirStockTotal();
        journal.ajouter("=== COMPOSITION STOCK EQ9 ===");
        
        for (String marque : marques.keySet()) {
            double stock = obtenirStock(marque);
            double pct = (total > 0) ? (stock / total) * 100 : 0;
            journal.ajouter("  " + marque + " : " + 
                           String.format("%.0f", stock/1000) + "T (" + 
                           String.format("%.1f%%", pct) + ")");
        }
        
        journal.ajouter("  TOTAL : " + String.format("%.0f", total/1000) + "T");
    }
    
    //Représente une marque
    private static class MarqueTransformateur {
        String nom;
        String transformateur;  // EQ4, EQ5, etc.
        double partMarchePercent;  // % de la demande totale que cette marque représente
        
        public MarqueTransformateur(String n, String t, double p) {
            this.nom = n;
            this.transformateur = t;
            this.partMarchePercent = p;
        }
    }
}
