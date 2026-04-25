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
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.IProduit;
import abstraction.eqXRomu.produits.Chocolat;

import java.util.HashMap;
/** @author Le Clézio Brevael */
public class StockChocolat{

    private HashMap<Chocolat , Double> stock;

    public StockChocolat() {
        stock = new HashMap<>();
        stock.put(Chocolat.C_BQ , 0.0);
        stock.put(Chocolat.C_BQ_E, 0.0);
        stock.put(Chocolat.C_HQ,0.0);
        stock.put(Chocolat.C_HQ_E,100000.0);
        stock.put(Chocolat.C_MQ,0.0);
        stock.put(Chocolat.C_MQ_E,0.0);
    }

    // Ajouter un produit au stock
    public void ajouterProduit(Chocolat produit, double quantite) {
        stock.put(produit ,  quantite);
    }

    // Ajouter de la quantité à un produit existant
    public void ajouterQuantite(Chocolat produit, double quantite) {
        double actuelle = stock.getOrDefault(produit, 0.0);
        stock.put(produit, actuelle + quantite);
    }

    // Retirer une quantité
    public void retirerQuantite(Chocolat produit, double quantite) {
        double actuelle = stock.getOrDefault(produit, 0.0);
        stock.put(produit, actuelle - quantite);
    }

    // Consulter le stock d'un produit
    public double getQuantite(Chocolat produit) {
        return stock.getOrDefault(produit, 0.0);
    }

    public Set<Chocolat> getChocolat() {
    return stock.keySet();
}

    

}