package abstraction.eq6Transformateur3;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IActeur;
import abstraction.eqXRomu.bourseCacao.IAcheteurBourse;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.general.VariablePrivee;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.IProduit;
import abstraction.eq6Transformateur3.StockFeve;
import abstraction.eq6Transformateur3.StockChocolat;

/**@author: Brevael Le Clezio */

public class Transformateur3AcheteurBourse extends Transformateur3Acteur implements IAcheteurBourse {

    public Transformateur3AcheteurBourse() {
        super();
    }

    public double demande(Feve f, double cours) {

        journal.ajouter("Demande appelée pour " + f + " au cours " + cours);
        if (f == Feve.F_HQ) {
            return 80;
        }

        if (f == Feve.F_MQ) {
            return 120;
        }

        if (f == Feve.F_BQ) {
            return 60;
        }


        return 0;
    }

    public void notificationAchat(Feve f, double quantiteEnT, double coursEnEuroParT) {
        stockFeve.ajouterQuantite(f, quantiteEnT);

        journal.ajouter("Achat : " + quantiteEnT + " T de " + f);
    }

    public void notificationBlackList(int dureeEnStep) {
        journal.ajouter("Blacklist : " + dureeEnStep);
    }
}
