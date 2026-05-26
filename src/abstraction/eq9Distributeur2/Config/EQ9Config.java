package abstraction.eq9Distributeur2.Config;

/**
 * @author Paul JUHEL
 * Centralise les constantes
 * Unités : tonnes pour les quantités (suffixe _T), kg si précisé (suffixe _KG), €/T pour les prix.
 */
public class EQ9Config {
    private EQ9Config() {}

    // Stocks et seuils (en tonnes)
    public static final double SEUIL_MIN_T = 10.0;
    public static final double STOCK_CIBLE_T = 50.0;
    public static final double CC_QUANTITE_MIN_T = 100.0;

    // Coûts et frais (€/tonne)
    public static final double FRAIS_STOCKAGE_EUR_PAR_T = 120.0;
    public static final double COUT_STOCKAGE_PAR_TONNE = 500.0;
    public static final double COUT_PENURIE_PAR_TONNE = 2000.0;

    // Stratégie
    public static final double PART_MARCHE_CIBLE = 0.45;      // 45%
    public static final double MIX_MARQUE_PROPRE_CIBLE = 0.55;
    public static final double MARGE_BRUTE_MIN = 0.15;        // 15%
    public static final double CASH_BUFFER_MIN = 2_000_000.0; // €

    // Capacités et limites
    public static final double CAPACITE_RAYON_T = 500.0;

    // Paramètres AO/CC
    public static final double MIN_ACHAT_AO_T = 1.0;
}
