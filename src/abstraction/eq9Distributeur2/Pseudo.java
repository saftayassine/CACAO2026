package abstraction.eq9Distributeur2;

import java.util.List;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.ChocolatDeMarque;

/**
 * @author Paul Rossignol
 */
public class Pseudo {

    public static class ActionTeteDeGondole {
        private ChocolatDeMarque chocolat;
        private int quantite;

        public ActionTeteDeGondole(ChocolatDeMarque chocolat, int quantite) {
            this.chocolat = chocolat;
            this.quantite = quantite;
        }

        public ChocolatDeMarque getChocolat() {
            return chocolat;
        }

        public int getQuantite() {
            return quantite;
        }

        
        public void choisirEtPlacerMeilleurProduit(List<ChocolatDeMarque> produits,
                                                  List<Double> prixAchats,
                                                  List<Double> prixVentes,
                                                  List<Integer> volumesDisponibles) {
            if (produits == null || prixAchats == null || prixVentes == null || volumesDisponibles == null) {
                return;
            }
            int n = produits.size();
            if (n == 0 || prixAchats.size() != n || prixVentes.size() != n || volumesDisponibles.size() != n) {
                return;
            }

            double meilleureMarge = Double.NEGATIVE_INFINITY;
            int idxMeilleur = -1;

            for (int i = 0; i < n; i++) {
                ChocolatDeMarque p = produits.get(i);
                Double pa = prixAchats.get(i);
                Double pv = prixVentes.get(i);
                Integer vol = volumesDisponibles.get(i);

                if (p == null || pa == null || pv == null || vol == null) {
                    continue;
                }
                if (pa < 0.0 || pv <= 0.0) {
                    continue;
                }

                double marge = pv - pa;
                if (marge > meilleureMarge) {
                    meilleureMarge = marge;
                    idxMeilleur = i;
                }
            }

            if (idxMeilleur >= 0) {
                this.chocolat = produits.get(idxMeilleur);
                this.quantite = volumesDisponibles.get(idxMeilleur);
            }
        }
    }
}
        