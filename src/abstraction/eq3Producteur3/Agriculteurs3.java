package abstraction.eq3Producteur3;

/** @author Vassili Spiridonov*/
public class Agriculteurs3 {
    private int nbCDI;  
    private int nbInterim;
    private int nbEnfant;    
    private double salaireCDI; 
    private double salaireInterim;     
    private double salaireEnfant;   
    private boolean exploitationEnfant; 

    public void Agriculteur3(int nbCDIInitial) {
        this.nbCDI = nbCDIInitial;
        this.nbInterim = 0;
        this.nbEnfant = 0; // Entrerpise éthique : aucun enfants exploités 
        this.salaireCDI = 12.0; // On les rémunères au max décidé dans les règles de fonctionnement (0.8€/jour)
        this.salaireInterim = 20.0; // On paye plus chère les intérimaires
        this.salaireEnfant = 3.0;  // D'après les règles de fonctionnemments : 0.2€/jour
        this.exploitationEnfant = false; // On vérifie le respect de la charte éthique 
    }

    public double getForceDeTravailTotale() {
        return this.nbCDI + (this.nbEnfant * 0.6) + this.nbInterim; /* Les enfants comptent pour 0.6 d'un adulte.
     */
    }

    public double getCoutMainOeuvreTotal() {
        return (this.nbCDI * this.salaireCDI) + (this.nbCDI ) + (this.nbInterim * this.salaireInterim) + (this.nbInterim )
               (this.nbEnfant * this.salaireEnfant);
    }

    //On Embauche des CDI 
    public void EmbaucheCDI(int nb) { 
        this.nbCDI += Math.max(0, nb);
    }

    //On licencie des CDI
    public void licencieCDI(int nb) {
        this.nbCDI -= Math.max(0, nb);
    }

    //Recrutement ponctuel des intérimaires 
    public void setNbInterim(int nb) {
        this.nbInterim = Math.max(0, nb);
    }



    //Cette fonction décrit notre engagement éthique concernant l'exploitation d'enfants
    public String getEthiqueEnfant() {
        if (!this.exploitationEnfant && this.nbEnfant == 0) {
            return "Pas d'enfants exploités";
        }
        return "Exploitation non conforme aux standards éthiques.";
    }



}
