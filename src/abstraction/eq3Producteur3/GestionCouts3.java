package abstraction.eq3Producteur3;
import java.util.List;

import abstraction.eq3Producteur3.Agriculteurs3;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.Gamme;


/** @author Guillaume Leroy*/
public class GestionCouts3 {

    private double coutHectare;
    private double coutStockageTonne;
    private double coutLabelHappyWorker;

    public GestionCouts3(){
        this.coutHectare= 7.8;
        this.coutLabelHappyWorker=1000;
        this.coutStockageTonne=7.5;
    }

    public void nextCout(Producteur3Acteur acteur) {
        // Coût plantation
        double coutPlantation = acteur.plantationeq3.getNbHectareTotal() * this.coutHectare;
        acteur.journal_cout_periode.ajouter("Période " + Filiere.LA_FILIERE.getEtape() + " : coût plantation = " + coutPlantation);
        Filiere.LA_FILIERE.getBanque().payerCout(acteur, acteur.cryptogramme, "Coût des plantations", coutPlantation);

        // Coût stockage (7.5 par unité)
        double coutStock = acteur.stock.getCoutStockage(this.coutStockageTonne);
        acteur.journal_cout_periode.ajouter("Période " + Filiere.LA_FILIERE.getEtape() + " : coût stockage = " + coutStock);
        if (coutStock>0){Filiere.LA_FILIERE.getBanque().payerCout(acteur, acteur.cryptogramme, "Coût du stockage", coutStock);}

        // Coût Main d'oeuvre
        for (Feve f : List.of(Feve.F_BQ, Feve.F_MQ, Feve.F_HQ, Feve.F_MQ_E, Feve.F_HQ_E)){
            double coutMO = acteur.agriculteurs.getCoutMainOeuvreFeve(f);
            acteur.journal_cout_periode.ajouter("Période " + Filiere.LA_FILIERE.getEtape() + " : coût main d'oeuvre de "+f.toString()+" = " + coutMO);
            if (coutMO>0){Filiere.LA_FILIERE.getBanque().payerCout(acteur, acteur.cryptogramme, "Coût de la main d'oeuvre", coutMO);}
        }
        for (Gamme g : List.of(Gamme.BQ, Gamme.MQ, Gamme.HQ)) {
        if (acteur.agriculteurs.getStatutHappyWorker(g)){ 
            acteur.journal_cout_periode.ajouter("Période " + Filiere.LA_FILIERE.getEtape() + " : label Happyworker pour " + g.toString() +" = "+ this.coutLabelHappyWorker);
            Filiere.LA_FILIERE.getBanque().payerCout(acteur, acteur.cryptogramme, "label Happyworker", this.coutLabelHappyWorker);
        }}
    }
    
    public double getCoutTot(Producteur3Acteur acteur){
        return acteur.plantationeq3.getNbHectareTotal() * this.coutHectare + acteur.stock.getCoutStockage(this.coutStockageTonne) + acteur.agriculteurs.getCoutMainOeuvreTotal();
    }

    public double getCoutFeve(Feve f ,  Producteur3Acteur acteur){
        double c= acteur.agriculteurs.getCoutMainOeuvreFeve(f) + acteur.plantationeq3.getRepartitionTerrainFeve(f)*this.coutHectare;
        Gamme g = f.getGamme();
            if (acteur.agriculteurs.getStatutHappyWorker(g)){
                c+= this.coutLabelHappyWorker;
            }
        return c ;
    }
}
