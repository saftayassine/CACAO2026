package abstraction.eq5Transformateur2;

import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.filiere.Filiere;

/** @author Pierre
 */
public class Transformateur2ProductionChocolat extends Transformateur2Stock {
    
    public Transformateur2ProductionChocolat(){
        super();

    }

    @Override
    public void next() {
        super.next();

        // 1. PAIEMENT DES EMPLOYÉS
        double coutSalaires = 9000 * 625.0;
        Filiere.LA_FILIERE.getBanque().payerCout(this, cryptogramme, "Salaires des employés", coutSalaires);

        // 2. CONFIGURATION DES STOCKS CIBLES
        double stockCibleHQ = 200000.0;
        double stockCibleMQ = 200000.0;
        double stockCibleBQ = 200000.0;

        ChocolatDeMarque chocoHQ = new ChocolatDeMarque(Chocolat.C_HQ, "Ferrara Rocher", 100);
        ChocolatDeMarque chocoMQ = new ChocolatDeMarque(Chocolat.C_MQ, "Ferrara Rocher", 100);
        ChocolatDeMarque chocoBQ = new ChocolatDeMarque(Chocolat.C_BQ, "Ferrara Rocher", 45);

        double capaciteProductionTour = 9000 * 8.4; 
        
        double besoinHQ = Math.max(0, stockCibleHQ - this.getStock_chocolatDeMarque(chocoHQ));
        double besoinMQ = Math.max(0, stockCibleMQ - this.getStock_chocolatDeMarque(chocoMQ));
        double besoinBQ = Math.max(0, stockCibleBQ - this.getStock_chocolatDeMarque(chocoBQ));

        // --- DIRECTIVE CRITIQUE : CAPTURE DES STOCKS DOS DOS AVANT PRODUCTION ---
        double stockFeveHQ_Initial = this.getStock_feve(Feve.F_HQ);
        double stockFeveMQ_Initial = this.getStock_feve(Feve.F_MQ);
        double stockFeveBQ_Initial = this.getStock_feve(Feve.F_BQ);

        // --- PRODUCTION HQ ---
        if (stockFeveHQ_Initial > 0 && stockFeveMQ_Initial > 0 && besoinHQ > 0 && capaciteProductionTour > 0) {
            double maxPossibleHQ = Math.min(stockFeveHQ_Initial / 0.49, stockFeveMQ_Initial / 0.51);
            double capaciteAlloueeHQ = capaciteProductionTour / 5.0; // 1/3 max de l'usine
            
            double prodHQ = Math.min(besoinHQ, Math.min(maxPossibleHQ, capaciteAlloueeHQ));
            this.ProductionFerraraHQ(prodHQ);
            capaciteProductionTour -= prodHQ;
            
            // On met à jour les stocks virtuels restants pour les lignes suivantes
            stockFeveMQ_Initial -= (prodHQ * 0.51);
        }

        // --- PRODUCTION BQ (On la fait passer AVANT le MQ pour protéger ses fèves !) ---
        if (stockFeveBQ_Initial > 0 && besoinBQ > 0 && capaciteProductionTour > 0) {
            // On réserve la moitié du stock de fèves BQ pour le chocolat BQ, l'autre moitié ira au MQ
            double fèvesBQPourCeBloc = stockFeveBQ_Initial * 0.5; 
            double maxPossibleBQ = fèvesBQPourCeBloc / 0.45;
            
            double capaciteAlloueeBQ = 2*capaciteProductionTour / 5.0; // Partage équitable du reste
            
            double prodBQ = Math.min(besoinBQ, Math.min(maxPossibleBQ, capaciteAlloueeBQ));
            this.ProductionFerraraBQ(prodBQ);
            capaciteProductionTour -= prodBQ;
            
            // On déduit ce qui a été consommé du stock réel pour le MQ
            stockFeveBQ_Initial -= (prodBQ * 0.45);
        }

        // --- PRODUCTION MQ (En dernier, elle prend les restes de fèves MQ et BQ) ---
        if (stockFeveMQ_Initial > 0 && stockFeveBQ_Initial > 0 && besoinMQ > 0 && capaciteProductionTour > 0) {
            double maxPossibleMQ = Math.min(stockFeveMQ_Initial / 0.26, stockFeveBQ_Initial / 0.74);
            double capaciteAlloueeMQ = capaciteProductionTour; // Prend tout ce qui reste de main d'œuvre
            
            double prodMQ = Math.min(besoinMQ, Math.min(maxPossibleMQ, capaciteAlloueeMQ));
            this.ProductionFerraraMQ(prodMQ);
            capaciteProductionTour -= prodMQ;
        }
    }

    public void ProductionChocolat(Chocolat c,Double n){
        if(c==Chocolat.C_HQ){
            ProductionFerraraHQ(n);
        }
        else if(c==Chocolat.C_MQ){
            ProductionFerraraMQ(n);
        }
        else if(c==Chocolat.C_BQ){
            ProductionFerraraBQ(n);
        }
    }

    /**
     * @author Maxence & Pierre
     * Notre Chocolat HQ a 100% de cacao, dont 49% de fèves HQ et 51% de fèves MQ
     */
    public void ProductionFerraraHQ(Double quantiteDemandee){
        
        if (quantiteDemandee > 0) {
            double fevesHQUtilisees = quantiteDemandee * 0.49;
            double fevesMQUtilisees = quantiteDemandee * 0.51;
            
            this.remove_feve(fevesHQUtilisees, Feve.F_HQ);
            this.remove_feve(fevesMQUtilisees, Feve.F_MQ);
            
            ChocolatDeMarque chocoHQ = new ChocolatDeMarque(Chocolat.C_HQ, "Ferrara Rocher", 100);
            this.add_chocolatDeMarque(chocoHQ, quantiteDemandee);
        }
    }

    /** 
     * @author Maxence & Pierre
     * notre chocolat MQ a 100% de cacao, dont 26% de fèves MQ et 74% de fèves BQ
     */
    public void ProductionFerraraMQ(Double quantiteDemandee){
        
        if (quantiteDemandee > 0) {
            double fevesMQUtilisees = quantiteDemandee * 0.26;
            double fevesBQUtilisees = quantiteDemandee * 0.74;
            
            this.remove_feve(fevesMQUtilisees, Feve.F_MQ);
            this.remove_feve(fevesBQUtilisees, Feve.F_BQ);
            
            ChocolatDeMarque chocoMQ = new ChocolatDeMarque(Chocolat.C_MQ, "Ferrara Rocher", 100);
            this.add_chocolatDeMarque(chocoMQ, quantiteDemandee);       
        }
    }

    /** 
     * @author Maxence & Pierre
     * notre chocolat BQ a 45% de cacao
     */
    public void ProductionFerraraBQ(Double quantiteDemandee){
        
        if (quantiteDemandee > 0) {
            double fevesBQUtilisees = quantiteDemandee * 0.45;
            double quantiteMP = quantiteDemandee * 0.55;
            
            this.remove_feve(fevesBQUtilisees, Feve.F_BQ);
            
            ChocolatDeMarque chocoBQ = new ChocolatDeMarque(Chocolat.C_BQ, "Ferrara Rocher", 45);
            this.add_chocolatDeMarque(chocoBQ, quantiteDemandee);
            
            Filiere.LA_FILIERE.getBanque().payerCout(this, cryptogramme, "Achat de MP pour production Ferrara BQ", quantiteMP * prix_MP);
        }
    }
}