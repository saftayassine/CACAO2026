package abstraction.eq8Distributeur1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Gamme;
import abstraction.eqXRomu.produits.IProduit;

/** @author Ewen Landron */
public class MiseEnRayon extends AppelOffre {

    public MiseEnRayon() {
        super();
    }
    
    public void miseajour_TailleRayon() {
        double t = this.volumeStock.getValeur();
        this. TailleRayon = t/2;
    }

    public void executerMiseEnRayon() {
        // 1. Définition de l'espace total disponible
        double espaceTotal = this.TailleRayon;
        
        // 2. On traite les 6 catégories symétriquement en utilisant tes variables de Approvisionnement2
        remplirEspaceCategorie(Gamme.BQ, false, espaceTotal * this.pourcentBQ);
        remplirEspaceCategorie(Gamme.BQ, true,  espaceTotal * this.pourcentBQ_E);
        
        remplirEspaceCategorie(Gamme.MQ, false, espaceTotal * this.pourcentMQ);
        remplirEspaceCategorie(Gamme.MQ, true,  espaceTotal * this.pourcentMQ_E);
        
        remplirEspaceCategorie(Gamme.HQ, false, espaceTotal * this.pourcentHQ);
        remplirEspaceCategorie(Gamme.HQ, true,  espaceTotal * this.pourcentHQ_E);
    }

    private void remplirEspaceCategorie(Gamme gamme, boolean equitable, double espaceAlloueCategorie) {
        // 1. Récupérer uniquement les produits de cette catégorie précise présents en stock
        List<ChocolatDeMarque> produitsEnStock = new ArrayList<>();
        for (IProduit p : this.Stock.keySet()) {
            if (p instanceof ChocolatDeMarque) {
                ChocolatDeMarque cdm = (ChocolatDeMarque) p;
                if (cdm.getGamme() == gamme && cdm.isEquitable() == equitable && this.Stock.get(cdm) > 0) {
                    produitsEnStock.add(cdm);
                }
            }
        }

        // 2. Tri au sein de la catégorie (par exemple par quantité croissante pour diversifier les marques en rayon)
        Collections.sort(produitsEnStock, new Comparator<ChocolatDeMarque>() {
            public int compare(ChocolatDeMarque c1, ChocolatDeMarque c2) {
                double qte1 = MiseEnRayon.this.Stock.get(c1);
                double qte2 = MiseEnRayon.this.Stock.get(c2);
                return Double.compare(qte1, qte2);
            }
        });

        // 3. Remplissage de l'espace alloué pour cette catégorie
        double espaceOccupeCategorie = 0;

        for (ChocolatDeMarque cdm : produitsEnStock) {
            // Si le quota de la catégorie est atteint, on arrête pour cette catégorie
            if (espaceOccupeCategorie >= espaceAlloueCategorie) break;

            double resteAFermerCategorie = espaceAlloueCategorie - espaceOccupeCategorie;
            double quantiteEnStock = this.Stock.get(cdm);

            // Sécurité : ne pas dépasser le quota de la catégorie ET la capacité physique globale restante
            double placeRestanteMagasin = Math.max(0, this.TailleRayon - this.volumerayon);
            double aDeplacer = Math.min(quantiteEnStock, Math.min(resteAFermerCategorie, placeRestanteMagasin));

            if (aDeplacer > 0.001) {
                // Utilisation exclusive de AjoutenRayon pour éviter la double soustraction du stock
                // et garantir la mise à jour de volumerayon et volumeStock.
                this.AjoutenRayon(cdm, aDeplacer);
                
                espaceOccupeCategorie += aDeplacer;
            }
        }
    }
}