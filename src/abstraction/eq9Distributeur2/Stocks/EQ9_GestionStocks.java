package abstraction.eq9Distributeur2.Stocks;

import abstraction.eq9Distributeur2.Config.EQ9Config;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import java.util.Map;


/**
* @author Paul JUHEL
*/

public class EQ9_GestionStocks {

    private Map<? super ChocolatDeMarque, Double> stock;
    private java.util.function.Function<ChocolatDeMarque, Double> restantDuCC;

    public EQ9_GestionStocks(Map<? super ChocolatDeMarque, Double> stock,
                             java.util.function.Function<ChocolatDeMarque, Double> restantDuCC) {
        this.stock = stock;
        this.restantDuCC = restantDuCC;
    }

    public double stockProjete(ChocolatDeMarque choco) {
        return stock.getOrDefault(choco, 0.0) + restantDuCC.apply(choco);
    }

    public double ventesMoyennes(ChocolatDeMarque choco) {
        int etape = Filiere.LA_FILIERE.getEtape();
        if (etape < 1) return 0.0;
        double ventes = Filiere.LA_FILIERE.getVentes(choco, etape - 1);
        return ventes ;
    }

    public double DOS(ChocolatDeMarque choco) {
        double stockKg = stockProjete(choco) * 1000.0;
        double ventesJour = ventesMoyennes(choco);
        return ventesJour > 0 ? stockKg / ventesJour : 999.0;
    }

    public boolean risqueRupture(ChocolatDeMarque choco) {
        return DOS(choco) < 10; // moins de 10 jours de stock
    }

    public boolean surstock(ChocolatDeMarque choco) {
        return DOS(choco) > 40; // plus de 40 jours de stock
    }

    public boolean doitAcheter(ChocolatDeMarque choco) {
        double sp = stockProjete(choco);
        return sp < EQ9Config.STOCK_CIBLE_T * 1000;
    }

    public double quantiteAacheter(ChocolatDeMarque choco) {
        double sp = stockProjete(choco);
        double cible = EQ9Config.STOCK_CIBLE_T;

        if (sp < EQ9Config.SEUIL_MIN_T) {
            return cible - sp;
        }
        if (sp < cible) {
            return (cible - sp) * 0.7;
        }
        return 0.0;
    }

    public boolean prefererCC(ChocolatDeMarque choco) {
        return DOS(choco) > 20; // si on a du temps : CC
    }

    public boolean prefererAO(ChocolatDeMarque choco) {
        return DOS(choco) < 20; // besoin rapide : AO
    }



}