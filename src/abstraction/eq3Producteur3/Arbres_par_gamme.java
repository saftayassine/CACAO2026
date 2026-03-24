package abstraction.eq3Producteur3;

import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.Gamme;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Arbres_par_gamme {
    /** @author Victor Vannier-Moreau*/
    private Feve feve;
    // Liste de 961 éléments (de l'indice 0 à 960)
    private List<Integer> distributionAge; 
    public int nbHectareTotal;

    
    public Arbres_par_gamme(Feve feve) {
        /** @author Victor Vannier-Moreau*/
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
        /** @author Vassili Spiridonov*/
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
    public long getProductionFeve() { 
    /** @author Guillaule Leroy*/
    Map<String, Integer> recap = this.getTranches();
    
    long nbJeunes = recap.get("3-5");   
    long nbAdultes = recap.get("5-25"); 
    long nbVieux = recap.get("25-40");  

    long totalCabosses = 0; 
    totalCabosses = totalCabosses + (nbJeunes * 10);
    totalCabosses = totalCabosses + (nbAdultes * 50);
    totalCabosses = totalCabosses + (nbVieux * 25);

    long coeffGamme; 
    Gamme g = this.feve.getGamme();
    
    if (g == Gamme.BQ) {
        coeffGamme = 50;
    } else if (g == Gamme.MQ) {
        coeffGamme = 40;
    } else {
        coeffGamme = 30; 
    }
    
    // Le L après 1000 force Java à faire le calcul en long
    return totalCabosses * coeffGamme * 1000L; 
}



    /**
     * Fait vieillir la plantation d'une période
     */
    public void ageIncr() {
        /** @author Victor Vannier-Moreau*/
        // 1. On retire les arbres qui ont fini leur 960ème période (40 ans)
        int arbresSortants = distributionAge.remove(960); 
        
        // 2. On les replante immédiatement à l'âge 0 (indice 0)
        // La liste se décale automatiquement vers la droite
        distributionAge.add(0, arbresSortants);
    }
}