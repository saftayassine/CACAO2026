package abstraction.eq2Producteur2;

import java.util.HashMap;

import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.filiere.Filiere;

/** @author Thomas */
public class Producteur2couts extends Producteur2Stock {
    protected HashMap<Feve, Double> cout_unit_t;
    protected Journal JournalCout;

    public Producteur2couts() {
        super();
        this.cout_unit_t = new HashMap<Feve, Double>();
        this.JournalCout = new Journal("Journal Coûts Eq2", null);

        for (Feve f : Feve.values()) {
            this.cout_unit_t.put(f, 0.0);
        }
    }


    public void next() {
        calcul_cout_unit();
        super.next();
    }

    /** Calcul des couts unitaires de production de chaque tonne de feve */
    public void calcul_cout_unit() {
        int etape = Filiere.LA_FILIERE.getEtape();

        for (Feve f : Feve.values()) {
            double prod = stock.get(f).getOrDefault(etape, 0.0);
            if (prod > 0) {
                double cout = cout_stockage * prod;
                cout_unit_t.put(f, cout / prod);
                JournalCout.ajouter("Step " + etape + " : Coût unitaire de production pour " + f + " = " + cout_unit_t.get(f) + " €/t");
            } else {
                cout_unit_t.put(f, 0.0);
                JournalCout.ajouter("Step " + etape + " : Pas de production pour " + f + ", coût unitaire = 0 €/t");
            }
        }
    }


}