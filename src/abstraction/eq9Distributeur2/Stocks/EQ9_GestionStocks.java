package abstraction.eq9Distributeur2.Stocks;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eq9Distributeur2.Config.EQ9Config;
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
        double stockT = stockProjete(choco) ;
        double ventesParEtape = ventesMoyennes(choco);
        return ventesParEtape > 0 ? stockT / ventesParEtape : 999.0;
    }

    // Rupture si < 0.2 NEXT de stock
    public boolean risqueRupture(ChocolatDeMarque choco) {
        return DOS(choco) < 0.2;
    }

    // Surstock si > 1 NEXT de stock
    public boolean surstock(ChocolatDeMarque choco) {
        return DOS(choco) > 1.0;
    }


    // Achat si stock projeté < stock cible
    public boolean doitAcheter(ChocolatDeMarque choco) {
        return stockProjete(choco) < EQ9Config.STOCK_CIBLE_T;
    }

    // Quantité à acheter cohérente
    public double quantiteAacheter(ChocolatDeMarque choco) {
        int etape = Filiere.LA_FILIERE.getEtape();
        double sp = stockProjete(choco);
        double cible = EQ9Config.STOCK_CIBLE_T;

        if (sp < EQ9Config.SEUIL_MIN_T) {
            return cible - sp; // remonter à 30k t
        }
        if (sp < cible) {
            return (cible - sp) * 0.5; // achat progressif
        }
        
        double cibleDynamique = ventesMoyennes(choco) * 3.0;
        return Math.max(0.0, cibleDynamique - sp);
    }

    // CC si on a du temps (stock > 0.5 NEXT)
    public boolean prefererCC(ChocolatDeMarque choco) {
        return DOS(choco) > 0.5;
    }

    // AO si besoin rapide
    public boolean prefererAO(ChocolatDeMarque choco) {
        return DOS(choco) < 0.5;
    }


}