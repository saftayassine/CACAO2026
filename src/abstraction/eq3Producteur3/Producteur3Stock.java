package abstraction.eq3Producteur3;

import java.util.HashMap;
import abstraction.eqXRomu.produits.Feve;

/** @author Guillaume Leroy */
public class Producteur3Stock {
    private HashMap<Feve, Double> stock;
    public Producteur3Stock(){
        this.stock = new HashMap<Feve, Double>();
        for (Feve f : Feve.values()) {
    		this.stock.put(f, 0.0);
        }
    }

    public double getStock(Feve f) {
        return this.stock.get(f);
    }

    public double getStockTotal(){
        double stock_total = 0;
        for (Feve f : Feve.values()){
            stock_total = stock_total + this.stock.get(f);
        }
        return stock_total;
    }

    public void addStock(Feve f, Double quantite){
        this.stock.put(f, this.stock.get(f) + quantite);
    }

    public void retireStock(Feve f , Double quantite){
        double c = this.stock.get(f) - quantite;
        if (c>=0){
            this.stock.put(f , c);
        }
    }
}