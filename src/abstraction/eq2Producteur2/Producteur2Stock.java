package abstraction.eq2Producteur2;

import java.util.HashMap;
import java.util.List;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.IProduit;

/** @author Thomas */
public class Producteur2Stock {

    protected Variable stockTotal;
    protected HashMap<Feve, HashMap<Integer, Double>> stock;
    protected HashMap<Feve, Double> stock_initial;
    protected HashMap<Feve, Variable> stockvar;
    protected HashMap<Feve, Double> seuil_stock;
    protected HashMap<Feve, Double> prodParStep;
    protected int cryptogramme;
    protected double cout_stockage = 7.5;

    public Producteur2Stock() {
        super();
        this.stock = new HashMap<Feve, HashMap<Integer, Double>>();
        this.stock_initial = new HashMap<Feve, Double>();
        this.stockvar = new HashMap<Feve, Variable>();
        this.seuil_stock = new HashMap<Feve, Double>();
        this.prodParStep = new HashMap<Feve, Double>();

        for (Feve f : Feve.values()) {
            this.stock.put(f, new HashMap<Integer, Double>());
            this.stock_initial.put(f, 0.0);
            this.stockvar.put(f, new Variable("Stock " + f, null, 0.0));
            this.seuil_stock.put(f, 0.0);
            this.prodParStep.put(f, 0.0);
        }

        this.stock_initial.put(Feve.F_BQ, 5000.0);
        this.stock_initial.put(Feve.F_MQ, 4000.0);
        this.stock_initial.put(Feve.F_HQ, 1200.0);
        this.stock_initial.put(Feve.F_HQ_E, 0.0);

        this.prodParStep.put(Feve.F_BQ, 200.0);
        this.prodParStep.put(Feve.F_MQ, 150.0);
        this.prodParStep.put(Feve.F_HQ, 80.0);
        this.prodParStep.put(Feve.F_HQ_E, 0.0);

        for (Feve f : Feve.values()) {
            this.stockvar.get(f).setValeur(null, this.stock_initial.get(f));
        }

        this.stockTotal = new Variable("Stock Total EQ2", null, 0.0);

        // Statut par défaut
        this.cryptogramme = 0;
    }



    public void next() {
        super.next();
        setStockMin(0.1);
        prodParStep();
        TaxeStockage();
        setTotalStock();
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

    public void prodParStep() {
        for (Feve f : Feve.values()) {
            double toProduce = this.prodParStep.getOrDefault(f, 0.0);
            if (toProduce > 0) {
                addStock(f, Filiere.LA_FILIERE.getEtape(), toProduce);
            }
        }
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


    public void TaxeStockage(){
        double tonnes = this.stockTotal.getValeur(cryptogramme);
    }

}

