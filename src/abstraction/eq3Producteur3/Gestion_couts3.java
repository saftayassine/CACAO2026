package abstraction.eq3Producteur3;
import abstraction.eq3Producteur3.Agriculteurs3;
import abstraction.eqXRomu.filiere.Filiere;

/** @author Guillaume Leroy*/
public class Gestion_couts3 {

    public void nextCout(Producteur3Acteur acteur) {
        // Coût plantation
        double coutPlantation = acteur.plantationeq3.getNbHectareTotal() * 250;
        acteur.journal_cout_periode.ajouter("Période " + Filiere.LA_FILIERE.getEtape() + " : coût plantation = " + coutPlantation);
        Filiere.LA_FILIERE.getBanque().payerCout(acteur, acteur.cryptogramme, "Coût des plantations", coutPlantation);

        // Coût stockage (7.5 par unité)
        double coutStock = acteur.stock.getCoutStockage(7.5);
        acteur.journal_cout_periode.ajouter("Période " + Filiere.LA_FILIERE.getEtape() + " : coût stockage = " + coutStock);
        Filiere.LA_FILIERE.getBanque().payerCout(acteur, acteur.cryptogramme, "Coût du stockage", coutStock);

        // Coût Main d'oeuvre
        double coutMO = acteur.agriculteurs.getCoutMainOeuvreTotal();
        acteur.journal_cout_periode.ajouter("Période " + Filiere.LA_FILIERE.getEtape() + " : coût main d'oeuvre = " + coutMO);
        Filiere.LA_FILIERE.getBanque().payerCout(acteur, acteur.cryptogramme, "Coût de la main d'oeuvre", coutMO);
    }
    
    public double getCoutTot(Producteur3Acteur acteur){
        return acteur.plantationeq3.getNbHectareTotal() * 250 + acteur.stock.getCoutStockage(7.5) + acteur.agriculteurs.getCoutMainOeuvreTotal();
    }
}
