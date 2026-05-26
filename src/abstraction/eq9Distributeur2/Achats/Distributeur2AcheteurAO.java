package abstraction.eq9Distributeur2.Achats;

import abstraction.eq9Distributeur2.Config.EQ9Config;
import abstraction.eq9Distributeur2.Core.Distributeur2Acteur;
import abstraction.eq9Distributeur2.Stocks.EQ9_GestionStocks;
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

public Distributeur2AcheteurAO(){
    super();
}
    //  recherche

    protected double restantDu(abstraction.eqXRomu.produits.IProduit produit){
        // déléguer à la logique CC si disponible
        if (this instanceof Distributeur2AcheteurCC) {
            return ((Distributeur2AcheteurCC)this).restantDu(produit);
        }
        return 0.0;
    }
    public void faireUnAppelDOffre() {
        EQ9_GestionStocks gs = new EQ9_GestionStocks(this.stock, this::restantDu);

        SuperviseurVentesAO superviseurAO = (SuperviseurVentesAO) Filiere.LA_FILIERE.getActeur("Sup.AO");
        if (superviseurAO == null) {
            this.journalAO.ajouter("Sup.AO introuvable : aucun AO cette étape");
            return;
        }

        List<ChocolatDeMarque> produitsFiliere = Filiere.LA_FILIERE.getChocolatsProduits();
        if (produitsFiliere == null || produitsFiliere.isEmpty()) {
            return;
        }

        double solde = getSolde();
        if (solde < EQ9Config.CASH_BUFFER_MIN) {
            this.journalAO.ajouter("Cash < buffer minimal (" + solde + "€) : pas d'AO cette étape");
            return;
        }

        for (ChocolatDeMarque choco : produitsFiliere) {
            if (!gs.doitAcheter(choco)) continue;
            if (!gs.prefererAO(choco)) continue; // CC gère le reste

            double stockActuel = this.stock.getOrDefault(choco, 0.0);
            double enCoursCC = restantDu(choco); // livraisons CC à venir
            double stockProjete = stockActuel + enCoursCC;

            double seuilMin = EQ9Config.SEUIL_MIN_T;
            double stockCible = EQ9Config.STOCK_CIBLE_T;

            double quantiteAO = 0.0;

            if (stockProjete < seuilMin) {
                quantiteAO = stockCible - stockProjete;
                this.journalAO.ajouter("Stock CRITIQUE " + choco.getNom()
                    + " (" + stockActuel + "t actuel, " + stockProjete + "t projeté, en cours CC=" + enCoursCC + "t)"
                    + ": réappro massif (objectif " + stockCible + "t)");
            } else if (stockProjete < stockCible) {
                // Stock bas → réappro agressif (plus que 50%)
                quantiteAO = (stockCible - stockProjete) * 0.7;
                this.journalAO.ajouter("Stock bas " + choco.getNom()
                    + " (" + stockActuel + "t actuel, " + stockProjete + "t projeté)"
                    + ": réappro partiel");
            } else {
                // Stock suffisant : pas d'AO
                continue;
            }

            // Respecter la quantité minimum des AO
            double qMinAO = Math.max(AppelDOffre.AO_QUANTITE_MIN, EQ9Config.MIN_ACHAT_AO_T);
            if (quantiteAO < qMinAO) {
                quantiteAO = qMinAO;
            }

            // Vérifier qu'on a les fonds suffisants
            double prixEstime = prix(choco);
            double coutEstime = quantiteAO * prixEstime;
            if (solde < coutEstime) {
                this.journalAO.ajouter("Fonds insuffisants pour " + choco.getNom()
                    + " : solde=" + solde + "€, besoin≈" + coutEstime + "€ pour " + quantiteAO + "t");
                continue;
            }

            OffreVente offreRetenue = superviseurAO.acheterParAO(
                this, this.cryptogramme, choco, quantiteAO);

            if (offreRetenue != null) {
                double prixAchat = offreRetenue.getPrixT();
                double quantiteAchetee = offreRetenue.getQuantiteT();
                double stockActuelApresAchat = this.stock.getOrDefault(choco, 0.0);
                this.stock.put(choco, stockActuelApresAchat + quantiteAchetee);
                this.indicateurStockTotal.setValeur(this, getStockTotal());

                this.journalAO.ajouter("Achat AO : " + quantiteAchetee + "t de "
                    + choco.getNom() + " à " + prixAchat + "€/T chez "
                    + offreRetenue.getVendeur().getNom()
                    + " (stock total=" + this.stock.get(choco) + "t)");
            } else {
                this.journalAO.ajouter("Aucune offre acceptable pour " + choco.getNom());
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

        double stockActuel = this.stock.getOrDefault(choco, 0.0);
        double prixMaxAcceptable;
        if (stockActuel < 10.0) {
            prixMaxAcceptable = prix(choco); // Accepter de descendre à 0% de marge si stock critique, mais jamais de vente à perte
        } else {
            prixMaxAcceptable = prix(choco) * 0.95; // Exiger au moins 5% de marge en temps normal
        }

        // Rejeter si trop cher
        if (prixPropose > prixMaxAcceptable) {
            this.journalAO.ajouter("Offre rejetée (trop chère) : "
                + choco.getNom() + " à " + prixPropose + "€/T (max=" + prixMaxAcceptable + ")");
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