package abstraction.eq1Producteur1;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import abstraction.eqXRomu.filiere.Banque;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.produits.Feve;

public class Producteur1Stock extends Producteur1Acteur{

    private List<Lot> lots;
    HashMap<Feve, Double> stock = new HashMap<>();
    protected double totalStock=0;
    protected Journal journalStock;

    public Producteur1Stock(){
        super();
        this.lots= new ArrayList<Lot>();
        this.stock.put(Feve.F_BQ,0.);
        this.stock.put(Feve.F_BQ_E,0.);
        this.stock.put(Feve.F_MQ,0.);
        this.stock.put(Feve.F_MQ_E,0.);
        this.stock.put(Feve.F_HQ,0.);
        this.stock.put(Feve.F_HQ_E,0.);

        this.add_lot(Feve.F_BQ, 8500,0);
        this.add_lot(Feve.F_MQ, 1500,0);

        this.stockTot.setValeur(this, totalStock, this.cryptogramme);
        this.journalStock = new Journal("Journal "+this.getNom()+ " Stock", this);
    }

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

    // Effectue un inventaire complet : utile pour synchroniser nos registres (une fois par an par exemple).
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

    public void add_lot( Feve f, double quantite ){
        this.add_lot(f, quantite, Filiere.LA_FILIERE.getEtape());
    }

    // En ajoutant les lots à la fin de la liste, ils restent naturellement classés du plus ancien au plus récent.
    public void add_lot( Feve f, double quantite, int etape){
        Lot lot = new Lot(f, etape, quantite);
        this.lots.add(lot);  
        this.changeStock(f, quantite);
    }

    public void removeLot(int i){
        this.lots.remove(i);
    }

    // Récupère la quantité demandée en piochant dans nos lots, en commençant par les plus vieux (FIFO).
    public List<Lot> takeFeve(Feve f,double quantite){
        if(quantite > this.stock.get(f)){
            return null; // On annule si on nous demande plus que ce qu'on possède.
        }

        List<Lot> take_out = new ArrayList<Lot>();
        double rest = quantite;
        for(int i=0; i<this.lots.size() && rest != 0 ;i++){
            Lot lot = this.lots.get(i);
            if(lot.getGamme().equals(f)){
                if(lot.getQuantite() <= rest){ 
                    take_out.add(lot);
                    rest -= lot.getQuantite();
                    this.changeStock(f, -lot.getQuantite());
                    this.removeLot(i);
                    i--; // On recule l'index après avoir supprimé un élément pour ne rien rater.
                }
                else{ 
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
   
    public void loyer(){
        this.journalBanque.ajouter("total stock : " + this.totalStock);
        double montant = Filiere.LA_FILIERE.getParametre("cout moyen stockage producteur").getValeur() * this.totalStock;
        Banque banque=Filiere.LA_FILIERE.getBanque();
        banque.payerCout(this, this.cryptogramme, "Loyer Stockage" , montant);
        this.journalBanque.ajouter("Loyer Stockage : " + montant);
    }
    
    public List<Journal> getJournaux() {
		List<Journal> res=super.getJournaux();
		res.add(this.journalStock);
		return res;
	}

    public void next() {
        super.next();

        // Mise à jour de notre interface avec les chiffres finaux du tour.
        this.stockTot.setValeur(this, this.totalStock, this.cryptogramme);
        
        // On garde une trace de chaque qualité dans le journal pour faciliter le suivi.
		this.journalStock.ajouter( "Stock fève BQ :"+String.valueOf(this.stock.get(Feve.F_BQ)));
        this.journalStock.ajouter( "Stock fève BQ_E :"+String.valueOf(this.stock.get(Feve.F_BQ_E)));
        this.journalStock.ajouter( "Stock fève MQ :"+String.valueOf(this.stock.get(Feve.F_MQ)));
        this.journalStock.ajouter( "Stock fève MQ_E :"+String.valueOf(this.stock.get(Feve.F_MQ_E)));
        this.journalStock.ajouter( "Stock fève HQ :"+String.valueOf(this.stock.get(Feve.F_HQ)));
        this.journalStock.ajouter( "Stock fève HQ_E :"+String.valueOf(this.stock.get(Feve.F_HQ_E)));

        this.loyer();
        
        // Un inventaire complet est effectué au début de chaque année.
        if(Filiere.LA_FILIERE.getEtape()%24==1){this.checkStock();}
    }

}