package abstraction.eq3Producteur3;

import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.Gamme;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class Plantation3 {
    // On stocke un objet Arbres_par_gamme pour chaque type de fève précis
    /** @author Vassili Spiridonov*/
    private Map<Feve, Arbres_par_gamme> plantation;
    
    public Plantation3() {
        /** @author Vassili Spiridonov*/
        this.plantation = new HashMap<>();

        // Initialisation pour chaque type de fève disponible
        for (Feve f : List.of(Feve.F_BQ, Feve.F_MQ, Feve.F_HQ)) {
        Arbres_par_gamme arbres = new Arbres_par_gamme(f);
        this.plantation.put(f, arbres);
    }
    }

    /**
     * Retourne la production totale de fèves, toutes gammes confondues
     */
    public double getProductionTotale() {
    /** @author Vassili Spiridonov*/
        double total = 0;
        for (Arbres_par_gamme g : plantation.values()) {
            total = total + g.getProductionFeve();
        }
        return total;
    }

    /**
     * Retourne la production pour une fève précise
     */
    public double getProductionFeve(Feve f) {
        /** @author Vassili Spiridonov*/
        return this.plantation.get(f).getProductionFeve();
    }

    /**
     * Retourne le nombre d'hectares total de la plantation
     */
    public int getNbHectareTotal() {
        /** @author Guillaume Leroy*/
        int totalHa = 0;
        // Chaque Arbres_par_gamme a son propre nbHectareTotal 
        for (Arbres_par_gamme g : plantation.values()) {
            totalHa = totalHa + g.nbHectareTotal; 
        }
        return totalHa;
    }

    /**
     * Retourne la répartition du terrain en pourcentage pour chaque type de fève
     */
    public Map<Feve, Double> getRepartitionTerrain() {
        /** @author Victor Vannier-Moreau*/
        Map<Feve, Double> repartition = new HashMap<>();
        double surfaceTotale = this.getNbHectareTotal();

        // Calculer la part de plantation en hectare de terrain de chaque gamme
        if (surfaceTotale > 0) { // Sécurité pour éviter la division par zéro
            for (Feve f : plantation.keySet()) {
                int surfaceGamme = plantation.get(f).getNbHectare();
                double pourcentage = (surfaceGamme / surfaceTotale) * 100.0;
                repartition.put(f, pourcentage);
            }
        }
        return repartition;
}


    /**
     * Fait avancer le temps d'une période pour tous les arbres
     */
    public void nextStep() {
        /** @author Guillaume Leroy*/
        for (Arbres_par_gamme g : plantation.values()) {
            g.ageIncr();
        }
    }

    /**
     * Affiche un récapitulatif complet
     */
    public void afficherRecap() {
    System.out.println("=== RÉCAPITULATIF DE LA PLANTATION ===");
    Map<Feve, Double> parts = this.getRepartitionTerrain();
    
    for (Feve f : plantation.keySet()) {
        double prod = plantation.get(f).getProductionFeve();
        double partTerrain = parts.get(f);
        
        System.out.printf("Fève %s : %.2f tonnes | Part du terrain : %.1f%%\n", 
                          f, prod, partTerrain);
    }
    System.out.println("---------------------------------------");
    System.out.println("PRODUCTION TOTALE : " + getProductionTotale() + " tonnes ");
    System.out.printf("Surface totale : %d hectares\n", getNbHectareTotal());
    
}

    public static void main(String[] args) {
        Plantation3 maPlantation = new Plantation3();
        maPlantation.afficherRecap();
    }
}