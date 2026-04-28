package abstraction.eq3Producteur3;

import java.util.HashMap;

import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.Gamme;

/** @author Vassili Spiridonov*/
public class Agriculteurs3 {

    private HashMap<Gamme, Integer> nbCDI;
    private HashMap<Gamme, Integer> nbInterim;

    private int nbEnfant;
    private HashMap<Gamme, Double> pourcentagesEquitables;
    private double salaireCDIMin;         // Salaire minimum (0.5€/jour soit 7.5€ par step de 15 jours) 
    private double salaireCDIMax; 
    private double salaireInterim;     
    private double salaireEnfant;  

    
    public Agriculteurs3(Plantation3 plantation) {
        this.nbCDI = new HashMap<Gamme, Integer>();
        this.nbInterim = new HashMap<Gamme, Integer>();
        this.nbEnfant = 0; // Entrerpise éthique : aucun enfants exploités 
        this.salaireCDIMin = 7.5;
        this.salaireCDIMax = 12.0;

        // Initialisation des pourcentages par gamme
        this.pourcentagesEquitables = plantation.getPourcentageEquitable();
        this.repartirTravailleurs(plantation);
        this.salaireInterim = 2*this.salaireCDIMax; // On paye deux fois plus chère les intérimaires
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
            int cdiGamme = nbCDI.getOrDefault(g, 0);
            int interimGamme = nbInterim.getOrDefault(g, 0);
            
            // On récupère le pourcentage spécifique à la gamme (0.0 par défaut si absent)
            double pourcentageMax = this.pourcentagesEquitables.getOrDefault(g, 0.0);

            // Calcul du nombre de travailleurs au salaire Max vs Min
            int nbHautSalaire = (int) (cdiGamme * pourcentageMax);
            int nbBasSalaire = cdiGamme - nbHautSalaire;

            // Somme des salaires CDI pour cette gamme
            cout += (nbHautSalaire * this.salaireCDIMax);
            cout += (nbBasSalaire * this.salaireCDIMin);
            
            // Ajout des intérimaires 
            cout += (interimGamme * this.salaireInterim);
        }
        // Ajout des enfants 
        cout += (this.nbEnfant * this.salaireEnfant);
        
        return cout;
    }

    public double getCoutMainOeuvreFeve(Feve f){
        double cout = 0;
        Gamme g = f.getGamme();

        int cdiGamme = nbCDI.getOrDefault(g, 0);
        int interimGamme = nbInterim.getOrDefault(g, 0);
        double pourcentageMax = this.pourcentagesEquitables.getOrDefault(g, 0.0);


        if (f.isEquitable()){
            int nbHautSalaire = (int) (cdiGamme * pourcentageMax);
            cout += (nbHautSalaire * this.salaireCDIMax);
        }
        else{
            int nbBasSalaire = (int) (cdiGamme * (1-pourcentageMax));
            cout += (nbBasSalaire * this.salaireCDIMin);
            cout += (interimGamme * this.salaireInterim);
            cout += (this.nbEnfant * this.salaireEnfant);
        }
        return cout;
    }   
    

    //Cette fonction décrit notre engagement éthique 
    
    public boolean estEthique(Gamme g, double pourcentage) {
        if (g == null) return false;

        //Aucun enfant sur TOUTE la plantation
        boolean pasExploitationEnfant = (this.nbEnfant == 0);

        //100% de CDI pour la gamme demandée
        int interimGamme = this.nbInterim.getOrDefault(g, 0);
        boolean queDesCDI = (interimGamme == 0);

        double pourcentageBienPayes = this.pourcentagesEquitables.getOrDefault(g, 0.0);
        boolean salaireMaxRespecte = (pourcentageBienPayes >= pourcentage);

        return pasExploitationEnfant && queDesCDI && salaireMaxRespecte;
    }
    

    //Verification de notre éligibilité 
    public Boolean getStatutHappyWorker(Gamme g) {
        double pourcentage = 0.1; // au moins 10% de la production de la gamme g vérifie les conditions de travail pour la certification
        if (this.estEthique(g,pourcentage)) {
            return true;
        }
        return false;
    }

}