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
            
            // Seuil de sécurité réaliste : 10 tonnes minimum en stock - Arbitraire à revoir
            double seuilDeSecurite = 10000.0; 
            
            if (stockActuel < seuilDeSecurite) {
                
                // Quantité à acheter : viser 50 tonnes total en stock
                double quantiteCible = 50000.0; // 50 tonnes
                double quantiteAcheter = quantiteCible - stockActuel;
                
                // Respecter la quantité minimum pour les appels d'offres
                if (quantiteAcheter < AppelDOffre.AO_QUANTITE_MIN) {
                    quantiteAcheter = AppelDOffre.AO_QUANTITE_MIN;
                }
                
                this.journal.ajouter("Alerte stock bas pour " + choco.getNom() + 
                                   " (" + (stockActuel/1000) + "t). Lancement d'AO pour " + 
                                   (quantiteAcheter/1000) + "t");
                
                double quantiteAO = calculerQuantiteAchatVolume(choco, stockActuel);
                if (quantiteAO < AppelDOffre.AO_QUANTITE_MIN) {
                    continue;
                }

                OffreVente offreRetenue = superviseurAO.acheterParAO(this, this.cryptogramme, choco, quantiteAO);
                
                if (offreRetenue != null) {
                    // Vérifier la rentabilité du label
                    Distributeur2Acteur.AnalyseROILabelV1 analyseur = new Distributeur2Acteur.AnalyseROILabelV1();
                    double prixVente = prix(choco);
                    double prixAchat = offreRetenue.getPrixT();
                    double coutCertification = 1000.0; // Coût additionnel de certification (€/T)
                    double attractivite = 1.15; // Le label améliore légèrement les ventes
                    
                    boolean labelRentable = analyseur.acheterLabel(choco, prixAchat, prixVente, coutCertification, attractivite);
                    
                    if (!labelRentable) {
                        this.journal.ajouter("Refus : label non rentable pour " + choco.getNom() + " à " + offreRetenue.getPrixT() + "€/T");
                        continue;
                    }
                    
                    this.journal.ajouter("Achat réussi : " + (quantiteAO/1000) + "t de " + 
                                       choco.getNom() + " à " + offreRetenue.getPrixT() + "€/T chez " + 
                                       offreRetenue.getVendeur().getNom());
                    
                    this.stock.put(choco, stockActuel + quantiteAO);
                    this.indicateurStockTotal.setValeur(this, getStockTotal());
                } else {
                    this.journal.ajouter("Échec : Aucune offre acceptable pour " + choco.getNom());
                }
            }
        }
    }

    /**
     * @author Anass OUISRANI 
     * @author Paul ROSSIGNOL
     */
    protected double calculerQuantiteAchatVolume(ChocolatDeMarque choco, double stockActuel) {
        if (Filiere.LA_FILIERE == null || choco == null) {
            return 0.0;
        }

        final double capaciteStock = 100000.0;
        final int COUVERTURE_STEPS = 6;

        int etape = Filiere.LA_FILIERE.getEtape();
        double ventesRecentes = (etape >= 1) ? Filiere.LA_FILIERE.getVentes(choco, etape - 1) : 0.0;
        double prixMoyen = (etape >= 1) ? Filiere.LA_FILIERE.prixMoyen(choco, etape - 1) : Double.NaN;

        double stockCible = ventesRecentes * COUVERTURE_STEPS;
        double besoin = Math.max(0.0, stockCible - stockActuel);
        double marge = Math.max(0.0, capaciteStock - stockActuel);
        double quantite = Math.min(besoin, marge);

        double prixActuel = prix(choco);
        if (!Double.isNaN(prixMoyen) && prixActuel < prixMoyen) {
            quantite = Math.min(quantite * 1.5, marge);
        }

        double stockMax = capaciteStock * 0.95;
        if (stockActuel >= stockMax) {
            return 0.0;
        }

        return Math.max(0.0, quantite);
    }

    //  Le superviseur donne la liste de toutes les propositions de vente
    @Override
    public OffreVente choisirOV(List<OffreVente> propositions) {
        OffreVente meilleureOffre = null;
        double meilleurScore = Double.MAX_VALUE;
        
        // Prix maximum acceptable selon la qualité du chocolat demandé
        double prixMaxAcceptable = 30000.0; // 30 000 €/T maximum
        
        // On compare les offres concurrentes pour prendre la meilleure valeur prix/volume/conditions
        for (OffreVente offre : propositions) {
            double prixPropose = offre.getPrixT();
            double volume = offre.getOffre().getQuantiteT();
            double penalites = evaluerConditionsOffre(offre);
            double score = (prixPropose / Math.max(1.0, volume)) + penalites;

            if (score < meilleurScore && prixPropose <= prixMaxAcceptable) {
                meilleurScore = score;
                meilleureOffre = offre;
            }
        }
        
        if (meilleureOffre != null) {
            this.journal.ajouter("Offre sélectionnée : " + meilleureOffre.getPrixT() + "€/T pour " + 
                               (meilleureOffre.getQuantiteT()) + "t de " + 
                               ((ChocolatDeMarque)meilleureOffre.getProduit()).getNom());
        } else {
            this.journal.ajouter("Aucune offre acceptable (prix > " + prixMaxAcceptable + "€/T)");
        }
        
        return meilleureOffre;
    }

    /**
     * @author Paul ROSSIGNOL
     */
    protected double evaluerConditionsOffre(OffreVente offre) {
        if (offre == null) {
            return 1000.0;
        }
        // Pénalités de base : plus la quantité est faible, plus c'est coûteux (frais fixes)
        double volume = offre.getOffre().getQuantiteT();
        double penaliteVolume = (volume <= 0) ? 100.0 : 50.0 / volume;

        // Si vendeur déjà connu, on pourrait appliquer une prime de fidélité via CC
        // (rien de dispo ici, on garde simple)
        return penaliteVolume;
    }
}