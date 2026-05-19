package abstraction.eq1Producteur1;
import abstraction.eqXRomu.filiere.Banque;
import abstraction.eqXRomu.filiere.Filiere;
import java.util.HashMap;

public class Producteur1Cooperative extends Producteur1Planteur {

    HashMap<String, Double> coopNonEq = new HashMap<>();
    HashMap<String, Double> coopEq = new HashMap<>();

    // 50% de la force de travail non équitable est faite par des enfants
    double pourcentageEnfant = 0.5;

    // PART DE LA SURFACE ÉQUITABLE (0 = tout non équitable)
    double partEquitable = 0.0;

    public Producteur1Cooperative() {
        super();

        //-----------------------------
        // SALAIRES
        //-----------------------------
        coopEq.put("salaire adultes", 2.0);

        coopNonEq.put("salaire adultes", 0.5);
        coopNonEq.put("salaire enfants", 0.2);

        //-----------------------------
        // RÉPARTITION DES TERRES
        //-----------------------------
        double surfaceTotale = 1000000;

        double surfaceEq = surfaceTotale * partEquitable;
        double surfaceNonEq = surfaceTotale - surfaceEq;

        coopEq.put("taille", surfaceEq);
        coopNonEq.put("taille", surfaceNonEq);

        //-----------------------------
        // COOP ÉQUITABLE (100% adultes)
        //-----------------------------
        double forceEq = surfaceEq * 30;

        coopEq.put("nombre adultes", forceEq);

        //-----------------------------
        // COOP NON ÉQUITABLE
        //-----------------------------
        double forceNonEq = surfaceNonEq * 30;

        double travailEnfant = forceNonEq * pourcentageEnfant;
        double travailAdulte = forceNonEq - travailEnfant;

        double nbEnfant = travailEnfant * 2;
        double nbAdulte = travailAdulte;

        coopNonEq.put("nombre enfants", nbEnfant);
        coopNonEq.put("nombre adultes", nbAdulte);
    }

    // -----------------------------
    // GETTERS
    // -----------------------------
    public double getSalaireAdulte(boolean equitable) {
        return equitable
                ? coopEq.get("salaire adultes")
                : coopNonEq.get("salaire adultes");
    }

    public double getNombreAdultes(boolean equitable) {
        return equitable
                ? coopEq.get("nombre adultes")
                : coopNonEq.get("nombre adultes");
    }

    public double getNombreEnfant() {
        return coopNonEq.get("nombre enfants");
    }

    public double getSalaireEnfant() {
        return coopNonEq.get("salaire enfants");
    }

    // -----------------------------
    // POURCENTAGE ENFANTS
    // -----------------------------
    public void setPourcentageEnfants(double p) {

        if (p >= 0 && p <= 1) {

            this.pourcentageEnfant = p;

            double surfaceNonEq = coopNonEq.get("taille");
            double force = surfaceNonEq * 30;

            double travailEnfant = force * p;
            double travailAdulte = force - travailEnfant;

            coopNonEq.put("nombre enfants", travailEnfant * 2);
            coopNonEq.put("nombre adultes", travailAdulte);
        }
    }

    // -----------------------------
    // SALAIRES
    // -----------------------------
    public void payerSalaire() {

        double montantJournalier =
                coopEq.get("nombre adultes") * coopEq.get("salaire adultes")
                        +
                coopNonEq.get("nombre adultes") * coopNonEq.get("salaire adultes")
                        +
                coopNonEq.get("nombre enfants") * coopNonEq.get("salaire enfants");

        double montant = montantJournalier * 15;

        Banque banque = Filiere.LA_FILIERE.getBanque();

        banque.payerCout(
                this,
                this.cryptogramme,
                "Masse salariale",
                montant
        );

        this.journalBanque.ajouter(
                "Salaire payé : " + montant
        );
    }

    public void setSalaire(boolean adulte, boolean equitable, double salaire) {

        if (equitable) {
            coopEq.put("salaire adultes", salaire);
        }
        else if (adulte) {
            coopNonEq.put("salaire adultes", salaire);
        }
        else {
            coopNonEq.put("salaire enfants", salaire);
        }
    }

    public void next() {
        super.next();
        this.payerSalaire();
    }
}