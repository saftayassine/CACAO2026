package abstraction.eq9Distributeur2.Stocks;

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
        double stockT = stockProjete(choco) ;
        double ventesParEtape = ventesMoyennes(choco);
        return ventesParEtape > 0 ? stockT / ventesParEtape : 999.0;
    }

    public boolean risqueRupture(ChocolatDeMarque choco) {
        return DOS(choco) < 10; // moins de 10 jours de stock
    }

    public boolean surstock(ChocolatDeMarque choco) {
        return DOS(choco) > 40; // plus de 40 jours de stock
    }

    public boolean doitAcheter(ChocolatDeMarque choco) {
        int etape = Filiere.LA_FILIERE.getEtape();
        double sp = stockProjete(choco);
        double ventes = ventesMoyennes(choco);
        
        
        if (etape < 3) {
            return sp < 10.0; 
        }
        
        
        if (ventes <= 0.0) {
            return false;
        }

    
        double cibleDynamique = ventes * 3.0; 
        return sp < cibleDynamique;
    }

    public double quantiteAacheter(ChocolatDeMarque choco) {
        int etape = Filiere.LA_FILIERE.getEtape();
        double sp = stockProjete(choco);
        double ventes = ventesMoyennes(choco);
        
        if (etape < 3 && sp < 10.0) {
            return 10.0 - sp; // 10 Tonnes d'amorçage max
        }
        
        if (ventes <= 0.0) {
            return 0.0; // 
        }
        
        double cibleDynamique = ventes * 3.0;
        return Math.max(0.0, cibleDynamique - sp);
    }

    public boolean prefererCC(ChocolatDeMarque choco) {
        return DOS(choco) > 20; // si on a du temps : CC
    }

    public boolean prefererAO(ChocolatDeMarque choco) {
        return DOS(choco) < 20; // besoin rapide : AO
    }



}