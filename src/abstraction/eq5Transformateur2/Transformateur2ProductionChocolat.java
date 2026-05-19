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

        // 1. PAIEMENT DES EMPLOYÉS (Coût fixe) 
        double coutSalaires = 9000 * 625.0;
        
        // On demande à la banque de payer nos employés
        Filiere.LA_FILIERE.getBanque().payerCout(this, cryptogramme, "Salaires des employés", coutSalaires);

        // 2. OPTIMISATION DE LA PRODUCTION (Flux tendu)
        double stockCibleHQ = 200000.0;
        double stockCibleMQ = 200000.0;
        double stockCibleBQ = 200000.0;

        // On recrée nos références exactes pour lire les stocks
        ChocolatDeMarque chocoHQ = new ChocolatDeMarque(Chocolat.C_HQ, "Ferrara Rocher", 100);
        ChocolatDeMarque chocoMQ = new ChocolatDeMarque(Chocolat.C_MQ, "Ferrara Rocher", 100);
        ChocolatDeMarque chocoBQ = new ChocolatDeMarque(Chocolat.C_BQ, "Ferrara Rocher", 45); 

        // On calcule ce qu'il nous MANQUE pour atteindre l'objectif
        double aProduireHQ = stockCibleHQ - this.getStock_chocolatDeMarque(chocoHQ);
        double aProduireMQ = stockCibleMQ - this.getStock_chocolatDeMarque(chocoMQ);
        double aProduireBQ = stockCibleBQ - this.getStock_chocolatDeMarque(chocoBQ);

        // Capacité de production de notre usine 
        double capaciteRestante = 9000 * 8.4; 

        // 3. On lance la production par ordre de priorité (le HQ rapporte le plus !)        
        if (aProduireHQ > 0 && capaciteRestante > 0) {
            double stockFeveHQ = this.getStock_feve(Feve.F_HQ);
            double stockFeveMQ = this.getStock_feve(Feve.F_MQ);
            
            double maxPossibleHQ = stockFeveHQ / 0.49;
            double maxPossibleMQ = stockFeveMQ / 0.51;
            
            double prodHQ = Math.min(Math.min(aProduireHQ, capaciteRestante), Math.min(maxPossibleHQ, maxPossibleMQ));
            this.ProductionFerraraHQ(prodHQ);
            capaciteRestante -= prodHQ; // On met à jour la capacité restante
        }
        
        if (aProduireMQ > 0 && capaciteRestante > 0) {
            double stockFeveMQ = this.getStock_feve(Feve.F_MQ);
            double stockFeveBQ = this.getStock_feve(Feve.F_BQ);
            
            double maxPossibleMQ = stockFeveMQ / 0.26;
            double maxPossibleBQ = stockFeveBQ / 0.74;
        
            double prodMQ = Math.min(Math.min(aProduireMQ, capaciteRestante), Math.min(maxPossibleMQ, maxPossibleBQ));
            this.ProductionFerraraMQ(prodMQ);
            capaciteRestante -= prodMQ;
        }

        if (aProduireBQ > 0 && capaciteRestante > 0) {
            double stockFeveBQ = this.getStock_feve(Feve.F_BQ);
        
            double maxPossibleBQ = stockFeveBQ / 0.45;
            double prodBQ = Math.min(Math.min(aProduireBQ, capaciteRestante), maxPossibleBQ);
            this.ProductionFerraraBQ(prodBQ);
            capaciteRestante -= prodBQ;
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
            double quantiteMP = quantiteDemandee * 0.65;
            
            this.remove_feve(fevesBQUtilisees, Feve.F_BQ);
            
            ChocolatDeMarque chocoBQ = new ChocolatDeMarque(Chocolat.C_BQ, "Ferrara Rocher", 45);
            this.add_chocolatDeMarque(chocoBQ, quantiteDemandee);
            
            Filiere.LA_FILIERE.getBanque().payerCout(this, cryptogramme, "Achat de MP pour production Ferrara BQ", quantiteMP * prix_MP);
        }
    }
}