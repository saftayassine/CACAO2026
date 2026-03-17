package abstraction.eq1Producteur1;
import java.util.ArrayList;
import java.util.List;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.Feve;


/**
 * @author Elise Dossal
 */


public class Producteur1Stock extends Producteur1Acteur{

    private List<Lot> lots;
    private double totalStock= 0;
    private double HQ_Stock = 0;
    private double HQ_E_Stock = 0;
    private double MQ_Stock = 0;
    private double MQ_E_Stock = 0;
    private double BQ_Stock = 0;
    private double BQ_E_Stock = 0;

    public Producteur1Stock(){
        super();
        this.lots= new ArrayList<Lot>();
    }

	/////////////////////////////////////////////
	//         Dénombrement des stocks         //
	/////////////////////////////////////////////

    public double getTotalStock(){
        return this.totalStock;
    }

    public double getHQStock(){
        return this.HQ_Stock;
    }

    public double getHQ_EStock(){
        return this.HQ_E_Stock;
    }

    public double getMQStock(){
        return this.MQ_Stock;
    }

    public double getMQ_EStock(){
        return this.MQ_E_Stock;
    }

    public double getBQStock(){
        return this.BQ_Stock;
    }

    public double getBQ_EStock(){
        return this.BQ_E_Stock;
    }

    public void changeStock(Feve f, double quantite){
        if(f == Feve.F_HQ){
            this.HQ_Stock += quantite;

        }

        if(f == Feve.F_HQ_E){
            this.HQ_Stock += quantite;

        }

        if(f == Feve.F_MQ){
            this.MQ_Stock += quantite;
        }

        if(f == Feve.F_MQ_E){
            this.MQ_Stock += quantite;

        }

        if(f == Feve.F_BQ){
            this.BQ_Stock += quantite;

        }

        if(f == Feve.F_BQ_E){
            this.BQ_Stock += quantite;
        }
    }

    public void checkStock(){
        double stock = 0;
        double stock_HQ =0;
        double stock_HQ_E =0;
        double stock_MQ =0;
        double stock_MQ_E =0;
        double stock_BQ =0;
        double stock_BQ_E =0;

        for(int i=0; i<this.lots.size();i++){
            Feve f = this.lots.get(i).getGamme();
            stock += this.lots.get(i).getQuantite();

            if(f == Feve.F_HQ){
                stock_HQ ++;
            }

            if(f == Feve.F_HQ_E){
                stock_HQ_E ++;
            }

            if(f == Feve.F_MQ){
                stock_MQ ++;
            }

            if(f == Feve.F_MQ_E){
                stock_MQ_E ++;
            }

            if(f == Feve.F_BQ){
                stock_BQ ++;
            }

            if(f == Feve.F_BQ_E){
                stock_BQ_E ++;
            }
        }
        this.totalStock=stock;
        this.HQ_Stock = stock_HQ;
        this.HQ_E_Stock = stock_HQ_E ;
        this.MQ_Stock = stock_MQ ;
        this.MQ_E_Stock = stock_MQ_E;
        this.BQ_Stock = stock_BQ ;
        this.BQ_E_Stock = stock_BQ_E;
    }

	//////////////////////////////////////////
	//         Manipulation de lots         //
	//////////////////////////////////////////

    public void add_lot( Feve f, double quantite ){
        Lot lot = new Lot(f, Filiere.LA_FILIERE.getEtape(), quantite);
        this.lots.add(lot);  // les lots sont trié du plus vieux au plus récent
        this.totalStock = this.totalStock + quantite;
        this.changeStock(f, quantite);
    }

    public void removeLot(int i){
        this.lots.remove(i);
    }

    public List<Lot> takeFeve(Feve f,double quantite){  // Permet d'extraire des stocks les plus vieilles fêves de la qualité voulue
        if(f == Feve.F_HQ){
                if(quantite > this.HQ_Stock){
                    return null;
                }
            }

            if(f == Feve.F_HQ_E){
                if(quantite > this.HQ_E_Stock){
                    return null;
                    }
            }

            if(f == Feve.F_MQ){
                if(quantite > this.MQ_Stock){
                    return null;
                    }
            }

            if(f == Feve.F_MQ_E){
                if(quantite > this.MQ_E_Stock){
                    return null;
                    }
            }

            if(f == Feve.F_BQ){
                if(quantite > this.BQ_Stock){
                    return null;
                    }
            }

            if(f == Feve.F_BQ_E){
                if(quantite > this.BQ_E_Stock){
                    return null;
                    }
            }

        List<Lot> take_out = new ArrayList<Lot>();
        double rest = quantite;
        for(int i=0; i<this.lots.size() && rest != 0 ;i++){
            Lot lot = this.lots.get(i);
            if(lot.getGamme() == f){
                if(lot.getQuantite() <= rest){ // Si on peut prendre tout le lot, on le prend
                    take_out.add(lot);
                    rest -= lot.getQuantite();
                }

                else{ //Si on n'a pas besoin de tout le lot, on le sépare en 2
                    Lot new_lot = new Lot(f, lot.getEtapeCreation(), rest);
                    take_out.add(new_lot);
                    lot.setQuantite(lot.getQuantite() - rest );

                }
            }
        }

        return take_out;
    }
}
