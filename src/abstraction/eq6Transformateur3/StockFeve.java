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
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.IProduit;

import java.util.HashMap;
/** @author Le Clézio Brevael */
public class StockFeve{

    private HashMap<Feve , Double> stock;

    public StockFeve() {
        stock = new HashMap<>();
        stock.put(Feve.F_BQ , 100000.0);
        stock.put(Feve.F_BQ_E, 0.0);
        stock.put(Feve.F_HQ,0.0);
        stock.put(Feve.F_HQ_E,0.0);
        stock.put(Feve.F_MQ,0.0);
        stock.put(Feve.F_MQ_E,0.0);

    }

    // Ajouter un produit au stock
    public void ajouterProduit(Feve produit, double quantite) {
        stock.put(produit ,  quantite);
    }

    // Ajouter de la quantité à un produit existant
    public void ajouterQuantite(Feve produit, double quantite) {
        double actuelle = stock.getOrDefault(produit, 0.0);
        stock.put(produit, actuelle + quantite);
    }

    // Retirer une quantité
    public void retirerQuantite(Feve produit, double quantite) {
        double actuelle = stock.getOrDefault(produit, 0.0);
        stock.put(produit, actuelle - quantite);
    }

    // Consulter le stock d'un produit
    public double getQuantite(Feve produit) {
        return stock.getOrDefault(produit, 0.0);
    }

    public Set<Feve> getFeves() {
    return stock.keySet();
}
}