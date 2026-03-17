package abstraction.eq1Producteur1;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.Feve;

/**
 * @author Elise Dossal
 */

public class Plantation {
    private Feve gamme;
    private double taille;
    private int etapeCreation;
    private int etat;

    public Plantation(Feve gamme, double taille,int etapeCreation){
        this.gamme= gamme;
        this.taille = taille;
        this.etapeCreation =etapeCreation;
        this.etat = 0;

    }

    public Feve getGamme(){
        return this.gamme;
    }

    public int getEtapeCreation(){
        return this.etapeCreation;
    }
    
    public double getTaille(){
        return this.taille;
    }

    public int getEtat(){
        int etape = Filiere.LA_FILIERE.getEtape();
        int age = etape - this.etapeCreation;

        if(age<=72){
            this.etat = 0;
        }

        if(age<=120 && age > 72){
            this.etat = 1;
        }

        if(age<= 600 && age > 120){
            this.etat = 2;
        }

        if(age<= 960 && age > 960){
            this.etat = 3;
        }

        else{
            this.etat = 10;
        }

        return this.etat;
    }


    public double collecte(){ // retourne le poid en tonne de cacao
        this.getEtat();
        double cabosse = 0;
        int feves_par_cabosse = 0;

        if(this.etat==1){
            cabosse = 500000*this.taille;
        }

        if(this.etat==2){
            cabosse = 2500000*this.taille;
        }

        if(this.etat==3){
            cabosse = 1250000*this.taille;
        }

        else{
            return 0;
        }

        

        if(this.gamme == Feve.F_HQ || this.gamme == Feve.F_HQ_E){
            feves_par_cabosse = 30;
        }


        if(this.gamme == Feve.F_MQ||this.gamme == Feve.F_MQ_E){
            feves_par_cabosse = 40;
        }



        if(this.gamme == Feve.F_BQ || this.gamme == Feve.F_BQ_E){
            feves_par_cabosse = 50;
        }

        double cacao = ( feves_par_cabosse * cabosse * 2 )/1000000;

        return cacao;

    }

}
