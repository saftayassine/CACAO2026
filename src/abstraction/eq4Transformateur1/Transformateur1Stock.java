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
<<<<<<< HEAD
    
    public double getStocksFevesMQ(){
        return stockFeves.get(Feve.F_MQ);
    }

    public double getStocksFevesHQ(){
        return stockFeves.get(Feve.F_HQ);
    }
    
    public double getStocksFevesBQE(){
        return stockFeves.get(Feve.F_BQ_E);
    }
    
    public double getStocksFevesMQE(){
        return stockFeves.get(Feve.F_MQ_E);
    }

    public double getStocksFevesHQE(){
        return stockFeves.get(Feve.F_HQ_E);
    }

    public double getTotalStocksChoco(){
        return this.getStocksChocoBQ()+this.getStocksChocoMQ()+this.getStocksChocoHQ()+this.getStocksChocoBQE()+this.getStocksChocoMQE()+this.getStocksChocoHQE();
    }
    public double getTotalStocksFeves(){
        return this.getStocksFevesBQ()+this.getStocksFevesMQ()+this.getStocksFevesHQ()+this.getStocksFevesBQE()+this.getStocksFevesMQE()+this.getStocksFevesHQE();
=======

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
>>>>>>> a3f7cd93906f660acd7cc4fa6abbcbe1b1e2fbe0
    }

    public double getTotalStocks(){
        return this.getTotalStocksChoco()+this.getTotalStocksFeves();
    }

<<<<<<< HEAD
    public void setStockFevesBQ(double QuantiteEnT){
        stockFeves.put(Feve.F_BQ,QuantiteEnT);
    }

    public void setStockFevesBQE(double QuantiteEnT){
        stockFeves.put(Feve.F_BQ_E,QuantiteEnT);
    }

    public void setStockFevesMQ(double QuantiteEnT){
        stockFeves.put(Feve.F_MQ,QuantiteEnT);
    }

    public void setStockFevesMQE(double QuantiteEnT){
        stockFeves.put(Feve.F_MQ_E,QuantiteEnT);
    }

    public void setStockFevesHQ(double QuantiteEnT){
        stockFeves.put(Feve.F_HQ,QuantiteEnT);
    }

    public void setStockFevesHQE(double QuantiteEnT){
        stockFeves.put(Feve.F_HQ_E,QuantiteEnT);
    }

    public void setStockChocoBQ(double QuantiteEnT){
        stockChoco.put(Chocolat.C_BQ,QuantiteEnT);
    }

    public void setStockChocoBQE(double QuantiteEnT){
        stockChoco.put(Chocolat.C_BQ_E,QuantiteEnT);
    }

    public void setStockChocoMQ(double QuantiteEnT){
        stockChoco.put(Chocolat.C_MQ,QuantiteEnT);
    }

    public void setStockChocoMQE(double QuantiteEnT){
        stockChoco.put(Chocolat.C_MQ_E,QuantiteEnT);
    }

    public void setStockChocoHQ(double QuantiteEnT){
        stockChoco.put(Chocolat.C_HQ,QuantiteEnT);
    }

    public void setStockChocoHQE(double QuantiteEnT){
        stockChoco.put(Chocolat.C_HQ_E,QuantiteEnT);
    }
// Des methodes pour faire un achat en Bourse
    public void setStockFeves(Feve f, double QuantiteEnT){
        stockFeves.put(f,QuantiteEnT);
        }
    public void setStockChoco(Chocolat c, double QuantiteEnT){
        stockChoco.put(c,QuantiteEnT);
        }
    public double getStockFeve(Feve f){
        return stockFeves.get(f);}
    public double getStockChoco(Chocolat c){
        return stockChoco.get(c);
=======
   
    public void setStocksProduit(IProduit p, double QuantiteEnT){
        if (this.getStock().containsKey(p));
        this.getStock().put(p,QuantiteEnT);
>>>>>>> a3f7cd93906f660acd7cc4fa6abbcbe1b1e2fbe0
    }
}