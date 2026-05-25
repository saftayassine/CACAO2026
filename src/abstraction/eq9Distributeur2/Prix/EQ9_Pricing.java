package abstraction.eq9Distributeur2.Prix;

import abstraction.eq9Distributeur2.Config.EQ9Config;
import abstraction.eq9Distributeur2.Stratégie.EQ9_StrategieConcurrentielle;
import abstraction.eqXRomu.produits.ChocolatDeMarque;

public class EQ9_Pricing {

    private EQ9_StrategieConcurrentielle stratConcu;

    public EQ9_Pricing() {
        this.stratConcu = new EQ9_StrategieConcurrentielle();
    }

    /**
     * @author 
     */
    public double calculerPrix(
            ChocolatDeMarque choco,
            double coutAchat,
            double stockT,
            double dos,
            double demande,
            double prixConcurrent,
            double partMarche,
            double cash) {

        double margeGamme = margeSelonGamme(choco);
        double prix = coutAchat * (1.0 + margeGamme);

        prix *= facteurDemande(demande, stockT);
        prix *= facteurStock(dos);
        prix = stratConcu.ajusterSelonConcurrence(prix, prixConcurrent);
        prix *= facteurEntreprise(partMarche);
        prix *= facteurEQ9(stockT, cash);

        return prix;
    }

    //  MARGES PAR GAMME
    private double margeSelonGamme(ChocolatDeMarque choco) {
        String nom = choco.getNom();

        if (nom.contains("EQ9")) return 0.35; // marque propre
        if (nom.contains("HQ")) return 0.25;
        if (nom.contains("MQ")) return 0.18;
        if (nom.contains("BQ")) return 0.12;

        return 0.18; // défaut
    }

    //  FACTEUR DEMANDE
    private double facteurDemande(double demande, double stockT) {
        if (stockT <= 0) return 1.20; // rupture → prix haut

        double ratio = demande / (stockT * 1000.0);

        if (ratio > 1.5) return 1.15;
        if (ratio > 1.0) return 1.05;
        if (ratio < 0.5) return 0.95;

        return 1.0;
    }

    //  FACTEUR STOCK (DOS)
    private double facteurStock(double dos) {
        if (dos < 10) return 1.10;  // stock critique : prix haut
        if (dos < 20) return 1.05;  // stock bas
        if (dos > 40) return 0.95;  // surstock
        if (dos > 60) return 0.90;  // surstock massif

        return 1.0;
    }

    //  STRATÉGIE D’ENTREPRISE
    private double facteurEntreprise(double partMarche) {
        double cible = EQ9Config.PART_MARCHE_CIBLE * 100;

        if (partMarche < cible) {
            return 0.97; // on baisse un peu pour gagner du marché
        }
        return 1.0;
    }

    //  STRATÉGIE EQ9
    private double facteurEQ9(double stockT, double cash) {

        // Grosse tréso : prix "agressifs"
        if (cash > EQ9Config.CASH_BUFFER_MIN * 2) {
            return 0.98;
        }

        // inversement
        if (stockT < EQ9Config.SEUIL_MIN_T) {
            return 1.05;
        }

        return 1.0;
    }
}
