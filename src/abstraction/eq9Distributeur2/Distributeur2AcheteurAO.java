package abstraction.eq9Distributeur2;

import abstraction.eqXRomu.appelDOffre.AppelDOffre;
import abstraction.eqXRomu.appelDOffre.IAcheteurAO;
import abstraction.eqXRomu.appelDOffre.OffreVente;
import abstraction.eqXRomu.appelDOffre.SuperviseurVentesAO;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import java.util.List;

    /**  
	 * @author Anass Ouisrani
     */
public class Distributeur2AcheteurAO extends Distributeur2Acteur implements IAcheteurAO {

    //  recherche 
    public void faireUnAppelDOffre() {
        // On récupère le superviseur des appels d'offres
        SuperviseurVentesAO superviseurAO = (SuperviseurVentesAO) Filiere.LA_FILIERE.getActeur("Sup.AO");
        
        // On récupère la liste de tous les chocolats du marché
        List<ChocolatDeMarque> produits = Filiere.LA_FILIERE.getChocolatsProduits();

        // on vérifie notre stock pour CHAQUE chocolat
        for (ChocolatDeMarque choco : produits) {
            
            //stock actuel
            double stockActuel = this.stock.getOrDefault(choco, 0.0);
            
            // On se fixe une limite  on veut toujours au moins 50 Tonnes en rayon
            double seuilDeSecurite = 50.0; 

            
            if (stockActuel < seuilDeSecurite) {
                
                
                double quantiteAcheter = 100.0 - stockActuel;
                
                
                if (quantiteAcheter < AppelDOffre.AO_QUANTITE_MIN) {
                    quantiteAcheter = AppelDOffre.AO_QUANTITE_MIN;
                }
                
                this.journal.ajouter("Alerte stock bas pour " + choco.getNom() + ". Lancement d'un AO pour " + quantiteAcheter + "T");
                
                
                OffreVente offreRetenue = superviseurAO.acheterParAO(this, this.cryptogramme, choco, quantiteAcheter);
                
                
                if (offreRetenue != null) {
                    this.journal.ajouter("Victoire : On a acheté à " + offreRetenue.getVendeur().getNom() + " pour " + offreRetenue.getPrixT() + "€/T");
                    
                    this.stock.put(choco, stockActuel + quantiteAcheter);
                    this.indicateurStockTotal.setValeur(this, getStockTotal());
                } else {
                    this.journal.ajouter("Échec : Aucune offre intéressante pour " + choco.getNom());
                }
            }
        }
    }

    //  Le superviseur donne la liste de toutes les propositions de vente
    public OffreVente choisirOV(List<OffreVente> propositions) {
        OffreVente meilleureOffre = null;
        double meilleurPrix = Double.MAX_VALUE;
        
        // On compare les offres concurrentes pour prendre la moins chère 
        for (OffreVente offre : propositions) {
            double prixPropose = offre.getPrixT();
            
            
            // Mais on peut vérifier le prix maximum qu'on est prêt à payer 
            double prixMaxAcceptable = 2500.0; // À ajuster 
            
            if (prixPropose < meilleurPrix && prixPropose <= prixMaxAcceptable) {
                meilleurPrix = prixPropose;
                meilleureOffre = offre;
            }
        }
        
        // On renvoie la meilleure offre au superviseur (ou null si tout le monde est trop cher)
        return meilleureOffre;
    }
}