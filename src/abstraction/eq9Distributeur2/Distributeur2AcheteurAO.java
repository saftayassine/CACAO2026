package abstraction.eq9Distributeur2;

import abstraction.eqXRomu.appelDOffre.AppelDOffre;
import abstraction.eqXRomu.appelDOffre.IAcheteurAO;
import abstraction.eqXRomu.appelDOffre.OffreVente;
import abstraction.eqXRomu.appelDOffre.SuperviseurVentesAO;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import java.util.List;

    /**  
	 * @author Paul ROSSIGNOL
     * @author Anass OUISRANI 
    */
public class Distributeur2AcheteurAO extends Distributeur2Acteur implements IAcheteurAO {

    // Variables pour V2 : optimisation des volumes d'achat
    protected java.util.Map<ChocolatDeMarque, java.util.List<Double>> historiqueVentes = new java.util.HashMap<>();
    protected double coutStockageParTonne = 500.0; // €/t/étape
    protected double coutPenurieParTonne = 2000.0; // €/t pour rupture
    protected java.util.Map<abstraction.eqXRomu.produits.Gamme, Integer> dureeVieProduits = java.util.Map.of(
        abstraction.eqXRomu.produits.Gamme.HQ, 12,
        abstraction.eqXRomu.produits.Gamme.MQ, 8,
        abstraction.eqXRomu.produits.Gamme.BQ, 6
    ); // Étapes avant péremption
public Distributeur2AcheteurAO(){
    super();
}
    //  recherche

    protected double restantDu(abstraction.eqXRomu.produits.IProduit produit){
        return 0.0;
    }
    public void faireUnAppelDOffre() {
        SuperviseurVentesAO superviseurAO = (SuperviseurVentesAO) Filiere.LA_FILIERE.getActeur("Sup.AO");
        List<ChocolatDeMarque> produits = Filiere.LA_FILIERE.getChocolatsProduits();

        if (produits == null || produits.isEmpty()) {
            return;
        }

        for (ChocolatDeMarque choco : produits) {
            double stockActuel = this.stock.getOrDefault(choco, 0.0);
            double stockProjete = stockActuel ;
            double seuilMin = 10.0;    // 10 tonnes : seuil minimum 
            double stockCible = 50.0;  // 50 tonnes : stock visé

            // Calculer la quantité à acheter en tenant compte des livraisons CC déjà prévues
            double quantiteAO = 0.0;
            if (stockProjete < seuilMin) {
                quantiteAO = stockCible - stockProjete;
                this.journalAO.ajouter("Stock critique pour " + choco.getNom() 
                    + " (" + (stockActuel) + "t actuel, " + (stockProjete) + "t projeté) → réappro obligatoire");
            } else if (stockProjete < stockCible) {
                quantiteAO = (stockCible - stockProjete) * 0.5;
                this.journalAO.ajouter("Stock bas pour " + choco.getNom() 
                    + " (" + (stockActuel) + "t actuel, " + (stockProjete) + "t projeté) → réappro partiel");
            } else {
                continue;
            }

            // Respecter la quantité minimum des AO
            if (quantiteAO < AppelDOffre.AO_QUANTITE_MIN) {
                quantiteAO = AppelDOffre.AO_QUANTITE_MIN;
            }

            // Vérifier qu'on a les fonds suffisants
            double prixEstime = prix(choco);
            double coutEstime = (quantiteAO) * prixEstime * 0.75;
            if (getSolde() < coutEstime) {
                this.journalAO.ajouter("Fonds insuffisants pour " + choco.getNom() 
                    + " : solde=" + getSolde() + "€, besoin=" + coutEstime + "€");
                continue;
            }

            OffreVente offreRetenue = superviseurAO.acheterParAO(
                this, this.cryptogramme, choco, quantiteAO);

            if (offreRetenue != null) {
                double prixAchat = offreRetenue.getPrixT();
                double quantiteAchetee = offreRetenue.getQuantiteT();
                double stockActuelApreAchat = this.stock.getOrDefault(choco, 0.0);
                this.stock.put(choco, stockActuelApreAchat + quantiteAchetee);
                this.indicateurStockTotal.setValeur(this, getStockTotal());

                this.journalAO.ajouter("Achat réussi : " + (quantiteAchetee) + "t de "
                    + choco.getNom() + " à " + prixAchat + "€/T chez "
                    + offreRetenue.getVendeur().getNom());
            } else {
                this.journalAO.ajouter("Aucune offre pour " + choco.getNom());
            }
        }
    }


    @Override
    public OffreVente choisirOV(List<OffreVente> propositions) {
    if (propositions == null || propositions.isEmpty()) return null;

    OffreVente meilleureOffre = null;
    double meilleurPrix = Double.MAX_VALUE;

    

    for (OffreVente offre : propositions) {
        double prixPropose = offre.getPrixT();
        ChocolatDeMarque choco = (ChocolatDeMarque) offre.getProduit();

        double margeMin = 1.2; // 20% de marge minimale
        double prixMaxAcceptable = prix(choco) / margeMin;

        // Rejeter si trop cher pour notre marge minimale
        if (prixPropose >= prixMaxAcceptable) continue;

        // Rejeter si vente à perte
        if (prixPropose >= prix(choco)) {
            this.journalAO.ajouter("Offre rejetée (vente à perte) : "
                + choco.getNom() + " à " + prixPropose + "€/T");
            continue;
        }

        //prise en compte de la quantité proposée
        if (offre.getQuantiteT() < AppelDOffre.AO_QUANTITE_MIN)
            continue;

        // Garder la moins chère
        if (prixPropose < meilleurPrix) {
            meilleurPrix = prixPropose;
            meilleureOffre = offre;
        }
    }

    if (meilleureOffre != null) {
        ChocolatDeMarque choco = (ChocolatDeMarque) meilleureOffre.getProduit();
        this.journalAO.ajouter("Offre retenue : " + meilleurPrix 
            + "€/T de " + choco.getMarque()
            + " (marge=" + (prix(choco) - meilleurPrix) + "€/T)");
    } else {
        this.journalAO.ajouter("Aucune offre acceptable");
    }

    return meilleureOffre;
}

    
}