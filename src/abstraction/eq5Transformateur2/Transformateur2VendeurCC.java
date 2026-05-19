package abstraction.eq5Transformateur2;

import java.util.List;

import abstraction.eqXRomu.contratsCadres.*;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.IProduit;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;


/**
 * @author Pierre GUTTIEREZ
 */
 
public class Transformateur2VendeurCC extends Transformateur2AchatCC implements IVendeurContratCadre{

    public Transformateur2VendeurCC() {
        super();
    }

	public boolean vend(IProduit produit) {
		if (produit != null && produit instanceof ChocolatDeMarque){ 
			ChocolatDeMarque cdm = (ChocolatDeMarque) produit;
			
			String marque = cdm.getMarque().toLowerCase();
			
			if (marque.contains("ferrara")) {
			    return true; 
			}
		}
		
		return false;
	}

	public Echeancier contrePropositionDuVendeur(ExemplaireContratCadre contrat) {
        if (!(contrat.getProduit() instanceof ChocolatDeMarque)) {
            return null;
        }
        ChocolatDeMarque choco = (ChocolatDeMarque) contrat.getProduit();
        Echeancier echeancier = contrat.getEcheancier();
        
        // NOTRE PLAFOND D'ENGAGEMENT (Doit être le même que dans next)
        double PLAFOND_CC = 10000.0; 
        
        double quantiteDejaPromise = 0.0;
        for (ExemplaireContratCadre c : this.mesContratsEnCours) {
            if (c.getVendeur().equals(this) && c.getProduit().equals(choco)) {
                quantiteDejaPromise += c.getQuantiteRestantALivrer();
            }
        }
        
        // Ce qu'on est encore autorisé à signer
        double espaceLibre = PLAFOND_CC - quantiteDejaPromise;

        // Si le carnet de commandes est plein, on refuse
        if (espaceLibre <= 0.0) {
            return null;
        }

        double quantiteDemandee = contrat.getQuantiteTotale();
        boolean modification = false;

        // 1. On bloque la durée à 5 tours maximum pour ne pas geler l'usine trop longtemps
        int nbEcheancesPropose = Math.min(echeancier.getNbEcheances(), 5);
        if (echeancier.getNbEcheances() > 5) {
            modification = true;
        }

        // 2. On rabote si l'acheteur demande plus que notre espace libre
        if (quantiteDemandee > espaceLibre) {
            quantiteDemandee = espaceLibre;
            modification = true;
        }

        if (quantiteDemandee < 100.0) {
            return null; // On ne s'embête pas pour moins de 100 Tonnes
        }

        // 3. On renvoie l'offre finale
        if (modification) { 
            int etapeDebut = echeancier.getStepDebut();
            Echeancier nouvelEcheancier = new Echeancier(etapeDebut); 
            double quantiteParEcheance = quantiteDemandee / nbEcheancesPropose;
            for (int i = 0; i < nbEcheancesPropose; i++) {
                nouvelEcheancier.ajouter(quantiteParEcheance);
            }
            return nouvelEcheancier;
        } else {
            return echeancier; 
        }
    }
	
	public double propositionPrix(ExemplaireContratCadre contrat){
        if (!(contrat.getProduit() instanceof ChocolatDeMarque)) {
            return 0.0;
        }
    
        ChocolatDeMarque cdm = (ChocolatDeMarque) contrat.getProduit();
    
        double prixPlancherTonne;
        switch (cdm.getChocolat()) {
            case C_HQ: 
                prixPlancherTonne = 10000.0;
                break;
            case C_MQ: 
                prixPlancherTonne = 7500.0; 
                break;
            case C_BQ: 
                prixPlancherTonne = 5000.0; 
                break;
            default:   
                prixPlancherTonne = 3000.0;
                break;
        }

        
        double coutDeRevient = prix_MP; 
        double prixCalculeTonne = coutDeRevient * 1.55;

        double prixFinalTonne = Math.max(prixCalculeTonne, prixPlancherTonne);

        return prixFinalTonne;
        }

