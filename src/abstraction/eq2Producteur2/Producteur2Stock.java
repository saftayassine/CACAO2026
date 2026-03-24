package abstraction.eq2Producteur2;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IActeur;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.IProduit;
import abstraction.eqXRomu.bourseCacao.IVendeurBourse;

/** @author Thomas */
public class Producteur2Stock {
    protected Variable stockTotal;
	protected HashMap<Feve,HashMap<Integer,Double>> stock;
    protected HashMap<Feve,Double> stock_initial;
    double cout_stockage = 7.5;


    public Producteur2Stock(){
        this.stock = new HashMap<Feve,HashMap<Integer,Double>>();
        for (Feve f : Feve.values()) {
            this.stock.put(f,new HashMap<Integer,Double>());
        }
        this.stock_initial = new HashMap<Feve,Double>();

        this.stock_initial.put(Feve.F_BQ,1000.0);
        this.stock_initial.put(Feve.F_BQ_E,0.0);
        this.stock_initial.put(Feve.F_MQ,1000.0);
        this.stock_initial.put(Feve.F_MQ_E,0.0);
        this.stock_initial.put(Feve.F_HQ_E,0.0);
        this.stock_initial.put(Feve.F_HQ,0.0);

    }
}
