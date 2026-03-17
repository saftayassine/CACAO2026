package abstraction.eq9Distributeur2;

import java.util.List;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.ChocolatDeMarque;

public class Pseudo {
    


    // Stratégie de choix du fournisseur :
    // Nous choisissons toujours le transformateur qui propose le prix le plus bas,
    // en vérifiant les volumes disponibles et quelques conditions de base du contrat,
    // afin de réduire nos coûts d'achat et d'augmenter notre marge.
    // La mise en concurrence via différents protocoles sera implémentée en V2.
    
    /** @author Paul Juhel */
    public Transformateur choisirFournisseur(List<Transformateur> transformateursDisponibles, Produit produit, double quantiteNecessaire) {
        Transformateur meilleurFournisseur = null;
        double meilleurPrix = Double.MAX_VALUE;
        
        for (Transformateur t : transformateursDisponibles) {
            // Vérifier si le transformateur a du volume disponible
            double volumeDisponible = t.getStock(produit);
            if (volumeDisponible >= quantiteNecessaire) {
                // Prix
                double prixPropose = t.getPrix(produit, quantiteNecessaire); 
                
                // Évaluer les conditions du contrat ATTENTION V1/V2
                double penalites = evaluerConditionsContrat(t, produit, quantiteNecessaire);
                
                // Prix total 
                double prixTotal = prixPropose + penalites;
                
                // Choix transformateur
                if (prixTotal < meilleurPrix) {
                    meilleurPrix = prixTotal;
                    meilleurFournisseur = t;
                }
            }
        }
        
        return meilleurFournisseur;
    }
    
    /** @author Paul Rossignol */
    private double evaluerConditionsContrat(Transformateur t, Produit produit, double quantite) {
        double penalites = 0.0;
        
        // Délai
        int delaiLivraison = t.getDelaiLivraison(produit); // en jours
        int delaiMaxAcceptable = 30; // jours
        if (delaiLivraison > delaiMaxAcceptable) {
            // Pénalité simple : 0.01€ par jour de retard
            penalites += (delaiLivraison - delaiMaxAcceptable) * 0.01;
        }
        
        // Qualité
        double qualite = t.getQualite(produit); // entre 0 et 1
        double qualiteMinimale = 0.8;
        if (qualite < qualiteMinimale) {
            penalites += (qualiteMinimale - qualite) * 50.0;
        }
        
        
        return penalites;
    }
    
    
    // V2 -  Mise en concurrence des transformateurs :
    // Nous utiliserons les différents protocoles (contrats cadres, appels d'offres, enchères) 
    // pour mettre les transformateurs en concurrence, en nous appuyant sur l'historique 
    // des négociations, les prix moyens et les offres concurrentes, afin d'obtenir 
    // des conditions d'achat plus avantageuses.
    
    // Méthodes V2 à ajouter :
    // - analyserHistoriqueNegotiations()
    // - comparerPrixMoyens()
    // - evaluerOffresConcurrentes()
    // - lancerAppelOffres()
    // - gererEncheres()
    // - negocierContratsCadres()



    // Fidélité :
    // Nous maintenons un fournisseur uniquement si cela nous apporte remises ou avantages,
    // en nous basant sur l’historique des contrats, les remises obtenues et les prix concurrents,
    // afin d’utiliser la fidélité comme levier de négociation.
    
    /** @author Paul Juhel */
    public boolean garderFournisseur(Transformateur t, Produit produit, double quantite, HistoriqueContrats historique, List<Transformateur> concurrents) {
        // Calcul du prix net actuel
        double remiseHistorique = historique.getRemiseMoyenne(t, produit); 
        double prixNetActuel = t.getPrix(produit, quantite) * (1 - remiseHistorique);

        // Meilleur prix concurrent
        double meilleurPrixConcurrent = Double.MAX_VALUE;
        for (Transformateur c : concurrents) {
            if (!c.equals(t)) {
                double prixC = c.getPrix(produit, quantite);
                meilleurPrixConcurrent = Math.min(meilleurPrixConcurrent, prixC);
            }
        }

        // Si concurrent moins cher (>5%), changement
        double diffRel = (prixNetActuel - meilleurPrixConcurrent) / meilleurPrixConcurrent;
        if (diffRel > 0.05) {
            return false;
        }

        // Calcul de l'avantage fidélité
        double scoreFidelite = historique.getScoreFidelite(t, produit);
        double avantageFidelite = scoreFidelite * 0.1;

        // Avantage attendu par remise future
        double remisePotentielle = historique.getRemisePotentielle(t, produit);

        // Règle opportuniste : conserver si avantage de fidélité ou perspective de remise
        if (remisePotentielle > 0.02) {
            return true;
        }
        if (avantageFidelite > 0.05) {
            return true;
        }

        // Sinon on conserve si l’écart est faible (≤3%) et l’historique est bon
        return diffRel <= 0.03 && scoreFidelite >= 0.7;
    }

   
     // Achats en volume – V1.
     
     // Regle : on vise un stock cible = ventes recentes * COUVERTURE_STEPS.
     // Si le prix actuel est sous le prix moyen, on achete un peu plus.
     /** @author Paul Rossignol */
    public double AchatsEnVolumeV1(ChocolatDeMarque produit, double prixActuel, double stockActuel, double capaciteStock) {
        private static   final int COUVERTURE_STEPS = 6;
        private static final double FACTEUR_PRIX_BAS = 1.50;

        public double quantiteAAcheter(ChocolatDeMarque produit, double prixActuel, double stockActuel, double capaciteStock) {
            if (produit == null || Filiere.LA_FILIERE == null) {
                return 0.0;
            }
            if (prixActuel <= 0.0 || capaciteStock <= 0.0 || stockActuel >= capaciteStock) {
                return 0.0;
            }

            int step = Filiere.LA_FILIERE.getEtape();
            double ventesRecentes = (step >= 1) ? Filiere.LA_FILIERE.getVentes(produit, step - 1) : 0.0;
            double prixMoyen = (step >= 1) ? Filiere.LA_FILIERE.prixMoyen(produit, step - 1) : Double.NaN;

            double stockCible = ventesRecentes * COUVERTURE_STEPS;
            double besoin = Math.max(0.0, stockCible - stockActuel);
            double marge = Math.max(0.0, capaciteStock - stockActuel);
            double quantite = Math.min(besoin, marge);

            if (prixMoyen > 0.0 && prixActuel < prixMoyen && quantite > 0.0) {
                quantite = Math.min(quantite * FACTEUR_PRIX_BAS, marge);
            }

            return Math.max(0.0, quantite);
        }
    }

}