package abstraction.eq5Transformateur2;

import java.util.HashMap;

import abstraction.eqXRomu.bourseCacao.IAcheteurBourse;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.Feve;
/**
 * @Auteur Maxence
 */
public class Transformateur2AcheteurBourse extends Transformateur2FabriquantChocolatDeMarque implements IAcheteurBourse {

    public Transformateur2AcheteurBourse(){
        super();
    }
    /** 
    public double demande(Feve f, double cours) {
        HashMap<Chocolat, Double> demandeChoco =this.DemandeChocolat();
        if (f == Feve.F_BQ){
            if(this.getStock_feve(f)<10000.0){
                return demandeChoco.get(Chocolat.C_BQ) * 0.25;
            }
            else{
                return 0.0;
            }
        }
        if (f == Feve.F_MQ){
            if(this.getStock_feve(f)<10000.0){
                return demandeChoco.get(Chocolat.C_MQ) * 0.25;
            }
            else{
                return 0.0;
            }
        }
        else{
            if(this.getStock_feve(f)<10000.0){
                return demandeChoco.get(Chocolat.C_HQ) * 0.25;
            }
            else{
                return 0.0;
            }
        }
    }
    */

    public double demande(Feve f, double cours) {
        // 1. Frein d'urgence global
        if (this.getStock_feve_total() > 650000.0) {
            return 0.0;
        }

        // 2. On récupère nos cibles (les mêmes que pour les Contrats Cadres)
        double cible = 0.0;
        if (f == Feve.F_HQ) cible = 98000.0;
        else if (f == Feve.F_MQ) cible = 154000.0;
        else if (f == Feve.F_BQ) cible = 238000.0;

        double stockActuel = this.getStock_feve(f);

        // 3. Achat de survie : Si le stock tombe sous 30% de la cible, on achète en urgence !
        if (stockActuel < cible * 0.30) {
            // On achète par blocs de 5000 Tonnes max pour ne pas vider la trésorerie d'un coup
            return Math.min(7500.0, cible - stockActuel); 
        }
        
        return 0.0; // Sinon, on n'achète rien en Bourse
    }
    
    public void notificationAchat(Feve f, double quantiteEnT, double coursEnEuroParT) {
        this.getJournaux().get(7).ajouter("Achat effectué de: "+quantiteEnT+" fèves "+f+" au prix/tonne de "+coursEnEuroParT);

        this.add_feve(quantiteEnT,f);
    }

    public void notificationBlackList(int dureeEnStep) {
        this.getJournaux().get(7).ajouter("Nous avons été blacklistés pour "+dureeEnStep+" étapes.");
    }
    

}
