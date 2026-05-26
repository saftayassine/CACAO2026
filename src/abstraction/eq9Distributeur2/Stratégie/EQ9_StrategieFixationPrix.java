package abstraction.eq9Distributeur2.Stratégie;

import abstraction.eqXRomu.general.Journal;

/**
 * Gère la fixation dynamique des prix de vente EQ9
 * Formule : Prix = Coût × Marge × Facteur_Demande × Facteur_Inventaire × Facteur_Compétition
 * 
 * @author Paul ROSSIGNOL
 */
public class EQ9_StrategieFixationPrix {
    
    // Marges de base par gamme de produit
    private static final double MARGE_GAMME_BASSE = 12.0;    
    private static final double MARGE_GAMME_MOYENNE = 18.0;  
    private static final double MARGE_GAMME_HAUTE = 25.0;   
    private static final double MARGE_MARQUE_PROPRE = 35.0; 
    
    // Seuils d'inventaire
    private static final double OBJECTIF_STOCK_KG = 50000.0;  
    private static final double SEUIL_ALERTE_BAS = 10000.0;  
    private static final double SEUIL_SURSTOCK = 75000.0;      
    
    public EQ9_StrategieFixationPrix(Journal j) {
    }
    
    /**
     * Calcule le prix de vente optimal pour un produit chocolaté
     * 
     * @param coutAchatEuroPT Coût d'achat en €/tonne
     * @param nomProduit Nom du produit (identifie marque)
     * @param inventaireKg Stock actuel en kg
     * @param demandeClients Demande clients estimée pour ce produit
     * @param prixConcurrentEuro Prix du concurrent direct (même gamme/marque)
     * 
     * @return Prix de vente recommandé en €/tonne
     */
    public double calculerPrixVente(
        double coutAchatEuroPT,
        String nomProduit,
        double inventaireKg,
        double demandeClients,
        double prixConcurrentEuro
    ) {
        //DÉTERMINER LA MARGE DE BASE
        double margePercent = obtenirMargeBasePourProduit(nomProduit);
        double prixBase = coutAchatEuroPT * (1.0 + margePercent / 100.0);
        
        //FACTEUR DEMANDE/OFFRE
        double offre = inventaireKg;
        double ratioDemandeOffre = (offre > 0) ? demandeClients / offre : 2.0;
        double facteurDemande = obtenirFacteurDemande(ratioDemandeOffre);
        
        //FACTEUR INVENTAIRE
        double facteurInventaire = obtenirFacteurInventaire(inventaireKg);
        
        //FACTEUR COMPÉTITION DIRECTE
        double facteurCompetition = obtenirFacteurCompetition(
            prixBase * facteurDemande * facteurInventaire, 
            prixConcurrentEuro
        );
        
        //CALCUL FINAL
        double prixFinal = prixBase * facteurDemande * facteurInventaire * facteurCompetition;
        
        return prixFinal;
    }
    
    /**
     marge de base selon le type de produit
     */
    private double obtenirMargeBasePourProduit(String nomProduit) {
        // Marque propre  = marge maximale
        if (nomProduit.contains("EQ9")) {
            return MARGE_MARQUE_PROPRE;  // 35%
        }
        
        // Heuristique gammes (à adapter selon structure réelle)
        if (nomProduit.contains("MQ") || nomProduit.contains("Premium") 
            || nomProduit.contains("Deluxe")) {
            return MARGE_GAMME_HAUTE;    // 25%
        }
        
        if (nomProduit.contains("BQ") || nomProduit.contains("Budget") 
            || nomProduit.contains("Eco")) {
            return MARGE_GAMME_BASSE;    // 12%
        }
        
        // Par défaut = moyenne
        return MARGE_GAMME_MOYENNE;      // 18%
    }
    
    /**
     * Calcule le facteur demande/offre
     * 
     * Ratio = Demande / Offre
     *   > 1.5 : forte demande : prix haut
     *   1.0-1.5 : équilibre normal
     *   0.7-1.0 : légère suroffre : prix réduit
     *   < 0.7 : suroffre massive : prix très réduit
     */
    private double obtenirFacteurDemande(double ratio) {
        if (ratio > 1.5) {
            return 1.20;  // +20% prix (forte demande)
        } else if (ratio > 1.0) {
            return 1.0 + (ratio - 1.0) * 0.2;  // Interpolation +0% à +20%
        } else if (ratio > 0.7) {
            return 1.0 - (1.0 - ratio) * 0.05; // Interpolation -5%
        } else {
            return 0.85;  // -15% prix (surstock)
        }
    }
    
    /**
     * Calcule le facteur inventaire
     * Pénalise ou récompense selon stock vs objectif
     */
    private double obtenirFacteurInventaire(double stockKg) {
        if (stockKg > SEUIL_SURSTOCK) {
            // SURSTOCK massif → urgence déstockage
            return 0.90;  // -10% prix
        } else if (stockKg > OBJECTIF_STOCK_KG * 1.5) {
            // Surstock modéré
            return 0.95;  // -5% prix
        } else if (stockKg < SEUIL_ALERTE_BAS * 0.5) {
            // RUPTURE IMMINENTE → urgence réappro
            return 1.10;  // +10% prix
        } else if (stockKg < SEUIL_ALERTE_BAS) {
            // Seuil de sécurité atteint
            return 1.05;  // +5% prix
        } else {
            // Stock normal = pas d'ajustement
            return 1.0;
        }
    }
    
    /**
     * Calcule le facteur compétition
     * Compare notre prix avec concurrent direct
     */
    private double obtenirFacteurCompetition(double noPrix, double prixConcurrent) {
        if (prixConcurrent <= 0) {
            // Pas de concurrent identifié = on peut garder notre prix
            return 1.0;
        }
        
        double ratio = noPrix / prixConcurrent;
        
        if (ratio > 1.05) {
            // Nous sommes 5% plus chers : réduire pour rester compétitif
            return 0.98;  // -2%
        } else if (ratio > 1.02) {
            // Nous sommes 2% plus chers
            return 0.99;  // -1%
        } else {
            // Nous sommes compétitifs
            return 1.0;
        }
    }
    
    /**
     * Retourne la marge standard pour une gamme
     * Utile pour le monitoring/reporting
     */
    public static double obtenirMargeStandard(String gamme) {
        switch (gamme.toUpperCase()) {
            case "BASSE":
                return MARGE_GAMME_BASSE;
            case "MOYENNE":
                return MARGE_GAMME_MOYENNE;
            case "HAUTE":
                return MARGE_GAMME_HAUTE;
            case "PROPRE":
                return MARGE_MARQUE_PROPRE;
            default:
                return MARGE_GAMME_MOYENNE;
        }
    }
}
