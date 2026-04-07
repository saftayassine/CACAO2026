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
    private Variable stock_feve_affichage;
    private Variable stock_chocolat_affichage;
    
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
        
        this.stock_feve_affichage=new Variable("EQ5 Stock Fève", this, 0);
        this.stock_chocolat_affichage=new Variable("EQ5 Stock Chocloat", this, 0);
    }

    // Méthodes
    
	/** @author Pierre
    **/
	public List<Variable> getIndicateurs() {
		List<Variable> res = super.getIndicateurs();
        res.add(this.stock_feve_affichage);
        res.add(this.stock_chocolat_affichage);
		return res;
	}

    /** @author Pierre
    **/
    public Double getStock_feve_total(){
        return this.stock_feve.get(Feve.F_BQ) + this.stock_feve.get(Feve.F_BQ_E) + this.stock_feve.get(Feve.F_MQ) + this.stock_feve.get(Feve.F_MQ_E) + this.stock_feve.get(Feve.F_HQ) + this.stock_feve.get(Feve.F_HQ_E);
        }
    
    /** @author Pierre
    **/
    public Double getStock_chocolat_total(){
        return this.stock_chocolat.get(Chocolat.C_BQ) + this.stock_feve.get(Chocolat.C_BQ_E) + this.stock_feve.get(Chocolat.C_MQ) + this.stock_feve.get(Chocolat.C_MQ_E) + this.stock_feve.get(Chocolat.C_HQ) + this.stock_feve.get(Chocolat.C_HQ_E);
    }
    /**
     * @author Maxence
     */
    public Double getStock_feve(IProduit q){
        return this.stock_feve.get(q);
    }
    /**
     * @author Maxence
     */
    public Double getStock_chocolat(IProduit q){
        return this.stock_chocolat.get(q);
    }
    /** @author Pierre et Maxence
     * 
    **/
    public void add_feve(Double n, Feve q){
        assert n >= 0;
        this.stock_feve.put(q, this.stock_feve.get(q) + n);
        this.getJournaux().get(1).ajouter("Ajout de" + (n).toString()+ "de fève de qualité" + (q).toString() + "\n");
        this.stock_feve_affichage.ajouter(this,n);
    }

    /** @author Pierre et maxence
    **/
    public void remove_feve(Double n, Feve q){
        assert n >= 0;
        if (n <= this.stock_feve.get(q)){
            this.stock_feve.put(q, this.stock_feve.get(q) - n);
            this.getJournaux().get(1).ajouter("Déstockage de" + (n).toString()+ "de fève de qualité" + (q).toString() + "\n");
            this.stock_feve_affichage.retirer(this,n);
        }
    }

    /** @author Raphaël et Maxence
    **/
    public void add_chocolat(Double n, Chocolat q){
        assert n >= 0;
        this.stock_chocolat.put(q, this.stock_chocolat.get(q) + n);
        this.getJournaux().get(2).ajouter("Ajout de" + (n).toString()+ "de chocolat de qualité" + (q).toString() + "\n");
        this.stock_chocolat_affichage.ajouter(this,n);
    }

    /** @author Raphaël et Maxence
    **/
    public void remove_chocolat(Double n, Chocolat q){
        assert n >= 0;
        if (n <= this.stock_chocolat.get(q)){
            this.stock_chocolat.put(q, this.stock_chocolat.get(q) - n); 
            this.getJournaux().get(2).ajouter("Déstockage de" + (n).toString()+ "de chocolat de qualité" + (q).toString() + "\n");
    
        }
            this.stock_chocolat.put(q, this.stock_chocolat.get(q) - n);
            this.stock_chocolat_affichage.retirer(this,n);
            }
    }
