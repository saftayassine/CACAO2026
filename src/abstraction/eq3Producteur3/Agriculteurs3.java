package abstraction.eq3Producteur3;

import java.util.HashMap;

import abstraction.eqXRomu.produits.Gamme;

/** @author Vassili Spiridonov*/
public class Agriculteurs3 {

    private HashMap<Gamme, Integer> nbCDI;
    private HashMap<Gamme, Integer> nbInterim;

    private int nbEnfant;    
    private double salaireCDI; 
    private double salaireInterim;     
    private double salaireEnfant;   


    
    public Agriculteurs3(Plantation3 plantation) {
        this.nbCDI = new HashMap<Gamme, Integer>();
        this.nbInterim = new HashMap<Gamme, Integer>();
        this.nbEnfant = 0; // Entrerpise éthique : aucun enfants exploités 
        this.salaireCDI = 12.0; // On les rémunères au max décidé dans les règles de fonctionnement (0.8€/jour)
        this.salaireInterim = 2*this.salaireCDI; // On paye deux fois plus chère les intérimaires
        this.salaireEnfant = 3.0;  // D'après les règles de fonctionnemments : 0.2€/jour 
        this.repartirTravailleurs(plantation);
    }

    public void repartirTravailleurs(Plantation3 plantation) {
        for (Gamme g : Gamme.values()) {
            double surfaceGamme = plantation.plantation.get(g).getNbHectare(); 

            int ratio;
            if (g == Gamme.HQ) {
                ratio = 7;
            } else if (g == Gamme.MQ) {
                ratio = 5;
            } else {
                ratio = 3; 
            }
            
            int besoinTotalGamme = (int) (surfaceGamme * ratio);

            this.nbCDI.put(g, besoinTotalGamme);
            this.nbInterim.put(g, 0);
        }
    }

    public double getForceDeTravailTotale() {
        double totalAdulte = 0;
        for (Gamme g : Gamme.values()) {
            totalAdulte += nbCDI.getOrDefault(g, 0) + nbInterim.getOrDefault(g, 0);
        }
        return totalAdulte + (this.nbEnfant * 0.5); // Enfant compte pour 0.5
    }

    public double getCoutMainOeuvreTotal() {
        double cout = 0;
        for (Gamme g : Gamme.values()) {
            cout += (nbCDI.getOrDefault(g, 0) * salaireCDI);
            cout += (nbInterim.getOrDefault(g, 0) * salaireInterim);
        }
        cout += (nbEnfant * salaireEnfant);
        return cout;
    }
    

    


    //Cette fonction décrit notre engagement éthique 
    
    public boolean estEthique(Gamme g) {
        boolean PasExploitationEnfant = this.nbEnfant == 0;
        boolean SalaireMinimum = this.salaireCDI >= 12; //Salaire de 0.8€/jour 
        double totalAdultes = this.nbCDI.getOrDefault(g, 0) + this.nbInterim.getOrDefault(g, 0);
        boolean ContratLongTerme = false;
        if (totalAdultes > 0) {
            ContratLongTerme = (this.nbCDI.getOrDefault(g, 0) / totalAdultes) >= 0.8;
        }

        return PasExploitationEnfant && SalaireMinimum && ContratLongTerme;
    }

    //Verification de notre éligibilité 
    public String getStatutHappyWorker() {
        if (this.estEthique(null)) {
            return "Eligible au label Happy Worker";
        }
        return "Non éligible au label Happy Worker.";
    }
 

   
}