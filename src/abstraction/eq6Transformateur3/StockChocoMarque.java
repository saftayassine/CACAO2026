package abstraction.eq6Transformateur3;


import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IActeur;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.IProduit;

/** @author Selma Ben Abdelkader */
public class StockChocoMarque{   
    private HashMap<ChocolatDeMarque , Double> stock;
    
    public StockChocoMarque() {
        stock = new HashMap<>();

    }
    public void ajouterProduit(ChocolatDeMarque produit, double quantite) {
        stock.put(produit ,  quantite);
    }

    // Ajouter de la quantité à un produit existant
    public void ajouterQuantite(ChocolatDeMarque produit, double quantite) {
        double actuelle = stock.getOrDefault(produit, 0.0);
        stock.put(produit, actuelle + quantite);
    }

    // Retirer une quantité
    public void retirerQuantite(ChocolatDeMarque produit, double quantite) {
        double actuelle = stock.getOrDefault(produit, 0.0);
        stock.put(produit, actuelle - quantite);
    }

    // Consulter le stock d'un produit
    public double getQuantite(ChocolatDeMarque produit) {
        return stock.getOrDefault(produit, 0.0);
    }

    public Set<ChocolatDeMarque> getChocolatsDeMarque() {
    return stock.keySet();
}
}
