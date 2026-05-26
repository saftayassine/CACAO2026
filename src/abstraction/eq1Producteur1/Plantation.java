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

        else if(age<=120 && age > 72){
            this.etat = 1;
        }

        else if(age<= 600 && age > 120){
            this.etat = 2;
        }

        else if(age <= 960 && age > 600){
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
            cabosse = 10 * 1000 * this.taille;   // jeune : 10 cabosses/arbre
        } else if(this.etat==2){
            cabosse = 30 * 1000 * this.taille;   // plein rendement : 30 cabosses/arbre
        } else if(this.etat==3){
            cabosse = 15 * 1000 * this.taille;   // vieillissant : 15 cabosses/arbre
        }

    
        if(this.gamme == Feve.F_HQ || this.gamme == Feve.F_HQ_E){
            feves_par_cabosse = 30;
        }


        else if(this.gamme == Feve.F_MQ||this.gamme == Feve.F_MQ_E){
            feves_par_cabosse = 40;
        }



        else if(this.gamme == Feve.F_BQ || this.gamme == Feve.F_BQ_E){
            feves_par_cabosse = 50;
        }

        double cacao = ( feves_par_cabosse * cabosse * 2 )/1000000;

        return cacao;

    }

}
