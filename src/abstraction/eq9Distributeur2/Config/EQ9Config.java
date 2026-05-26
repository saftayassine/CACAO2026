package abstraction.eq9Distributeur2.Config;

/**
 * @author Paul JUHEL
 * Centralise les constantes
 * Unités : tonnes pour les quantités (suffixe _T), €/T pour les prix.
 */
public class EQ9Config {
    private EQ9Config() {}


    // Coûts et frais (€/tonne)
    public static final double FRAIS_STOCKAGE_EUR_PAR_T = 120.0;

    // pas encore utilisé
    public static final double MIX_MARQUE_PROPRE_CIBLE = 0.55;

    // Gestion des stocks (TONNES)
    public static final double SEUIL_MIN_T = 5000.0;        // sous-stock critique
    public static final double STOCK_CIBLE_T = 30000.0;     // objectif normal
    public static final double SEUIL_SOUS_STOCK_T = 15000.0; // tension
    public static final double SEUIL_SURSTOCK_T = 60000.0;   // surstock

    // Contrats Cadres 
    public static final double CC_QUANTITE_MIN_T = 10000.0;  

    // Capacité rayon 
    public static final double CAPACITE_RAYON_T = 50000.0;   

    // Stratégie 
    public static final double PART_MARCHE_CIBLE = 0.40;    
    public static final double MARGE_BRUTE_MIN = 0.15;
    public static final double CASH_BUFFER_MIN = 2_000_000.0;

    // AO 
    public static final double MIN_ACHAT_AO_T = 100.0;     
    
    // Prix de vente
    public static final double MARGE_SECURITE_MIN = 1.02; // +2% sur le coût d'achat

}