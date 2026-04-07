package abstraction.eq3Producteur3;

import java.util.ArrayList;
import java.util.HashMap;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.Gamme;
import java.util.List;
import abstraction.eqXRomu.general.Journal;

/** @author Guillaume Leroy */
public class Producteur3Stock {
    private HashMap<Feve, List<Double>> stock;
    private Journal journalStock;

    public Producteur3Stock(Journal journal) {
        this.journalStock = journal;
        this.stock = new HashMap<Feve, List<Double>>();
        for (Feve f : Feve.values()) {
            this.stock.put(f, new ArrayList<Double>());
        }
    }

    public double getStock(Feve f) {
        double tot = 0;
        if (this.stock.get(f) != null) {
            for (double m : this.stock.get(f)) {
                tot = tot + m;
            }
        }
        return tot;
    }

    public double getStockTotal() {
        double stock_total = 0;
        for (Feve f : Feve.values()) {
            stock_total = stock_total + this.getStock(f);
        }
        return stock_total;
    }

    public void addStock(Feve f, Double quantite) {
        List<Double> liste = this.stock.get(f);
        liste.add(0, quantite);
        this.gererChangementQualite(f, liste);
    }

    public void retireStock(Feve f, Double quantite) {
        List<Double> liste = this.stock.get(f);
        if (liste != null && !liste.isEmpty()) {
            int i = liste.size() - 1; 
            while (quantite > 0 && i >= 0) {
                double stockActuel = liste.get(i);
                if (stockActuel >= quantite) {
                    liste.set(i, stockActuel - quantite);
                    quantite = 0.0;
                } else {
                    quantite = quantite - stockActuel;
                    liste.set(i, 0.0);
                    i--; 
                }
            }
        }
    }

    public double getCoutStockage(double cout_stockage_tonne) { 
        return this.getStockTotal() * cout_stockage_tonne;
    }

    private void gererChangementQualite(Feve f, List<Double> liste) {
        if ((f.getGamme() == Gamme.HQ) && liste.size() > 12) {
            double qte = liste.remove(12);
            if (qte > 0) {
                Feve suivante = (f == Feve.F_HQ_E) ? Feve.F_MQ_E : Feve.F_MQ;
                this.stock.get(suivante).add(0, qte);
            }
        } else if ((f.getGamme() == Gamme.MQ) && liste.size() > 24) {
            double qte = liste.remove(24);
            if (qte > 0) {
                this.stock.get(Feve.F_BQ).add(0, qte);
            }
        } else if (f.getGamme() == Gamme.BQ && liste.size() > 48) {
            liste.remove(48);
        }
    }

    public void recapJournal() {
        this.journalStock.ajouter("--- RÉCAPITULATIF DES STOCKS ---");
        for (Feve f : Feve.values()) {
            double total = this.getStock(f);
            if (total > 0) {
                List<Double> lots = this.stock.get(f);
                String detail = "";
                // On parcourt les lots (indice 0 = plus récent)
                for (int i = 0; i < lots.size(); i++) {
                    if (lots.get(i) > 0) {
                        detail = detail + "[Age " + i + ": " + lots.get(i) + "t] ";
                    }
                }
                this.journalStock.ajouter("Fève " + f + " | Total: " + total + "t | " + detail);
            }
        }
        this.journalStock.ajouter("-------------------------------");
    }
}