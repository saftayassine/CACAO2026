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
                    double prixAchat = offreRetenue.getPrixT();
                    double prixVente = prix(choco);

                    // On vérifie juste que le prix d'achat est inférieur au prix de vente
                    if (prixAchat >= prixVente) {
                        this.journal.ajouter("Refus : achat à " + prixAchat 
                            + "€/T non rentable (vente à " + prixVente + "€/T)");
                        continue;
                    }

                    this.journal.ajouter("Achat réussi : " + (quantiteAO/1000) + "t de " +
                                       choco.getNom() + " à " + prixAchat + "€/T chez " +
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
        // V1 - Version originale (commentée)
        /*
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
        */

        // V2 - Optimisation des volumes d'achat
        // Intègre : prévision ventes, risque péremption, coûts stockage, fidélisation, valorisation marque
        if (Filiere.LA_FILIERE == null || choco == null) {
            return 0.0;
        }

        final double capaciteStock = 100000.0;
        int etape = Filiere.LA_FILIERE.getEtape();

        // Mise à jour historique ventes (garder 5 dernières étapes)
        historiqueVentes.computeIfAbsent(choco, k -> new java.util.ArrayList<>());
        if (etape >= 1) {
            double ventesEtapePrecedente = Filiere.LA_FILIERE.getVentes(choco, etape - 1);
            historiqueVentes.get(choco).add(ventesEtapePrecedente);
            if (historiqueVentes.get(choco).size() > 5) {
                historiqueVentes.get(choco).remove(0);
            }
        }

        // Prévision ventes : moyenne des ventes passées + tendance
        double ventesPrevues = historiqueVentes.get(choco).stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        if (historiqueVentes.get(choco).size() >= 2) {
            double tendance = (historiqueVentes.get(choco).get(historiqueVentes.get(choco).size() - 1) - 
                              historiqueVentes.get(choco).get(0)) / historiqueVentes.get(choco).size();
            ventesPrevues += tendance;
        }

        // Risque péremption : durée de vie par gamme, réduire quantité si proche péremption
        int dureeVieRestante = dureeVieProduits.getOrDefault(choco.getGamme(), 6);
        double facteurPeremption = Math.max(0.3, dureeVieRestante / 12.0); // Réduire si < 6 étapes

        // Valorisation marque : ajuster selon qualité perçue (produits premium = plus de stock)
        double facteurMarque = 1.0 + (choco.qualitePercue() - 0.5) * 0.5; // Bonus pour marques fortes

        // Fidélisation : bonus pour vendeurs connus (via score fidélité de CC)
        // Ici on suppose un bonus global, à affiner par vendeur dans choisirOV
        double facteurFidelite = 1.1; // Exemple : 10% bonus si fidélisé

        // Calcul EOQ simplifié : sqrt(2 * demande * coût commande / coût stockage)
        double demandeAnnuelle = ventesPrevues * 24; // Approximation sur 24 étapes
        double quantiteEOQ = Math.sqrt(2 * demandeAnnuelle * coutPenurieParTonne / coutStockageParTonne);

        // Stock cible : couverture 4 étapes, ajustée pour facteurs
        double stockCible = ventesPrevues * 4 * facteurPeremption * facteurMarque * facteurFidelite;
        double besoin = Math.max(0.0, stockCible - stockActuel);

        // Prix intéressants si volume important : encourager gros volumes
        double prixMoyen = (etape >= 1) ? Filiere.LA_FILIERE.prixMoyen(choco, etape - 1) : Double.NaN;
        double prixActuel = prix(choco);
        double facteurPrix = (!Double.isNaN(prixMoyen) && prixActuel < prixMoyen) ? 1.3 : 1.0;

        // Quantité finale : min(EOQ, besoin) * facteurs, dans limites capacité
        double marge = Math.max(0.0, capaciteStock - stockActuel);
        double quantite = Math.min(Math.min(quantiteEOQ, besoin) * facteurPrix, marge);

        // Pas de vente à perte : vérifier rentabilité (prix achat estimé < prix vente)
        double prixVenteEstime = prix(choco);
        double prixAchatEstime = prixMoyen; // Approximation
        if (!Double.isNaN(prixAchatEstime) && prixAchatEstime >= prixVenteEstime) {
            quantite *= 0.5; // Réduire si risque vente à perte
        }

        // Éviter surstock près capacité max
        double stockMax = capaciteStock * 0.9;
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