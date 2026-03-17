/**@author Ewan Lefort */

package abstraction.eq4Transformateur1;

import java.util.*;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.IProduit;
import abstraction.eqXRomu.produits.Chocolat;

public class Transformateur1Stock extends Transformateur1Acteur{

    private HashMap<IProduit, Double> stock;

    public Transformateur1Stock(){
        super();
        this.stock=new HashMap<IProduit, Double>();
    }

    public void initialiser(){
        this.stock.put(Feve.F_BQ,0.0);
        this.stock.put(Feve.F_MQ,0.0);
        this.stock.put(Feve.F_HQ,0.0);
        this.stock.put(Feve.F_BQ_E,0.0);
        this.stock.put(Feve.F_MQ_E,0.0);
        this.stock.put(Feve.F_HQ_E,0.0);
        this.stock.put(Chocolat.C_BQ,0.0);
        this.stock.put(Chocolat.C_MQ,0.0);
        this.stock.put(Chocolat.C_HQ,0.0);
        this.stock.put(Chocolat.C_BQ_E,0.0);
        this.stock.put(Chocolat.C_MQ_E,0.0);
        this.stock.put(Chocolat.C_HQ_E,0.0);
    }

    public HashMap<IProduit, Double> getStock(){
        return this.stock;
    }

    public double getStocksProduit(IProduit produit){
        return this.getStock().get(produit);
    }

    public double getTotalStocksFeves(){
        double totalstock=0;
        for (IProduit feve: stock.keySet()){
            if (feve.getType()=="Feve");
            totalstock+=this.getStocksProduit(feve);
        }
        return totalstock;
    }

    public double getTotalStocksChoco(){
        double totalstock=0;
        for (IProduit choco: stock.keySet()){
            if (choco.getType()=="Chocolat");
            totalstock+=this.getStocksProduit(choco);
        }
        return totalstock;
    }

    public double getTotalStocks(){
        return this.getTotalStocksChoco()+this.getTotalStocksFeves();
    }

   
    public void setStocksProduit(IProduit p, double QuantiteEnT){
        if (this.getStock().containsKey(p));
        this.getStock().put(p,QuantiteEnT);
    }
}