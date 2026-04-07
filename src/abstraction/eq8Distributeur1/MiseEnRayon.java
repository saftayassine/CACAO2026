package abstraction.eq8Distributeur1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Gamme;
import abstraction.eqXRomu.produits.IProduit;

/** @author Ewen Landron */
public class MiseEnRayon extends ContratCadre2 {

    public MiseEnRayon() {
        super();
    }

    public void executerMiseEnRayon() {
        // 1. Définition des quotas de l'espace total (utilisant l'héritage)
        double espaceTotal = this.TailleRayon;
        
        // 2. On traite chaque gamme séparément avec son budget d'espace
        remplirEspaceGamme(Gamme.BQ, espaceTotal * 0.20);
        remplirEspaceGamme(Gamme.MQ, espaceTotal * 0.45);
        remplirEspaceGamme(Gamme.HQ, espaceTotal * 0.35);
    }

    private void remplirEspaceGamme(Gamme gamme, double espaceAlloueGamme) {
        // 1. Récupérer tous les produits de cette gamme présents en stock
        // Correction : On parcourt en tant que IProduit car c'est le type de clé dans la Map de la filière
        List<ChocolatDeMarque> produitsEnStock = new ArrayList<>();
        for (IProduit p : this.Stock.keySet()) {
            if (p instanceof ChocolatDeMarque) {
                ChocolatDeMarque cdm = (ChocolatDeMarque) p;
                if (cdm.getGamme() == gamme && this.Stock.get(cdm) > 0) {
                    produitsEnStock.add(cdm);
                }
            }
        }

        // 2. Tri selon tes priorités
        Collections.sort(produitsEnStock, new Comparator<ChocolatDeMarque>() {
            public int compare(ChocolatDeMarque c1, ChocolatDeMarque c2) {
                // Priorité 1 : Equitable d'abord
                if (c1.isEquitable() && !c2.isEquitable()) return -1;
                if (!c1.isEquitable() && c2.isEquitable()) return 1;

                // Priorité 2 : Quantité en stock croissante
                // Correction : Utilisation de MiseEnRayon.this pour pointer sur l'acteur hérité
                double qte1 = MiseEnRayon.this.Stock.get(c1);
                double qte2 = MiseEnRayon.this.Stock.get(c2);
                return Double.compare(qte1, qte2);
            }
        });

        // 3. Remplissage de l'espace alloué pour cette gamme
        double espaceOccupeGamme = 0;

        for (ChocolatDeMarque cdm : produitsEnStock) {
            if (espaceOccupeGamme >= espaceAlloueGamme) break;

            double resteAFermerGamme = espaceAlloueGamme - espaceOccupeGamme;
            double quantiteEnStock = this.Stock.get(cdm);

            // Sécurité : ne pas dépasser le quota de gamme ET la capacité physique du magasin
            double placeRestanteMagasin = this.TailleRayon - this.volumerayon;
            double aDeplacer = Math.min(quantiteEnStock, Math.min(resteAFermerGamme, placeRestanteMagasin));

            if (aDeplacer > 0) {
                // Mise à jour du stock interne (this pointe vers l'instance de l'acteur via l'héritage)
                this.Stock.put(cdm, quantiteEnStock - aDeplacer);
                
                // Ajout effectif en rayon (méthode de l'acteur qui met à jour volumerayon)
                this.AjoutenRayon(cdm, aDeplacer);
                
                espaceOccupeGamme += aDeplacer;
            }
        }
    }
}