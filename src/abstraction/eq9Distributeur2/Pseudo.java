package abstraction.eq9Distributeur2;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.ChocolatDeMarque;

/**
 * @author Paul Rossignol
 */
public class Pseudo {

    /**
     * Analyse du retour sur investissement des labels – V1 .
     * On achète un produit labellisé seulement si la marge couvre
     * les coûts de certification (avec une petite marge de sécurité).
     */
    public static class AnalyseROILabelV1 {
        private static final double MARGE_SECURITE = 1.10; // ex : demander 10% de plus que le coût

        /**
         * @param produit          produit concerné 
         * @param prixAchat        prix d'achat du produit labellisé
         * @param prixVente        prix de vente au client final
         * @param coutCertification coût additionnel lié au label (par unité)
         * @param attractivite     facteur d'attractivité (>1 si le label permet de mieux vendre)
         * @return true si l'achat du label est jugé rentable
         */
        public boolean acheterLabel(ChocolatDeMarque produit,
                                    double prixAchat,
                                    double prixVente,
                                    double coutCertification,
                                    double attractivite) {
            if (produit == null || Filiere.LA_FILIERE == null) {
                return false;
            }
            if (prixAchat < 0.0 || prixVente <= 0.0 || coutCertification < 0.0) {
                return false;
            }
            if (attractivite <= 0.0) {
                attractivite = 1.0;
            }

            double margeUnitaire = (prixVente - prixAchat) * attractivite;
            double coutTotal = coutCertification * MARGE_SECURITE;

            return margeUnitaire >= coutTotal;
        }
    }
}
