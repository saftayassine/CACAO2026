package abstraction.eq1Producteur1;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.Feve;

public class Lot {
    private Feve f;
    private int etapeCreation;
    private int etapePeremption;
    private double quantite;

    public Lot(Feve f, int etapeCreation, double quantite){
        this.f = f;
        this.etapeCreation=etapeCreation;
        this.quantite= quantite;

        // La durée de conservation dépend de la qualité de la fève
        if(f == Feve.F_HQ || f == Feve.F_HQ_E){
            this.etapePeremption = etapeCreation + 12;
        }
        if(f == Feve.F_MQ || f == Feve.F_MQ_E){
            this.etapePeremption = etapeCreation + 24 ;
        }
        if(f == Feve.F_BQ || f == Feve.F_BQ_E){
            this.etapePeremption = etapeCreation + 48;
        }
    }

    public double getQuantite(){
        return this.quantite;
    }

    public Feve getGamme(){
        this.setGamme(); // On actualise la qualité au cas où le lot ait vieilli avant de le renvoyer
        return this.f;
    }

    public int getEtapePeremption(){
        return this.etapePeremption;
    }

    public int getEtapeCreation(){
        return this.etapeCreation;
    }

    // L'enchaînement des "else if" est crucial ici : il garantit qu'un lot trop vieux 
    // ne franchit qu'un seul palier de dégradation par tour de jeu.
    public void setGamme(){
        if(Filiere.LA_FILIERE.getEtape()>this.etapePeremption){
            if(f == Feve.F_HQ){
                this.f = Feve.F_MQ;
                this.etapeCreation = this.etapePeremption;
                this.etapePeremption = etapeCreation + 24;
            }
            else if(f == Feve.F_HQ_E){
                this.f = Feve.F_MQ_E;
                this.etapeCreation = this.etapePeremption;
                this.etapePeremption = etapeCreation + 24;
            }
            else if(f == Feve.F_MQ){
                this.f = Feve.F_BQ;
                this.etapeCreation = this.etapePeremption;
                this.etapePeremption = etapeCreation + 48;
            }
            else if(f == Feve.F_MQ_E){
                this.f = Feve.F_BQ_E;
                this.etapeCreation = this.etapePeremption;
                this.etapePeremption = etapeCreation + 48;
            }
            else if(f == Feve.F_BQ || f == Feve.F_BQ_E){
                // Les fèves de basse qualité en fin de vie finissent par pourrir, le lot est perdu.
                this.quantite=0;
            }
        }
    }

    public void setQuantite(double newQuantite){
        this.quantite = newQuantite;
    }

}