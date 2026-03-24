package abstraction.eq1Producteur1;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.Feve;

/**
 * @author Elise Dossal
 */

public class Lot {
    private Feve f;
    private int etapeCreation;
    private int etapePeremption;
    private double quantite;


    public Lot(Feve f, int etapeCreation, double quantite){
        this.f = f;
        this.etapeCreation=etapeCreation;
        this.quantite= quantite;

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
        this.setGamme();
        return this.f;
    }

    public int getEtapePeremption(){
        return this.etapePeremption;
    }

    public int getEtapeCreation(){
        return this.etapeCreation;
    }

    public void setGamme(){
        if(Filiere.LA_FILIERE.getEtape()>this.etapePeremption){
            if(f == Feve.F_HQ){
                this.f = Feve.F_MQ;
                this.etapeCreation = this.etapePeremption;
                this.etapePeremption = etapeCreation + 24;

            }

            if(f == Feve.F_HQ_E){
                this.f = Feve.F_MQ_E;
                this.etapeCreation = this.etapePeremption;
                this.etapePeremption = etapeCreation + 24;

            }

            if(f == Feve.F_MQ){
                this.f = Feve.F_BQ;
                this.etapeCreation = this.etapePeremption;
                this.etapePeremption = etapeCreation + 48;

            }

            if(f == Feve.F_MQ_E){
                this.f = Feve.F_BQ_E;
                this.etapeCreation = this.etapePeremption;
                this.etapePeremption = etapeCreation + 48;

            }

            if(f == Feve.F_BQ || f == Feve.F_BQ_E){
                this.quantite=0;

            }

        }

    }

    public void setQuantite(double newQuantite){
        this.quantite = newQuantite;
    }

}
