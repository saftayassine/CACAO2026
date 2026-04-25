package abstraction.eq3Producteur3;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.Gamme;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Plantation3 {
    // On stocke désormais par Gamme et non plus par Feve précise
    /** @author Vassili Spiridonov*/
    public Map<Gamme, ArbresParGamme> plantation;
    private Journal journal;
    
    public Plantation3(Journal journal) {
        /** @author Vassili Spiridonov*/
        this.plantation = new HashMap<>();

        this.journal = journal;
        // Initialisation pour chaque Gamme (BQ, MQ, HQ)
        for (Gamme g : List.of(Gamme.BQ, Gamme.MQ, Gamme.HQ)) {
            // On passe la Gamme au constructeur de ArbresParGamme
            ArbresParGamme arbres = new ArbresParGamme(g);
            this.plantation.put(g, arbres);
        }
    }


    /**
     * Retourne la production totale de fèves, toutes gammes confondues
     */
    public double getProductionTotale() {
        /** @author Vassili Spiridonov*/
        double total = 0;
        for (ArbresParGamme g : plantation.values()) {
            total = total + g.getProductionTotale();
        }
        return total;
    }

    /**
     * Retourne la production pour une gamme précise
     */
    public double getProductionGamme(Gamme g) {
        /** @author Vassili Spiridonov*/
        return this.plantation.get(g).getProductionTotale();
    }

    public double getProductionFeve(Feve f) {
        /** @author Vassili Spiridonov*/
        Gamme g = f.getGamme();
        if (plantation.containsKey(g)) {
            return plantation.get(g).getProductionTotale();
        }
        return 0.0;
    }

    /**
     * Retourne le nombre d'hectares total de la plantation
     */
    public int getNbHectareTotal() {
        /** @author Guillaume Leroy*/
        int totalHa = 0;
        for (ArbresParGamme g : plantation.values()) {
            totalHa = totalHa + g.nbHectareTotal; 
        }
        return totalHa;
    }

    /**
     * Retourne la répartition du terrain en pourcentage pour chaque gamme de fève
     */
    public Map<Gamme, Double> getRepartitionTerrain() {
        /** @author Victor Vannier-Moreau*/
        Map<Gamme, Double> repartition = new HashMap<>();
        double surfaceTotale = this.getNbHectareTotal();

        // Calculer la part de plantation en hectare de terrain de chaque gamme
        if (surfaceTotale > 0) {
            for (Gamme g : plantation.keySet()) {
                int surfaceGamme = plantation.get(g).getNbHectare();
                double pourcentage = (surfaceGamme / surfaceTotale) * 100.0;
                repartition.put(g, pourcentage);
            }
        }
        return repartition;
}


    /**
     * Fait avancer le temps d'une période pour tous les arbres
     */
    
    public void nextStep() {
        /** @author Guillaume Leroy / Victor Vannier-Moreau */
        int totalAReplanter = 0;

        // 1. Faire vieillir chaque gamme et collecter les hectares morts
        for (ArbresParGamme g : plantation.values()) {
            totalAReplanter += g.ageIncr(); 
        }

        // Stratégie 20/50/30
        int repartitionBQ = (int) (totalAReplanter * 0.20);
        int repartitionHQ = (int) (totalAReplanter * 0.30);
        int repartitionMQ = totalAReplanter - repartitionBQ - repartitionHQ; // Le reste en MQ (50%)

  
        plantation.get(Gamme.BQ).replanter(repartitionBQ);
        plantation.get(Gamme.MQ).replanter(repartitionMQ);
        plantation.get(Gamme.HQ).replanter(repartitionHQ);

        this.actualiserJournal();
    }

    /**
     * Affiche un récapitulatif complet
     */
    public void actualiserJournal() {
        this.journal.ajouter("--- ÉTAPE " + Filiere.LA_FILIERE.getEtape() + " ---");
        Map<Gamme, Double> parts = this.getRepartitionTerrain();
        
        for (Gamme g : plantation.keySet()) {
            double prod = plantation.get(g).getProductionTotale();
            double partTerrain = parts.get(g);
            int hectares = plantation.get(g).getNbHectare();
            
            this.journal.ajouter(String.format("Gamme %s : %.2f tonnes | %d hectares (%.1f%% du terrain)", 
                              g, prod, hectares, partTerrain));
        }
        this.journal.ajouter("Production Totale Plantation : " + String.format("%.2f", getProductionTotale()) + " tonnes");
        this.journal.ajouter("---------------------------------------");
    }
}