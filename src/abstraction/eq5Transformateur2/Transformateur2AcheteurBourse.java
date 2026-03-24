package abstraction.eq5Transformateur2;

import java.util.List;
import abstraction.eqXRomu.produits.Gamme;
import abstraction.eqXRomu.bourseCacao.IAcheteurBourse;
import abstraction.eqXRomu.filiere.Filiere;
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

        if (f.getGamme()==Gamme.MQ){
            return 80;
        }
        else {
            return 0;
        }
    }
    
    /** @author Maxence
    **/
    public void notificationAchat(Feve f, double quantiteEnT, double coursEnEuroParT) {
        this.getJournaux().get(1).ajouter("Achat effectué de: "+quantiteEnT+" fèves "+f+" au prix/tonne de "+coursEnEuroParT);
        this.getJournaux().get(4).ajouter("Achat effectué de: "+quantiteEnT+" fèves "+f+" au prix/tonne de "+coursEnEuroParT);

        this.add_feve(quantiteEnT,f);
    }

    /** @author Maxence
    **/
    public void notificationBlackList(int dureeEnStep) {
        this.getJournaux().get(4).ajouter("Nous avons été blacklistés pour "+dureeEnStep+" étapes.");
    }
    

}
