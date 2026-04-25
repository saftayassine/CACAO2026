package abstraction.eq2Producteur2;
/** @author Paul */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.produits.Feve;


public class Sechage extends Récolte {
    private HashMap<Feve, List<Double>> fileSechage;
    private HashMap<Feve, List<Integer>> fileSechageSteps;
    private Journal journalSechage;

    public Sechage() {
        super();
        this.fileSechage = new HashMap<Feve, List<Double>>();
        this.fileSechageSteps = new HashMap<Feve, List<Integer>>();
        this.journalSechage = new Journal("Journal Sechage Eq2",this);
        if (this.fevesSeches == null) {
            this.fevesSeches = new HashMap<Feve, Double>();
        }
        
        for (Feve f : Feve.values()) {
            this.fevesSeches.put(f, 0.0);
            this.fileSechage.put(f, new ArrayList<>());
            this.fileSechageSteps.put(f, new ArrayList<>());
        }
    }

    /**
     * Ajoute les fèves récoltées à la file de séchage avec leur step de fin.
     */
    public void ajouterAuSechage() {
        int stepActuel = Filiere.LA_FILIERE.getEtape();

        for (Feve f : Feve.values()) {
            double quantite = feve_recolte.get(f);
            if (quantite > 0 ) {
                int stepFinSechage = stepActuel + 2;
                fileSechage.get(f).add(quantite);
                fileSechageSteps.get(f).add(stepFinSechage);
                journalSechage.ajouter("Step " + stepActuel + " : Ajout au séchage de " + quantite + " t de " + f + " (fin prévue au step " + stepFinSechage + ")");
            
            }
        }
    }
    
    public void mettreAJourSechage() {
        int stepActuel = Filiere.LA_FILIERE.getEtape();
        HashMap<Feve, Double> sechageFini = new HashMap<>();

        for (Feve f : Feve.values()) {
            sechageFini.put(f, 0.0);

            List<Double> lots = fileSechage.get(f);
            List<Integer> stepsFin = fileSechageSteps.get(f);
            List<Double> nouveauxLots = new ArrayList<>();
            List<Integer> nouveauxSteps = new ArrayList<>();

            for (int i = 0; i < lots.size(); i++) {
                if (stepsFin.get(i) <= stepActuel) {
                    sechageFini.put(f, sechageFini.get(f) + lots.get(i));
                } else {
                    nouveauxLots.add(lots.get(i));
                    nouveauxSteps.add(stepsFin.get(i));
                }
            }

            // Mise à jour des listes après suppression des lots séchés
            fileSechage.put(f, nouveauxLots);
            fileSechageSteps.put(f, nouveauxSteps);

            // Mise à jour des fèves sèches et passage en tonnes
            switch (f) {
                case F_BQ:
                    fevesSeches.put(f, sechageFini.get(f));
                    break;
                case F_BQ_E:
                    fevesSeches.put(f, sechageFini.get(f));
                    break;
                case F_MQ:
                    fevesSeches.put(f, sechageFini.get(f));
                    break;
                case F_MQ_E:
                    fevesSeches.put(f, sechageFini.get(f));
                    break;
                case F_HQ:
                    fevesSeches.put(f, sechageFini.get(f));
                    break;
                case F_HQ_E:
                    fevesSeches.put(f, sechageFini.get(f));
                    break;
                default:
                    break;
            }
        }

        journalSechage.ajouter("Step " + stepActuel + " : Nouvelles tonnes de fèves sèches : " + fevesSeches);

    }
    
    public double getFevesSeches(Feve f) {
        return fevesSeches.get(f);
    }

    public void next() {
        super.next();
        ajouterAuSechage();
        mettreAJourSechage();
        int stepActuel = Filiere.LA_FILIERE.getEtape();
        for (Feve f : Feve.values()) {
            double quantiteSechee = fevesSeches.getOrDefault(f, 0.0);
            if (quantiteSechee > 0.0) {
                addStock(f, stepActuel, quantiteSechee);
            }
        }
        setTotalStock();
    }

    public List<Journal> getJournaux() {
        List<Journal> res = super.getJournaux();
        res.add(journalSechage);
        return res;
    }

}