	public double contrePropositionPrixVendeur(ExemplaireContratCadre contrat){
        double prixPlancherTonne;
        if (contrat.getProduit() instanceof ChocolatDeMarque) {
            switch (((ChocolatDeMarque) contrat.getProduit()).getChocolat()) {
                case C_HQ: prixPlancherTonne = 15000.0; break;
                case C_MQ: prixPlancherTonne = 10000.0; break;
                case C_BQ: prixPlancherTonne = 7000.0; break;
                default:   prixPlancherTonne = 5000.0; break;
            }
        } else {
            return -1000; 
        }
        
        if (contrat.getPrix() >= prixPlancherTonne) {
            this.getJournaux().get(4).ajouter(contrat.toString()+ "\n");
            return contrat.getPrix();
        }

        List<Double> listePrix = contrat.getListePrix();
        if (listePrix.size() < 2) {
            return prixPlancherTonne * 1.20; 
        }
        
        double notreDerniereOffre = listePrix.get(listePrix.size() - 2);
        double nouvelleOffre = (4*contrat.getPrix() + 6*notreDerniereOffre) / 10;
        
        double offreFinale = Math.max(nouvelleOffre, prixPlancherTonne);
        
        if (contrat.getPrix() >= offreFinale) {
            this.getJournaux().get(4).ajouter(contrat.toString()+ "\n");
            return contrat.getPrix();
        } else {
            return offreFinale;
        }
    }

	public double livrer(IProduit produit, double quantite, ExemplaireContratCadre contrat){
		if (produit instanceof ChocolatDeMarque){ 
        ChocolatDeMarque cdm = (ChocolatDeMarque) produit;
        double stockDispo = this.getStock_chocolatDeMarque(cdm);

        if (stockDispo >= quantite){
            this.remove_chocolatDeMarque(cdm, quantite); // <--- LIGNE CRITIQUE
            this.getJournaux().get(4).ajouter("Livraison Totale: " +contrat.getNumero() + " - " + contrat.getAcheteur().getNom() + " - " + String.valueOf(quantite)+"\n");
            return quantite;
        } else {
            this.remove_chocolatDeMarque(cdm, stockDispo); // <--- LIGNE CRITIQUE
            this.getJournaux().get(4).ajouter("Livraison partielle: " + contrat.getNumero() + " - " + contrat.getAcheteur().getNom() + " - " + String.valueOf(stockDispo) + "\n");
            return stockDispo; 
        }
    }
    return 0.0;
	}

    @Override
    public void next() {
        super.next(); 

        SuperviseurVentesContratCadre supCC = (SuperviseurVentesContratCadre) Filiere.LA_FILIERE.getActeur("Sup.CCadre");

        ChocolatDeMarque chocoHQ = new ChocolatDeMarque(Chocolat.C_HQ, "Ferrara Rocher", 100);
        ChocolatDeMarque chocoMQ = new ChocolatDeMarque(Chocolat.C_MQ, "Ferrara Rocher", 100);
        ChocolatDeMarque chocoBQ = new ChocolatDeMarque(Chocolat.C_BQ, "Ferrara Rocher", 45);
        
        ChocolatDeMarque[] mesChocolats = {chocoHQ, chocoMQ, chocoBQ};

        double PLAFOND_CC = 40000.0; 

        for (ChocolatDeMarque choco : mesChocolats) {
            double quantiteDejaPromise = 0.0;
            for (ExemplaireContratCadre c : this.mesContratsEnCours) {
                if (c.getVendeur().equals(this) && c.getProduit().equals(choco)) {
                    quantiteDejaPromise += c.getQuantiteRestantALivrer();
                }
            }

            double espaceLibre = PLAFOND_CC - quantiteDejaPromise;

            if (espaceLibre > 10000.0) {
                List<IAcheteurContratCadre> acheteurs = supCC.getAcheteurs(choco);

                if (!acheteurs.isEmpty()) {
                    IAcheteurContratCadre acheteur = acheteurs.get(0); // On pourrait rendre ça aléatoire
                    
                    double quantiteAProposer = espaceLibre / 4.0; 
                    double quantiteParTour = quantiteAProposer / 5.0;
                    
                    Echeancier echeancier = new Echeancier(Filiere.LA_FILIERE.getEtape() + 1, 5, quantiteParTour);
                    supCC.demandeVendeur(acheteur, this, choco, echeancier, cryptogramme, false);
                }
            }
        }
        
        // Nettoyage comptable
        this.mesContratsEnCours.removeIf(c -> c.getQuantiteRestantALivrer() == 0 || c.getAcheteur().getNom().toLowerCase().contains("faillite"));
    }
}