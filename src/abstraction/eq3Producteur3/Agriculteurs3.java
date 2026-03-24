package abstraction.eq3Producteur3;

/** @author Vassili Spiridonov*/
public class Agriculteurs3 {
    private int nbEmployesAdulte;  
    private int nbEmployesEnfant;    
    private double salaireAdulte;    
    private double salaireEnfant;   
    private boolean exploitationEnfant; 

    public void Agriculteur3(int nbAdulte, int nbEnfant) {
        this.nbEmployesAdulte = nbAdulte;
        this.nbEmployesEnfant = 0; // Entrerpise éthique : aucun enfants exploités 
        this.salaireAdulte = 12.0; // On les rémunères au max décidé dans les règles de fonctionnement (0.8€/jour)
        this.salaireEnfant = 3.0;  // D'après les règles de fonctionnemments : 0.2€/jour
        this.exploitationEnfant = false; // On vérifie le respect de la charte éthique 
    }

    public double getForceDeTravailTotale() {
        return this.nbEmployesAdulte + (this.nbEmployesEnfant * 0.6); /* Les enfants comptent pour 0.6 d'un adulte.
     */
    }

    public double getCoutMainOeuvreTotal() {
        return (this.nbEmployesAdulte * this.salaireAdulte) + 
               (this.nbEmployesEnfant * this.salaireEnfant);
    }

    //Cette fonction décrit notre engagement éthique concernant l'exploitation d'enfant 
    public String getEthiqueEnfant() {
        if (!this.exploitationEnfant && this.nbEmployesEnfant == 0) {
            return "Pas d'enfants exploités";
        }
        return "Exploitation non conforme aux standards éthiques.";
    }


}
