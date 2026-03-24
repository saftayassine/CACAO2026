package abstraction.eq3Producteur3;

import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.Gamme;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class Plantation3 {
    // On stocke un objet Arbres_par_gamme pour chaque type de fève précis
    private Map<Feve, Arbres_par_gamme> stock;
    /** @author Vassili Spiridonov*/

    public Plantation3() {
        /** @author Vassili Spiridonov*/
        this.stock = new HashMap<>();

        // Initialisation pour chaque type de fève disponible
        for (Feve f : List.of(Feve.F_BQ, Feve.F_MQ, Feve.F_HQ)) {
        Arbres_par_gamme arbres = new Arbres_par_gamme(f);
        this.stock.put(f, arbres);
    }
    }

    /**
     * Retourne la production totale de fèves, toutes gammes confondues
     */
    public long getProductionTotale() {
    /** @author Vassili Spiridonov*/
        long total = 0;
        for (Arbres_par_gamme g : stock.values()) {
            total = total + g.getProductionFeve();
        }
        return total;
    }

    /**
     * Retourne la production pour une fève précise
     */
    public long getProductionFève(Feve f) {
        /** @author Vassili Spiridonov*/
        return this.stock.get(f).getProductionFeve();
    }

    /**
     * Retourne le nombre d'hectares total de la plantation
     */
    public int getNbHectareTotal() {
        /** @author Guillaume Leroy*/
        int totalHa = 0;
        // Chaque Arbres_par_gamme a son propre nbHectareTotal 
        for (Arbres_par_gamme g : stock.values()) {
            totalHa = totalHa + g.nbHectareTotal; 
        }
        return totalHa;
    }

    /**
     * Fait avancer le temps d'une période pour tous les arbres
     */
    public void nextStep() {
        /** @author Guillaume Leroy*/
        for (Arbres_par_gamme g : stock.values()) {
            g.ageIncr();
        }
    }

    /**
     * Affiche un récapitulatif complet
     */
    public void afficherRecap() {
        /** @author Victor Vannier-Moreau*/
        System.out.println("=== RÉCAPITULATIF DE LA PLANTATION ===");
        for (Feve f : stock.keySet()) {
            long prod = stock.get(f).getProductionFeve();
            System.out.println("Fève " + f + " | Production : " + prod + " unités");
        }
        System.out.println("---------------------------------------");
        System.out.println("PRODUCTION TOTALE : " + getProductionTotale());
    }
    public static void main(String[] args) {
        Plantation3 maPlantation = new Plantation3();
        maPlantation.afficherRecap();
    }
}