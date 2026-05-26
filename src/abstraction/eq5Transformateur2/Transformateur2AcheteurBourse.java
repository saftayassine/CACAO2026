package abstraction.eq5Transformateur2;
import abstraction.eqXRomu.bourseCacao.IAcheteurBourse;
import abstraction.eqXRomu.produits.Feve;
/**
 * @Auteur Maxence
 */
public class Transformateur2AcheteurBourse extends Transformateur2FabriquantChocolatDeMarque implements IAcheteurBourse {

    public Transformateur2AcheteurBourse(){
        super();
    }

    public double demande(Feve f, double cours) {
        if (this.getStock_feve_total() > 650000.0) {
            return 0.0;
        }

        //On récupère nos cibles (les mêmes que pour les Contrats Cadres)
        double cible = 0.0;
        if (f == Feve.F_HQ) cible = 98000.0;
        else if (f == Feve.F_MQ) cible = 154000.0;
        else if (f == Feve.F_BQ) cible = 238000.0;

        double stockActuel = this.getStock_feve(f);

        // On achète si le stock est trop bas, on achète mais pas trop
        if (stockActuel < cible * 0.30) {
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
