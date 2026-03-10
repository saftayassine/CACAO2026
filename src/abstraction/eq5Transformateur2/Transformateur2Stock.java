package abstraction.eq5Transformateur2;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.lang.Integer;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IActeur;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.IProduit;

public class Transformateur2Stock extends Transformateur2Acteur{

    // Attributs
    private HashMap<Feve, Double> stock_feve;
    private HashMap<Chocolat, Double> stock_chocolat;
    
    // Constructeur

    /** @author Pierre
     * @author Raphaël
    **/ 
    public Transformateur2Stock(){
        this.stock_feve = new HashMap<Feve, Double>();
        this.stock_feve.put(Feve.F_BQ, 0.0);
        this.stock_feve.put(Feve.F_BQ_E, 0.0);
        this.stock_feve.put(Feve.F_MQ, 0.0);
        this.stock_feve.put(Feve.F_MQ_E, 0.0);
        this.stock_feve.put(Feve.F_HQ, 0.0);
        this.stock_feve.put(Feve.F_HQ_E, 0.0);
        this.stock_chocolat = new HashMap<Chocolat, Double>();
        this.stock_chocolat.put(Chocolat.C_BQ, 0.0);
        this.stock_chocolat.put(Chocolat.C_BQ_E, 0.0);
        this.stock_chocolat.put(Chocolat.C_MQ, 0.0);
        this.stock_chocolat.put(Chocolat.C_MQ_E, 0.0);
        this.stock_chocolat.put(Chocolat.C_HQ, 0.0);
        this.stock_chocolat.put(Chocolat.C_HQ_E, 0.0);
    }

    // Méthodes

    /** @author Pierre
    **/
    public Double getStock_feve(){
        return this.stock_feve.get(Feve.F_BQ) + this.stock_feve.get(Feve.F_BQ_E) + this.stock_feve.get(Feve.F_MQ) + this.stock_feve.get(Feve.F_MQ_E) + this.stock_feve.get(Feve.F_HQ) + this.stock_feve.get(Feve.F_HQ_E);
        }
    
    /** @author Pierre
    **/
    public Double getStock_chocolat(){
        return this.stock_chocolat.get(Chocolat.C_BQ) + this.stock_feve.get(Chocolat.C_BQ_E) + this.stock_feve.get(Chocolat.C_MQ) + this.stock_feve.get(Chocolat.C_MQ_E) + this.stock_feve.get(Chocolat.C_HQ) + this.stock_feve.get(Chocolat.C_HQ_E);
        }

    /** @author Pierre
    **/
    public void add_feve(Double n, Feve q){
        assert n >= 0;
        this.stock_feve.put(q, this.stock_feve.get(q) + n);
    }

    /** @author Pierre
    **/
    public void remove_feve(Double n, Feve q){
        assert n <= 0;
        if (n <= this.stock_feve.get(q)){
            this.stock_feve.put(q, this.stock_feve.get(q) - n);
        }
    }

    /** @author Raphaël
    **/
    public void add_chocolat(Double n, Chocolat q){
        assert n >= 0;
        this.stock_chocolat.put(q, this.stock_chocolat.get(q) + n);
    }

    /** @author Raphaël
    **/
    public void remove_chocolat(Double n, Chocolat q){
        assert n <= 0;
        if (n <= this.stock_chocolat.get(q)){
            this.stock_chocolat.put(q, this.stock_chocolat.get(q) - n); 
            }
    }

}
