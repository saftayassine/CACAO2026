package abstraction.eq9Distributeur2;

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

    }