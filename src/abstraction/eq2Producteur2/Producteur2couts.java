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
            double coutUnitaire = 0.0;

            if (f == Feve.F_BQ)
                coutUnitaire = 300.0;
            else if (f == Feve.F_MQ)
                coutUnitaire = 370.0;
            else if (f == Feve.F_HQ)
                coutUnitaire = 500.0;
            else if (f == Feve.F_HQ_E)
                coutUnitaire = 1000.0;

            cout_unit_t.put(f, coutUnitaire);
            JournalCout.ajouter("Step " + etape + " : Coût unitaire de production estimé pour " + f + " = "
                    + coutUnitaire + " €/t");
        }
    }

    public double getCoutUnitaire(Feve f) {
        return this.cout_unit_t.getOrDefault(f, 0.0);
    }

}