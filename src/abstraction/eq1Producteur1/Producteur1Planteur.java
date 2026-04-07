package abstraction.eq1Producteur1;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import abstraction.eqXRomu.filiere.Banque;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.Feve;



public class Producteur1Planteur extends Producteur1Stock{

    private List<Plantation> plantations = new ArrayList<Plantation>();
    private double taille_totale=10000;
    private double tailleEq;
    private double tailleNonEq;

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
    public double getTaillePlantation(){
        return this. taille_totale;
    }

    public double getTaillePlantation(Boolean eq){
        if(eq){
            return this.tailleEq;
        }

        else{
            return this.tailleNonEq;
        }
    }

    /**
     * @author Théophile Trillat
     */
    public void planter(Feve f, double taille){
        Plantation newP = new Plantation(f, taille , Filiere.LA_FILIERE.getEtape());
        this.plantations.add(newP);
        if(f.isEquitable()){
            this.tailleEq += taille;
        }

        else{
            this.tailleNonEq += taille;
        }

        // BQ 1800/h    MQ 3500/h    HQ 7000/h
    }

    /**
     * @author Elise Dossal
     */
    public void couper(int i){
        double taille = this.plantations.get(i).getTaille();
        Feve f = this.plantations.get(i).getGamme();
        this.plantations.remove(i);
        if(f.isEquitable()){
            this.tailleEq -= taille;
        }

        else{
            this.tailleNonEq -= taille;
        }

        this.taille_totale -= taille;
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

    public void impots(){
        double montant = 250*this.taille_totale;
        Banque banque=Filiere.LA_FILIERE.getBanque();
        banque.payerCout(this, this.cryptogramme, "Impot plantation" ,montant);
        this.journal.ajouter("Impot plantation : " + montant);
    }

    public void charge(){
        Banque banque=Filiere.LA_FILIERE.getBanque();
        banque.payerCout(this, this.cryptogramme, "Masse salariale" , 617.65);
        this.journal.ajouter("Charges payées : " + 617.65);
    }


    /**
     * @author Elise Dossal
     */
    public void next(){
        super.next();
        int etape = Filiere.LA_FILIERE.getEtape();
        this.impots();
        if(etape%24 == 0){ //Une collecte tous les ans, a une dâte arbitraire pour l'instant
            this.collecter();
            this.charge();
        }
    }

}
