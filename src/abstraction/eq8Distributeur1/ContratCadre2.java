package abstraction.eq8Distributeur1;

import java.util.List;
import java.awt.Color;

import abstraction.eqXRomu.contratsCadres.Echeancier;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.contratsCadres.IAcheteurContratCadre;
import abstraction.eqXRomu.contratsCadres.IVendeurContratCadre;
import abstraction.eqXRomu.contratsCadres.SuperviseurVentesContratCadre;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.IProduit;

/** @author Ewen Landron */
public class ContratCadre2 extends Approvisionnement implements IAcheteurContratCadre {
    
    // Variables de session (redéfinies à chaque contrat)
    private double besoinCourant;
    private double prixCibleCourant;
    private double prixMaxCourant;
    
    // Flag d'initiative (géré dans le next() d'Approvisionnement2)
    protected boolean lancement_CC;

    public ContratCadre2() {
        super();
        this.lancement_CC = false;
    }

    /**
     * Gère l'initialisation des paramètres de négociation.
     * Si nous sommes à l'initiative, les valeurs ont été fixées par methodeIntermediaireAchat.
     * Sinon, on les calcule à la volée via les données d'Approvisionnement2.
     */
    private void verifierEtInitialiserParametres(ExemplaireContratCadre contrat) {
        if (!this.lancement_CC) {
            // Cast explicite en IProduit pour corriger le Type Mismatch
            IProduit p = (IProduit) contrat.getProduit();
            
            // On récupère le prix de vente estimé dans la classe parente
            this.prixCibleCourant = this.prixDAchat.getOrDefault(p, 1000.0);
            this.prixMaxCourant = this.prixCibleCourant * 1.5;
            
            // On définit un besoin par défaut (10 tonnes par défaut)
            this.besoinCourant = 10000.0; 
        }
    }


    @Override
    protected void methodeIntermediaireAchat(ChocolatDeMarque cdm, double besoinParEtape, double prixCible, double prixMax) {
        this.besoinCourant = besoinParEtape;
        this.prixCibleCourant = prixCible;
        this.prixMaxCourant = prixMax;
        this.lancement_CC = true; // On marque qu'on est à l'initiative

        SuperviseurVentesContratCadre sup = (SuperviseurVentesContratCadre) (Filiere.LA_FILIERE.getActeur("Sup.CCadre"));
        List<IVendeurContratCadre> vendeurs = sup.getVendeurs(cdm);

        if (vendeurs.size() > 0) {
            Echeancier ech = new Echeancier(Filiere.LA_FILIERE.getEtape() + 1, 12, besoinParEtape);
            // On lance la négociation
            sup.demandeAcheteur(this, vendeurs.get(0), cdm, ech, this.cryptogramme, false);
        }
        this.lancement_CC = false; // Reset après la négociation
    }

    public boolean achete(IProduit produit) {
        // On accepte de discuter pour tout chocolat de marque
        return produit instanceof ChocolatDeMarque;
    }

    public Echeancier contrePropositionDeLAcheteur(ExemplaireContratCadre contrat) {
        // Sécurité : on initialise si c'est une demande entrante
        this.verifierEtInitialiserParametres(contrat);
        
        if (this.besoinCourant <= 0) return null;

        Echeancier echVendeur = contrat.getEcheancier();
        Echeancier echReponse = new Echeancier(echVendeur.getStepDebut());

        for (int step = echVendeur.getStepDebut(); step <= echVendeur.getStepFin(); step++) {
            double qteVendeur = echVendeur.getQuantite(step);

            if (qteVendeur > this.besoinCourant) {
                echReponse.set(step, this.besoinCourant);
            } else if (Math.abs(qteVendeur - this.besoinCourant) < 0.01) {
                echReponse.set(step, qteVendeur);
            } else {
                // Stratégie du milieu
                echReponse.set(step, (qteVendeur + this.besoinCourant) / 2.0);
            }
        }
        return echReponse;
    }

    public double contrePropositionPrixAcheteur(ExemplaireContratCadre contrat) {
        // Pas besoin d'initialiser ici, contrePropositionDeLAcheteur est toujours appelée avant par le superviseur
        double pVendeur = contrat.getPrix();

        if (pVendeur <= this.prixCibleCourant * 0.9) {
            return pVendeur;
        }

        double debutNego = this.prixCibleCourant * 0.9;
        double margeTotale = this.prixMaxCourant - debutNego;
        int tourDeNego = contrat.getListePrix().size() / 2;

        double nouvelleOffre = debutNego + (tourDeNego * (margeTotale / 10.0));

        if (tourDeNego >= 10) {
            return (pVendeur <= this.prixMaxCourant) ? pVendeur : -1.0;
        }

        if (nouvelleOffre >= pVendeur) {
            return pVendeur;
        }

        return nouvelleOffre;
    }

    public void notificationNouveauContratCadre(ExemplaireContratCadre contrat) {
        IProduit p = (IProduit) contrat.getProduit();
        if (p instanceof ChocolatDeMarque) {
            ChocolatDeMarque cdm = (ChocolatDeMarque) p;
        
            // Ajout du contrat à la liste si pas déjà présent
            if (!this.mesContrats.contains(contrat)) {
                this.mesContrats.add(contrat);
            }

            // MISE À JOUR DU PRIX MOYEN PONDÉRÉ
            // On utilise la logique de ChocolatsAchetes (quantité du tour) 
            // combinée au stock existant pour pondérer le nouveau prix
            double qteDejaAchetee = this.ChocolatsAchetes.getOrDefault(cdm, 0.0);
            double ancienPrixMoyen = this.prixDAchat.getOrDefault(cdm, contrat.getPrix());
        
            double qteNouveauContrat = contrat.getQuantiteTotale();
            double nouveauPrixMoyen = ((ancienPrixMoyen * qteDejaAchetee) + (contrat.getPrix() * qteNouveauContrat)) / (qteDejaAchetee + qteNouveauContrat);

            this.prixDAchat.put(cdm, nouveauPrixMoyen);
        
            // Actualisation du flux physique pour le tour en cours
            this.actualiserStockPredit(contrat);

            this.journal5.ajouter(Color.GREEN, Color.BLACK, "CC conclu (" + cdm + ") au prix unitaire de " + contrat.getPrix());
            this.journal5.ajouter(Color.CYAN, Color.BLACK, "Nouveau PMP pour " + cdm + " : " + nouveauPrixMoyen);
    }
    }

    public void receptionner(IProduit p, double quantiteEnTonnes, ExemplaireContratCadre contrat) {
        double stockActuel = this.Stock.getOrDefault(p, 0.0);
        this.Stock.put(p, stockActuel + quantiteEnTonnes);
        this.journal5.ajouter("Réception de " + quantiteEnTonnes + "T de " + p);
    }
}