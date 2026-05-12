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
        double espaceOccupeCategorie = 0;

        // --- ÉTAPE 1 : Mise en rayon des Têtes de Gondole (Priorité Absolue) ---
        // On parcourt le dictionnaire stockPreditTG pour trouver les produits correspondants
        for (ChocolatDeMarque cdm : this.stockPreditTG.keySet()) {
            if (cdm.getGamme() == gamme && cdm.isEquitable() == equitable) {
                double quantiteTG = this.stockPreditTG.get(cdm);
                double quantiteEnStockReel = this.getQuantiteEnStock(cdm, this.cryptogramme);
                
                // On ne peut mettre en TG que ce qu'on a réellement reçu en stock
                double aMettreEnTG = Math.min(quantiteTG, quantiteEnStockReel);

                if (aMettreEnTG > 0.001) {
                    // On utilise ta nouvelle méthode spécifique
                    this.AjoutenRayonTG(cdm, aMettreEnTG);
                    
                    // Très important : les TG consomment le quota d'espace de la catégorie
                    espaceOccupeCategorie += aMettreEnTG;
                }
            }
        }

        // --- ÉTAPE 2 : Mise en rayon classique (Complément) ---
        // Si après les TG il reste de la place pour cette catégorie, on complète avec le stock normal
        if (espaceOccupeCategorie < espaceAlloueCategorie) {
            
            // 1. Récupérer les produits restants en stock pour cette catégorie
            List<ChocolatDeMarque> produitsRestants = new ArrayList<>();
            for (IProduit p : this.Stock.keySet()) {
                if (p instanceof ChocolatDeMarque) {
                    ChocolatDeMarque cdm = (ChocolatDeMarque) p;
                    if (cdm.getGamme() == gamme && cdm.isEquitable() == equitable && this.Stock.get(cdm) > 0) {
                        produitsRestants.add(cdm);
                    }
                }
            }

            // 2. Tri pour diversifier les marques
            Collections.sort(produitsRestants, new Comparator<ChocolatDeMarque>() {
                public int compare(ChocolatDeMarque c1, ChocolatDeMarque c2) {
                    return Double.compare(MiseEnRayon.this.Stock.get(c1), MiseEnRayon.this.Stock.get(c2));
                }
            });

            // 3. Remplissage du reste de l'espace alloué
            for (ChocolatDeMarque cdm : produitsRestants) {
                if (espaceOccupeCategorie >= espaceAlloueCategorie) break;

                double resteAFermerCategorie = espaceAlloueCategorie - espaceOccupeCategorie;
                double quantiteEnStock = this.Stock.get(cdm);
                double placeRestanteMagasin = Math.max(0, this.TailleRayon - this.volumerayon);

                double aDeplacer = Math.min(quantiteEnStock, Math.min(resteAFermerCategorie, placeRestanteMagasin));

                if (aDeplacer > 0.001) {
                    this.AjoutenRayon(cdm, aDeplacer);
                    espaceOccupeCategorie += aDeplacer;
                }
            }
        }
    }
}