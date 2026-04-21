package abstraction.eq1Producteur1;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import abstraction.eqXRomu.filiere.Banque;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.Feve;


/**
 * @author Elise Dossal
 */


public class Producteur1Stock extends Producteur1Acteur{

    private List<Lot> lots;
    HashMap<Feve, Double> stock = new HashMap<>();
    protected double totalStock=0;

    public Producteur1Stock(){
        super();
        this.lots= new ArrayList<Lot>();
        this.stock.put(Feve.F_BQ,0.);
        this.stock.put(Feve.F_BQ_E,0.);
        this.stock.put(Feve.F_MQ,0.);
        this.stock.put(Feve.F_MQ_E,0.);
        this.stock.put(Feve.F_HQ,0.);
        this.stock.put(Feve.F_HQ_E,0.);

        this.add_lot(Feve.F_BQ, 4000,0);
        this.add_lot(Feve.F_MQ, 6000,0);


        this.stockTot.setValeur(this, totalStock, this.cryptogramme);
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
        this.add_lot(f, quantite, Filiere.LA_FILIERE.getEtape());
    }

    public void add_lot( Feve f, double quantite, int etape){
        Lot lot = new Lot(f, etape, quantite);
        this.lots.add(lot);  // les lots sont trié du plus vieux au plus récent
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
            if(lot.getGamme().equals(f)){
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

        this.stockTot.setValeur(this, this.totalStock, this.cryptogramme);


        return take_out;
    }

    ///////////////////////////////////
	//         Actions autres        //
	///////////////////////////////////
    
    public void loyer(){
        double montant = 180 * this.totalStock;
        Banque banque=Filiere.LA_FILIERE.getBanque();
        banque.payerCout(this, this.cryptogramme, "Loyer Stockage" , montant);
        this.journal.ajouter("Loyer Stockage : " + montant);
    }
    
    public void next() {
        super.next();

		// donne le stock à la fin de la période après tous les échanges
        // mettre à jour stockTot
        this.stockTot.setValeur(this, this.totalStock, this.cryptogramme);
        // Permet de suivre le stock de fève
		this.journal.ajouter( "Stock fève BQ :"+String.valueOf(this.stock.get(Feve.F_BQ)));
        this.journal.ajouter( "Stock fève BQ_E :"+String.valueOf(this.stock.get(Feve.F_BQ_E)));
        this.journal.ajouter( "Stock fève MQ :"+String.valueOf(this.stock.get(Feve.F_MQ)));
        this.journal.ajouter( "Stock fève MQ_E :"+String.valueOf(this.stock.get(Feve.F_MQ_E)));
        this.journal.ajouter( "Stock fève HQ :"+String.valueOf(this.stock.get(Feve.F_HQ)));
        this.journal.ajouter( "Stock fève HQ_E :"+String.valueOf(this.stock.get(Feve.F_HQ_E)));


        // Loyer
        int etape = Filiere.LA_FILIERE.getEtape();
        if(etape%24 == 0){ //Une collecte tous les ans, a une dâte arbitraire pour l'instant
            this.loyer();
        }

    }

}
