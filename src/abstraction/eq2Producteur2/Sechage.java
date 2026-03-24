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
        
        for (Feve f : Feve.values()) {
            fevesSeches.put(f, 0.0);
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
                journalSechage.ajouter("Step " + stepActuel + " : Ajout au séchage de " + quantite + " fèves " + f + " (fin prévue au step " + stepFinSechage + ")");
            
            }
        }
    }
}
