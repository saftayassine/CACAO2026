package abstraction.eq7Transformateur4;

import abstraction.eqXRomu.bourseCacao.BourseCacao;
import abstraction.eqXRomu.bourseCacao.IAcheteurBourse;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.Gamme;

public class Transformateur4AcheteurBourse extends Transformateur4Acteur implements IAcheteurBourse{

    //Auteur -> Aymeric
    @Override
    public double demande(Feve f, double cours) {  
        /* 
        if (f==Feve.F_BQ){
            return 200.;
        }
        else{
            return 0.0;
        }*/
        
        if (f.getGamme()==Gamme.MQ || f.getGamme()==Gamme.HQ || f.getGamme()==Gamme.BQ){
            BourseCacao bourse = (BourseCacao)(Filiere.LA_FILIERE.getActeur("BourseCacao"));
            double max = bourse.getCours(f).getMax();
            double min = bourse.getCours(f).getMin();
            if (max > min) {
                double pourcentage = (max - cours) / (max - min);
                double maxDemand = 200000; // Quantité maximale à demander
                return maxDemand * pourcentage;
            } else {
                return 0.0;
            }
        }
        else{
            return 0.0;
        }
    }
    //Auteur -> Matteo
    @Override
    public void notificationAchat(Feve f, double quantiteEnT, double coursEnEuroParT) {
        if (f.isEquitable()){
            this.get_EqStock().add(quantiteEnT, f.getGamme());
            this.journal_achat_bourse.ajouter("Achat de " + String.valueOf(quantiteEnT) + " tonnes de fèves " + f);
        }
        else{
            this.get_Stock().add(quantiteEnT, f.getGamme());
            this.journal_achat_bourse.ajouter("Achat de " + String.valueOf(quantiteEnT) + " tonnes de fèves " + f);
        }
    }
    //Auteur -> Matteo
    @Override
    public void notificationBlackList(int dureeEnStep) {
        // Ajouter une entrée dans le journal pour notifier la blacklist
        this.getJournaux().get(0).ajouter("Nous avons été blacklistés pour " + dureeEnStep + " étapes.");
    }

    

}
