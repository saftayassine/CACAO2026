package abstraction.eq1Producteur1;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.Feve;


/**
 * @author Elise Dossal
 */


public class Producteur1Stock extends Producteur1Acteur{

    private List<Lot> lots;
    HashMap<Feve, Double> stock = new HashMap<>();
    private double totalStock=0;

    public Producteur1Stock(){
        super();
        this.lots= new ArrayList<Lot>();
        this.stock.put(Feve.F_BQ,4000.);
        this.stock.put(Feve.F_BQ_E,0.);
        this.stock.put(Feve.F_MQ,2000.);
        this.stock.put(Feve.F_MQ_E,0.);
        this.stock.put(Feve.F_HQ,0.);
        this.stock.put(Feve.F_HQ_E,0.);

        for (double quantite : stock.values()){
            this.totalStock += quantite;
        }
    }

	/////////////////////////////////////////////
	//         Dénombrement des stocks         //
	/////////////////////////////////////////////

    public double getTotalStock(){
        return this.totalStock;
    }

    public double getStock(Feve f){
        return this.stock.get(f);

    }


    public void changeStock(Feve f, double quantite){
        this.stock.put(f, this.stock.get(f) + quantite);
        this.totalStock += quantite;
    }


    public void checkStock(){
        double totalStockTemp = 0;
        HashMap<Feve, Double> stockTemp = new HashMap<Feve, Double>();
        stockTemp.put(Feve.F_BQ,0.);
        stockTemp.put(Feve.F_BQ_E,0.);
        stockTemp.put(Feve.F_MQ,0.);
        stockTemp.put(Feve.F_MQ_E,0.);
        stockTemp.put(Feve.F_HQ,0.);
        stockTemp.put(Feve.F_HQ_E,0.);


        for(int i=0; i<this.lots.size();i++){
            Feve f = this.lots.get(i).getGamme();
            totalStockTemp += this.lots.get(i).getQuantite();

            stockTemp.put(f, stockTemp.get(f) + this.lots.get(i).getQuantite());
        }
        this.totalStock=totalStockTemp;
        this.stock = stockTemp;

    }

	//////////////////////////////////////////
	//         Manipulation de lots         //
	//////////////////////////////////////////

    public void add_lot( Feve f, double quantite ){
        Lot lot = new Lot(f, Filiere.LA_FILIERE.getEtape(), quantite);
        this.lots.add(lot);  // les lots sont trié du plus vieux au plus récent
        this.totalStock = this.totalStock + quantite;
        this.stock.put(f, this.stock.get(f) + quantite);
        this.changeStock(f, quantite);
    }

    public void removeLot(int i){
        this.lots.remove(i);
    }

    public List<Lot> takeFeve(Feve f,double quantite){  // Permet d'extraire des stocks les plus vieilles fêves de la qualité voulue
        if(quantite > this.stock.get(f)){
            return null;

            }


        List<Lot> take_out = new ArrayList<Lot>();
        double rest = quantite;
        for(int i=0; i<this.lots.size() && rest != 0 ;i++){
            Lot lot = this.lots.get(i);
            if(lot.getGamme() == f){
                if(lot.getQuantite() <= rest){ // Si on peut prendre tout le lot, on le prend
                    take_out.add(lot);
                    rest -= lot.getQuantite();
                    this.changeStock(f, -lot.getQuantite());
                    this.removeLot(i);
                }

                else{ //Si on n'a pas besoin de tout le lot, on le sépare en 2
                    Lot new_lot = new Lot(f, lot.getEtapeCreation(), rest);
                    take_out.add(new_lot);
                    lot.setQuantite(lot.getQuantite() - rest );
                    this.changeStock(f, -rest);
                    rest = 0;

                }
            }
        }

        return take_out;
    }
}
