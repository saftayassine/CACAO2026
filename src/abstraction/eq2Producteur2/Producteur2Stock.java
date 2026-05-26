package abstraction.eq2Producteur2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.IProduit;
import abstraction.eqXRomu.filiere.Filiere;

/** @author Thomas */
public class Producteur2Stock {

    protected Variable stockTotal;
    protected HashMap<Feve, HashMap<Integer, Double>> stock;
    protected HashMap<Feve, Double> stock_initial;
    protected HashMap<Feve, Variable> stockvar;
    protected HashMap<Feve, Double> seuil_stock;
    protected int cryptogramme;
    protected double cout_stockage = 7.5;
    protected Journal journalStock;

    public Producteur2Stock() {
        this.stock = new HashMap<Feve, HashMap<Integer, Double>>();
        this.stock_initial = new HashMap<Feve, Double>();
        this.stockvar = new HashMap<Feve, Variable>();
        this.seuil_stock = new HashMap<Feve, Double>();

        for (Feve f : Feve.values()) {
            this.stock.put(f, new HashMap<Integer, Double>());
            this.stock_initial.put(f, 0.0);
            this.stockvar.put(f, new Variable("Stock " + f, null, 0.0));
            this.seuil_stock.put(f, 0.0);
        }

        this.stock_initial.put(Feve.F_BQ, 5000.0);
        this.stock_initial.put(Feve.F_MQ, 4000.0);
        this.stock_initial.put(Feve.F_HQ, 1200.0);

        for (Feve f : Feve.values()) {
            this.stockvar.get(f).setValeur(null, this.stock_initial.get(f));
            this.stock.get(f).put(0, this.stock_initial.get(f));
        }

        this.stockTotal = new Variable("Stock Total EQ2", null, 0.0);
        this.journalStock = new Journal("Journal Stock Eq2", null);

        // Statut par défaut
        this.cryptogramme = 0;
    }



    public void next() {
        gererPeremption();
        setStockMin(0.1);
        TaxeStockage();
        setTotalStock();
    }

    public void gererPeremption() {
        int etapeActuelle = Filiere.LA_FILIERE.getEtape();
        
        // F_HQ et F_HQ_E se dégradent en F_MQ après 12 steps
        // F_MQ se dégrade en F_BQ après 24 steps
        // F_BQ se périme après 48 steps

        // 1. Péremption de BQ -> Perdu (après 48 steps)
        degraderStock(Feve.F_BQ, null, 48, etapeActuelle);

        // 2. Dégradation de MQ -> BQ (après 24 steps)
        degraderStock(Feve.F_MQ, Feve.F_BQ, 24, etapeActuelle);

        // 3. Dégradation de HQ -> MQ (après 12 steps)
        degraderStock(Feve.F_HQ, Feve.F_MQ, 12, etapeActuelle);

        // 4. Dégradation de HQ_E -> MQ (après 12 steps, on perd le label équitable)
        degraderStock(Feve.F_HQ_E, Feve.F_MQ, 12, etapeActuelle);
    }

    private void degraderStock(Feve source, Feve destination, int ageLimite, int etapeActuelle) {
        HashMap<Integer, Double> stockSource = this.stock.get(source);
        if (stockSource == null) return;
        
        List<Integer> steps = new ArrayList<Integer>(stockSource.keySet());
        double quantiteDegradeeTotale = 0.0;
        
        for (Integer stepProd : steps) {
            int age = etapeActuelle - stepProd;
            if (age >= ageLimite) {
                double quantite = stockSource.get(stepProd);
                stockSource.remove(stepProd);
                
                if (destination != null) {
                    // On transfère vers la destination avec le même step_prod d'origine (l'âge est conservé)
                    HashMap<Integer, Double> stockDest = this.stock.get(destination);
                    stockDest.put(stepProd, stockDest.getOrDefault(stepProd, 0.0) + quantite);
                    
                    // Mise à jour rapide des variables (sera recalculé via setTotalStock de toute façon)
                    Variable vSource = this.stockvar.get(source);
                    Variable vDest = this.stockvar.get(destination);
                    if (vSource != null) vSource.retirer(null, quantite, this.cryptogramme);
                    if (vDest != null) vDest.ajouter(null, quantite, this.cryptogramme);
                } else {
                    // Perte sèche, pas de destination
                    Variable vSource = this.stockvar.get(source);
                    if (vSource != null) vSource.retirer(null, quantite, this.cryptogramme);
                    this.stockTotal.retirer(null, quantite, this.cryptogramme);
                }
                quantiteDegradeeTotale += quantite;
            }
        }
        
        // Log s'il y a eu des dégradations
        if (quantiteDegradeeTotale > 0.0) {
            if (destination != null) {
                this.journalStock.ajouter(Filiere.LA_FILIERE.getEtape() + " : DÉGRADATION - " + quantiteDegradeeTotale + " T de " + source + " sont devenues " + destination + " (Âge >= " + ageLimite + " steps)");
            } else {
                this.journalStock.ajouter(Filiere.LA_FILIERE.getEtape() + " : PÉREMPTION - " + quantiteDegradeeTotale + " T de " + source + " ont été jetées (Âge >= " + ageLimite + " steps)");
            }
        }
    }

