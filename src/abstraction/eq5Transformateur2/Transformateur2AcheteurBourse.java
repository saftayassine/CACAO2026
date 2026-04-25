package abstraction.eq5Transformateur2;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import abstraction.eqXRomu.produits.Gamme;
import abstraction.eqXRomu.bourseCacao.IAcheteurBourse;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Feve;
/**
 * Auteur Maxence
 */
public class Transformateur2AcheteurBourse extends Transformateur2AnalyseurMarche implements IAcheteurBourse {

    /** @author Maxence
    **/
    public Transformateur2AcheteurBourse(){
        super();
    }

    /** @author Maxence
    **/
    public double demande(Feve f, double cours) {
        HashMap<Chocolat, Double> demandeChoco =this.DemandeChocolat();
        if (f == Feve.F_BQ){
            return demandeChoco.get(Chocolat.C_BQ) * 0.25;
        }
        if (f == Feve.F_MQ){
            return demandeChoco.get(Chocolat.C_MQ) * 0.20;
        }
        else{
            return demandeChoco.get(Chocolat.C_HQ) * 0.15;
        }
    }
    

    
    /** @author Maxence
    **/
    public void notificationAchat(Feve f, double quantiteEnT, double coursEnEuroParT) {
        this.getJournaux().get(1).ajouter("Achat effectué de: "+quantiteEnT+" fèves "+f+" au prix/tonne de "+coursEnEuroParT);
        this.getJournaux().get(7).ajouter("Achat effectué de: "+quantiteEnT+" fèves "+f+" au prix/tonne de "+coursEnEuroParT);

        this.add_feve(quantiteEnT,f);
    }

    /** @author Maxence
    **/
    public void notificationBlackList(int dureeEnStep) {
        this.getJournaux().get(7).ajouter("Nous avons été blacklistés pour "+dureeEnStep+" étapes.");
    }
    

}
