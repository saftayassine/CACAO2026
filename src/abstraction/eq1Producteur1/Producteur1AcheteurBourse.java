package abstraction.eq1Producteur1;

import java.util.ArrayList;
import java.util.List;

import abstraction.eqXRomu.produits.Gamme;
import abstraction.eqXRomu.bourseCacao.BourseCacao;
import abstraction.eqXRomu.bourseCacao.IAcheteurBourse;
import abstraction.eqXRomu.encheres.MiseAuxEncheres;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.Feve;

/**
 * @author Théophile Trillat & Elise Dossal
 */
public class Producteur1AcheteurBourse extends Producteur1Planteur implements IAcheteurBourse {

    public Producteur1AcheteurBourse() {
        super();
    }


    public void notificationAchat(Feve f, double quantiteEnT, double coursEnEuroParT) {
        this.getJournaux().get(1).ajouter("Achat effectué de: "+quantiteEnT+" fèves "+f+" au prix/tonne de "+coursEnEuroParT);
        this.getJournaux().get(4).ajouter("Achat effectué de: "+quantiteEnT+" fèves "+f+" au prix/tonne de "+coursEnEuroParT);

    }

    public void notificationBlackList(int dureeEnStep) {
        this.journal.ajouter("Aie... je suis blackliste... j'aurais du verifier "
                + "que j'avais assez d'argent avant de passer une trop grosse commande en bourse...");
    }


    @Override
    public double demande(Feve f, double cours) {
        // TODO Auto-generated method stub
        return 0;
    }
}