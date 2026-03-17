package abstraction.eq3Producteur3;

import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.Gamme;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/** @author Victor Vannier-Moreau, Guillaume Leroy et Vassili Spiridonov */
public class Arbres_par_gamme {
    private Feve feve;
    // Liste de 961 éléments (de l'indice 0 à 960)
    private List<Integer> distributionAge; 
    private int nbHectareTotal;

    
    public Arbres_par_gamme(Feve feve) {
        this.feve = feve;
        this.distributionAge = new ArrayList<>(961);
        this.nbHectareTotal = 961*350;
        
        int arbresParAge = nbHectareTotal / 961;

        for (int i = 0; i <= 960; i++) {
            this.distributionAge.add(arbresParAge);
        }
    }

        /**
     * Regroupe les données de la liste dans une HashMap pour l'affichage
     */
    public Map<String, Integer> getTranches() {
        Map<String, Integer> recap = new HashMap<>();
        recap.put("0-3", 0);
        recap.put("3-5", 0);
        recap.put("5-25", 0);
        recap.put("25-40", 0);

        for (int age = 0; age < distributionAge.size(); age++) {
            int nb = distributionAge.get(age);
            
            if (age < 72) {
                int ancienNb = recap.get("0-3");
                recap.put("0-3", ancienNb + nb);
            } else if (age < 120) {
                int ancienNb = recap.get("3-5");
                recap.put("3-5", ancienNb + nb);
            } else if (age < 600) {
                int ancienNb = recap.get("5-25");
                recap.put("5-25", ancienNb + nb);
            } else {
                int ancienNb = recap.get("25-40");
                recap.put("25-40", ancienNb + nb);
            }
        }
        return recap;
    }

    /**
     * Calcule la production de fèves selon les paliers d'âge
     */
    public int getProductionFeve() {
        // 1. On récupère la HashMap des tranches
        Map<String, Integer> recap = this.getTranches();
        
        // 2. On récupère le nombre d'arbres pour chaque tranche productive
        int nbJeunes = recap.get("3-5");   // Tranche 3-5 ans
        int nbAdultes = recap.get("5-25"); // Tranche 5-25 ans
        int nbVieux = recap.get("25-40");  // Tranche 25-40 ans

        // 3. Calcul des cabosses selon tes paliers
        int totalCabosses = 0;
        totalCabosses = totalCabosses + (nbJeunes * 10);
        totalCabosses = totalCabosses + (nbAdultes * 50);
        totalCabosses = totalCabosses + (nbVieux * 25);

        // 4. Calcul du coefficient selon la gamme
        int coeffGamme;
        Gamme g = this.feve.getGamme();
        
        if (g == Gamme.BQ) {
            coeffGamme = 50;
        } else if (g == Gamme.MQ) {
            coeffGamme = 40;
        } else {
            coeffGamme = 30; 
        }
        
        return totalCabosses * coeffGamme * 1000;
    }



    /**
     * Fait vieillir la plantation d'une période
     */
    public void ageIncr() {
        // 1. On retire les arbres qui ont fini leur 960ème période (40 ans)
        int arbresSortants = distributionAge.remove(960); 
        
        // 2. On les replante immédiatement à l'âge 0 (indice 0)
        // La liste se décale automatiquement vers la droite
        distributionAge.add(0, arbresSortants);
    }
}