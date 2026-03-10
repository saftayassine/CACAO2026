/**@author Ewan Lefort */

package abstraction.eq4Transformateur1;


public class Transformateur1Stock {

    protected double StocksFevesMQE; // La quantite totale de stock de fèves de moyenne qualité équitable
    protected double StocksFevesHQE; // La quantite totale de stock de fèves de haute qualité équitable
    protected double StocksFevesBQ; // La quantite totale de stock de fèves de basse qualité
    protected double StocksFevesMQ; // La quantite totale de stock de fèves de moyenne qualité
    protected double StocksFevesHQ; // La quantite totale de stock de fèves de haute qualité
    protected double StocksFevesBQE; // La quantite totale de stock de fèves de basse qualité équitable
    protected double StocksChocoBQ; // La quantite totale de stock de chocolat de basse qualité
    protected double StocksChocoMQ; // La quantite totale de stock de chocolat de moyenne qualité
    protected double StocksChocoHQ; // La quantite totale de stock de chocolat de haute qualité
    protected double StocksChocoBQE; // La quantite totale de stock de chocolat de basse qualité équitable
    protected double StocksChocoMQE; // La quantite totale de stock de chocolat de moyenne qualité équitable
    protected double StocksChocoHQE; // La quantite totale de stock de chocolat de haute qualité équitable
    

    public Transformateur1Stock(){
        this.StocksFevesMQE= 0;
        this.StocksFevesHQE= 0;
        this.StocksFevesBQ= 0;
        this.StocksFevesMQ= 0;
        this.StocksFevesHQ= 0;
        this.StocksFevesBQE= 0;
        this.StocksChocoBQ= 0;
        this.StocksChocoMQ= 0;
        this.StocksChocoHQ= 0;
        this.StocksChocoBQE=0;
        this.StocksChocoMQE= 0;
        this.StocksChocoHQE= 0;
    }
    
    public double getStocksFevesBQ(){
        return this.StocksFevesBQ;
    }
    
    public double getStocksFevesMQ(){
        return this.StocksFevesMQ;
    }

    public double getStocksFevesHQ(){
        return this.StocksFevesHQ;
    }
    
    public double getStocksFevesBQE(){
        return this.StocksFevesBQE;
    }
    
    public double getStocksFevesMQE(){
        return this.StocksFevesMQE;
    }

    public double getStocksFevesHQE(){
        return this.StocksFevesHQE;
    }

    public double getStocksChocoBQ(){
        return this.StocksChocoBQ;
    }

    public double getStocksChocoMQ(){
        return this.StocksChocoMQ;
    }

    public double getStocksChocoHQ(){
        return this.StocksChocoHQ;
    }

    public double getStocksChocoBQE(){
        return this.StocksChocoBQE;
    }

    public double getStocksChocoMQE(){
        return this.StocksChocoMQE;
    }

    public double getStocksChocoHQE(){
        return this.StocksChocoHQE;
    }

    public double getTotalStocksFeves(){
        return this.getStocksFevesBQ()+this.getStocksFevesMQ()+this.getStocksFevesHQ()+this.getStocksFevesBQE()+this.getStocksFevesMQE()+this.getStocksFevesHQE();
    }

    public double getTotalStocksChoco(){
        return this.getStocksChocoBQ()+this.getStocksChocoMQ()+this.getStocksChocoHQ()+this.getStocksChocoBQE()+this.getStocksChocoMQE()+this.getStocksChocoHQE();
    }

    public double getTotalStocks(){
        return this.getTotalStocksChoco()+this.getTotalStocksFeves();
    }


}
