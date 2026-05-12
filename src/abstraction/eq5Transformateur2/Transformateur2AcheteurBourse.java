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
    
    public void notificationAchat(Feve f, double quantiteEnT, double coursEnEuroParT) {
        this.getJournaux().get(7).ajouter("Achat effectué de: "+quantiteEnT+" fèves "+f+" au prix/tonne de "+coursEnEuroParT);

        this.add_feve(quantiteEnT,f);
    }

    public void notificationBlackList(int dureeEnStep) {
        this.getJournaux().get(7).ajouter("Nous avons été blacklistés pour "+dureeEnStep+" étapes.");
    }
    

}
