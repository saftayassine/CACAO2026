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

        // --- ÉTAPE 1 : MISE EN RAYON CLASSIQUE (Priorité aux volumes de base) ---
        // 1. Récupérer les produits en stock pour cette catégorie exacte
        List<ChocolatDeMarque> produitsDansCategorie = new ArrayList<>();
        for (IProduit p : this.Stock.keySet()) {
            if (p instanceof ChocolatDeMarque) {
                ChocolatDeMarque cdm = (ChocolatDeMarque) p;
                if (cdm.getGamme() == gamme && cdm.isEquitable() == equitable && this.Stock.getOrDefault(cdm, 0.0) > 0) {
                    produitsDansCategorie.add(cdm);
                }
            }
        }

        // 2. Tri pour diversifier les marques
        Collections.sort(produitsDansCategorie, new Comparator<ChocolatDeMarque>() {
            public int compare(ChocolatDeMarque c1, ChocolatDeMarque c2) {
                return Double.compare(MiseEnRayon.this.Stock.getOrDefault(c1, 0.0), MiseEnRayon.this.Stock.getOrDefault(c2, 0.0));
            }
        });

        // 3. Remplissage initial en rayon classique
        for (ChocolatDeMarque cdm : produitsDansCategorie) {
            if (espaceOccupeCategorie >= espaceAlloueCategorie) break;

            double resteAFermerCategorie = espaceAlloueCategorie - espaceOccupeCategorie;
            double quantiteEnStock = this.Stock.getOrDefault(cdm, 0.0);
            double placeRestanteMagasin = Math.max(0, this.TailleRayon - this.volumerayon);

            // On ne remplit ici que l'espace classique
            double aDeplacerClassique = Math.min(quantiteEnStock, Math.min(resteAFermerCategorie, placeRestanteMagasin));

            if (aDeplacerClassique > 0.001) {
                this.AjoutenRayon(cdm, aDeplacerClassique);
                espaceOccupeCategorie += aDeplacerClassique;
            }
        }

        // --- ÉTAPE 2 : AJOUT DES TÊTES DE GONDOLE (TG) ---
        // On ne traite la TG que si le quota de la catégorie n'est pas saturé par le stock classique
        if (espaceOccupeCategorie < espaceAlloueCategorie) {
            
            // Dynamic TG Cap : 10% de ce qui est REELLEMENT en rayon en ce moment (this.volumerayon)
            // moins ce qui occupe déjà l'espace TG global (getQuantiteTotaleTG())
            double limiteDynamiqueTG = (this.volumerayon * 0.1) - this.getQuantiteTotaleTG();
            
            if (limiteDynamiqueTG > 0.001) {
                for (ChocolatDeMarque cdm : this.stockPreditTG.keySet()) {
                    if (espaceOccupeCategorie >= espaceAlloueCategorie || limiteDynamiqueTG <= 0.001) break;

                    if (cdm.getGamme() == gamme && cdm.isEquitable() == equitable) {
                        double quantiteTGPredite = this.stockPreditTG.getOrDefault(cdm, 0.0);
                        
                        // Sécurité critique : On vérifie le stock REEL restant dans le dictionnaire 
                        // après les retraits de l'étape 1
                        double quantiteEnStockReelRestant = this.getQuantiteEnStock(cdm, this.cryptogramme);
                        
                        // On plafonne selon : le contrat, le stock physique restant, la place de la catégorie et la règle des 10%
                        double resteAFermerCategorie = espaceAlloueCategorie - espaceOccupeCategorie;
                        double placeRestanteMagasin = Math.max(0, this.TailleRayon - this.volumerayon);
                        
                        double aMettreEnTG = Math.min(quantiteTGPredite, quantiteEnStockReelRestant);
                        aMettreEnTG = Math.min(aMettreEnTG, Math.min(resteAFermerCategorie, Math.min(placeRestanteMagasin, limiteDynamiqueTG)));

                        if (aMettreEnTG > 0.001) {
                            this.AjoutenRayonTG(cdm, aMettreEnTG);
                            
                            espaceOccupeCategorie += aMettreEnTG;
                            limiteDynamiqueTG -= aMettreEnTG; // On réduit la marge TG disponible pour le prochain produit
                        }
                    }
                }
            }
        }
    }
}

