package abstraction.eq1Producteur1;
import java.util.ArrayList;
import java.util.List;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.Feve;



public class Producteur1Planteur extends Producteur1Stock{

    private List<Plantation> plantations = new ArrayList<Plantation>();
    private double taille_totale=10000;

    /**
     * @author Théophile Trillat
     */
    public Producteur1Planteur(){
        super();
        Plantation BQ = new Plantation(Feve.F_BQ, 850000 , -600);
        Plantation MQ = new Plantation(Feve.F_MQ, 150000 , -600);
        this.plantations.add(BQ);
        this.plantations.add(MQ);
        }

    /**
     * @author Elise Dossal
     */
    public double getTaille(){
        return this. taille_totale;
    }

    /**
     * @author Théophile Trillat
     */
    public void planter(Feve f, double taille){
        Plantation newP = new Plantation(f, taille , Filiere.LA_FILIERE.getEtape());
        this.plantations.add(newP);
    }

    /**
     * @author Elise Dossal
     */
    public void couper(int i){
        this.plantations.remove(i);
    }

    /**
     * @author Elise Dossal
     */
    public void collecter(){ //On crée un lot de chauqe qualité qui regroupe plusieurs plantations pour ne pas avoir des lots qui ne différent que par la quantité
        double lot_HQ = 0;
        double lot_HQ_E = 0;
        double lot_MQ = 0;
        double lot_MQ_E = 0;
        double lot_BQ = 0;
        double lot_BQ_E = 0;

        for(int i=0; i<this.plantations.size(); i++){
            Plantation plantation = this.plantations.get(i);
            if(plantation.getEtat() == 10){ //vérifie si les arbres ne sont pas morts, sinon les coupe
                this.couper(i);
            }
            double cacao = plantation.collecte();
            Feve gamme = plantation.getGamme();

            if(gamme == Feve.F_HQ){
                lot_HQ += cacao;
            }

            if(gamme == Feve.F_HQ_E){
                lot_HQ_E += cacao;
            }

            if(gamme == Feve.F_MQ){
                lot_MQ += cacao;
            }

            if(gamme == Feve.F_MQ_E){
                lot_MQ_E += cacao;
            }

            if(gamme == Feve.F_BQ){
                lot_BQ += cacao;
            }

            if(gamme == Feve.F_BQ_E){
                lot_BQ_E += cacao;
            }
        }



        this.add_lot(Feve.F_HQ, lot_HQ);
        this.add_lot(Feve.F_HQ_E, lot_HQ_E);
        this.add_lot(Feve.F_MQ, lot_MQ);
        this.add_lot(Feve.F_MQ_E, lot_MQ_E);
        this.add_lot(Feve.F_BQ, lot_BQ);
        this.add_lot(Feve.F_BQ_E, lot_BQ_E);

    }

    
    /**
     * @author Elise Dossal
     */
    public void next(){
        super.next();
        int etape = Filiere.LA_FILIERE.getEtape();
        if(etape%24 == 0){ //Une collecte tous les ans, a une dâte arbitraire pour l'instant
            this.collecter();
        }
    }

}
