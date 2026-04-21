package abstraction.eq9Distributeur2;

import java.util.List;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.ChocolatDeMarque;

/**
 * @author Paul Rossignol
 */
public class Pseudo {

    /**
     * Action strategique des tetes de gondole.
     * Cette classe place effectivement un produit en tete de gondole.
     */
    public static class ActionTeteDeGondole {
        private static final double SEUIL_ATTRACTIVITE = 1.0;
        private static final double SEUIL_MARGE_MIN = 0.50; // marge unitaire minimale en euros
        private static final int SEUIL_STOCK_MIN = 100; // volume minimum disponible
        private ChocolatDeMarque produitEnTeteDeGondole;

        /**
         * @param produit produit concerne
         * @param prixAchat prix d'achat unitaire
         * @param prixVente prix de vente unitaire
         * @param attractivite attractivite commerciale du produit
         * @param volumeDisponible quantite que l'on peut mettre en rayon
         * @return true si le produit est effectivement place en tete de gondole
         */
        public boolean placerProduitEnTeteDeGondole(ChocolatDeMarque produit,
                                                    double prixAchat,
                                                    double prixVente,
                                                    double attractivite,
                                                    int volumeDisponible) {
            if (produit == null || Filiere.LA_FILIERE == null) {
                return false;
            }
            if (prixAchat < 0.0 || prixVente <= 0.0 || volumeDisponible < 0) {
                return false;
            }
            if (attractivite <= 0.0) {
                attractivite = 1.0;
            }

            double margeUnitaire = prixVente - prixAchat;

            boolean attractif = attractivite >= SEUIL_ATTRACTIVITE;
            boolean margeSuffisante = margeUnitaire >= SEUIL_MARGE_MIN;
            boolean stockSuffisant = volumeDisponible >= SEUIL_STOCK_MIN;

            if (attractif && margeSuffisante && stockSuffisant) {
                this.produitEnTeteDeGondole = produit;
                return true;
            }
            return false;
        }

        /**
         * Selectionne et place le meilleur produit parmi une liste de candidats.
         * Le score est base sur : marge * attractivite * volume.
         */
        public ChocolatDeMarque choisirEtPlacerMeilleurProduit(List<ChocolatDeMarque> produits,
                                                                List<Double> prixAchats,
                                                                List<Double> prixVentes,
                                                                List<Double> attractivites,
                                                                List<Integer> volumesDisponibles) {
            if (produits == null || prixAchats == null || prixVentes == null
                    || attractivites == null || volumesDisponibles == null) {
                return null;
            }
            int n = produits.size();
            if (n == 0 || prixAchats.size() != n || prixVentes.size() != n
                    || attractivites.size() != n || volumesDisponibles.size() != n) {
                return null;
            }

            double meilleurScore = -1.0;
            ChocolatDeMarque meilleurProduit = null;

            for (int i = 0; i < n; i++) {
                ChocolatDeMarque p = produits.get(i);
                double pa = prixAchats.get(i);
                double pv = prixVentes.get(i);
                double a = attractivites.get(i);
                int v = volumesDisponibles.get(i);

                boolean place = placerProduitEnTeteDeGondole(p, pa, pv, a, v);
                if (!place) {
                    continue;
                }

                double marge = pv - pa;
                double score = marge * a * v;
                if (score > meilleurScore) {
                    meilleurScore = score;
                    meilleurProduit = p;
                }
            }

            this.produitEnTeteDeGondole = meilleurProduit;
            return meilleurProduit;
        }

        public ChocolatDeMarque getProduitEnTeteDeGondole() {
            return this.produitEnTeteDeGondole;
        }

        public void retirerProduitDeTeteDeGondole() {
            this.produitEnTeteDeGondole = null;
        }
    }
}