    public void setTotalStock() {
        double totalstock = 0.0;
        for (Feve f : Feve.values()) {
            double nb = 0;
            for (Integer k : this.stock.get(f).keySet()) {
                nb += this.stock.get(f).get(k);
            }
            totalstock += nb;
            this.stockvar.get(f).setValeur(null, nb);
        }
        this.stockTotal.setValeur(null, totalstock);
    }

    public void addStock(Feve f, int step_prod, double prod) {
        if (!this.stock.containsKey(f)) {
            return;
        }

        Variable v = this.stockvar.get(f);
        if (v != null) {
            v.ajouter(null, prod);
        }

        Double actual_value = this.stock.get(f).get(step_prod);
        if (actual_value == null) {
            this.stock.get(f).put(step_prod, prod);
        } else {
            this.stock.get(f).put(step_prod, prod + actual_value);
        }

        this.stockTotal.ajouter(null, prod);

    }

    protected double retirerDuStock(Feve f, double quantite) {
        if (f == null || quantite <= 0.0 || !this.stock.containsKey(f)) {
            return 0.0;
        }
        HashMap<Integer, Double> stockFeve = this.stock.get(f);
        List<Integer> steps = new ArrayList<Integer>(stockFeve.keySet());
        Collections.sort(steps);

        double restant = quantite;
        for (Integer step : steps) {
            if (restant <= 0.0) {
                break;
            }
            double disponible = stockFeve.getOrDefault(step, 0.0);
            if (disponible <= 0.0) {
                continue;
            }
            double retire = Math.min(disponible, restant);
            double nouveauStock = disponible - retire;
            if (nouveauStock <= 0.0) {
                stockFeve.remove(step);
            } else {
                stockFeve.put(step, nouveauStock);
            }
            restant -= retire;
        }
        double totalRetire = quantite - restant;
        if (totalRetire > 0.0) {
            Variable v = this.stockvar.get(f);
            if (v != null) {
                v.retirer(null, totalRetire, this.cryptogramme);
            }
            this.stockTotal.retirer(null, totalRetire, this.cryptogramme);
        }
        return totalRetire;
    }

    public void setStockMin(double pourcentage) {
        for (Feve f : Feve.values()) {
            double prod = this.stockvar.get(f).getValeur();
            this.seuil_stock.put(f, pourcentage * prod);
        }
    }

    public double getQuantiteEnStock(IProduit p, int cryptogramme) {
        if (!(p instanceof Feve)) {
            return 0.0;
        }
        Feve f = (Feve) p;
        double total = 0.0;
        for (Double q : this.stock.get(f).values()) {
            total += q;
        }
        return total;
    }

    public int getAgeAnciennete(Feve f) {
        HashMap<Integer, Double> stockFeve = this.stock.get(f);
        if (stockFeve == null || stockFeve.isEmpty()) return 0;
        int etapeActuelle = Filiere.LA_FILIERE.getEtape();
        int stepPlusAncien = etapeActuelle;
        boolean found = false;
        for (Integer stepProd : stockFeve.keySet()) {
            if (stockFeve.get(stepProd) > 1.0 && stepProd < stepPlusAncien) { // Ignore les poussières (< 1 tonne)
                stepPlusAncien = stepProd;
                found = true;
            }
        }
        return found ? (etapeActuelle - stepPlusAncien) : 0;
    }


    public void TaxeStockage(){
    }

}

