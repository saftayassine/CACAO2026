package abstraction.eq1Producteur1;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.Feve;

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

    // Détermine le cycle de vie de la plantation en fonction de son âge
    public int getEtat(){
        int etape = Filiere.LA_FILIERE.getEtape();
        int age = etape - this.etapeCreation;

        if(age<=72){
            this.etat = 0; // En croissance
        }
        else if(age<=120 && age > 72){
            this.etat = 1; // Jeune
        }
        else if(age<= 600 && age > 120){
            this.etat = 2; // Mâture (Plein rendement)
        }
        else if(age <= 960 && age > 600){
            this.etat = 3; // Vieillissant
        }
        else{
            this.etat = 10; // Arbres en fin de vie, bons à abattre
        }

        return this.etat;
    }

    // Calcule et renvoie le poids total de cacao produit par cette plantation (en tonnes)
    public double collecte(){ 
        this.getEtat();
        double cabosse = 0;
        int feves_par_cabosse = 0;

        // Le rendement varie énormément selon l'âge des arbres
        if(this.etat==1){
            cabosse = 10 * 1000 * this.taille;
        } else if(this.etat==2){
            cabosse = 50 * 1000 * this.taille;
        } else if(this.etat==3){
            cabosse = 25 * 1000 * this.taille;
        }

        // Le nombre de fèves par cabosse dépend de la qualité de l'arbre
        if(this.gamme == Feve.F_HQ || this.gamme == Feve.F_HQ_E){
            feves_par_cabosse = 30;
        }
        else if(this.gamme == Feve.F_MQ||this.gamme == Feve.F_MQ_E){
            feves_par_cabosse = 40;
        }
        else if(this.gamme == Feve.F_BQ || this.gamme == Feve.F_BQ_E){
            feves_par_cabosse = 50;
        }

        double cacao = (feves_par_cabosse * cabosse) / 1000000.0;

        return cacao;
    }

}