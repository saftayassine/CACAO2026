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
        
        double stockActuel = this.getStock_chocolatDeMarque(choco);
        double quantiteDejaPromise = 0.0;
        
        for (ExemplaireContratCadre c : this.mesContratsEnCours) {
            if (c.getVendeur().equals(this) && c.getProduit().equals(choco)) {
                quantiteDejaPromise += c.getQuantiteRestantALivrer();
            }
        }
        double stockVraimentDisponible = Math.max(0.0, stockActuel - quantiteDejaPromise);

        int nbEcheancesPropose = Math.min(echeancier.getNbEcheances(), 5);
        double capaciteFuture = nbEcheancesPropose * (500 * 8.4)*0.5;
        
        double quantiteMaxPossible = stockVraimentDisponible;

        double quantiteDemandee = contrat.getQuantiteTotale();
        boolean modification = false;

        if (quantiteDemandee > quantiteMaxPossible) {
            quantiteDemandee = quantiteMaxPossible;
            modification = true;
        }
        if (echeancier.getNbEcheances() > 5) {
            modification = true;
        }

        if (modification) { 
            int etapeDebut = echeancier.getStepDebut();
            Echeancier nouvelEcheancier = new Echeancier(etapeDebut); 
            double quantiteParEcheance = quantiteDemandee / nbEcheancesPropose;
            for (int i = 0; i < nbEcheancesPropose; i++) {
                nouvelEcheancier.ajouter(quantiteParEcheance);
            }
            return nouvelEcheancier;
        } else if (!modification) {
            return echeancier; 
        } else {
            return null; 
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
                case C_HQ: prixPlancherTonne = 10000.0; break;
                case C_MQ: prixPlancherTonne = 7500.0; break;
                case C_BQ: prixPlancherTonne = 5000.0; break;
                default:   prixPlancherTonne = 3000.0; break;
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
            this.getJournaux().get(4).ajouter(contrat.toString()+ "\n");
            return quantite;
        } else {
            this.remove_chocolatDeMarque(cdm, stockDispo); // <--- LIGNE CRITIQUE
            this.getJournaux().get(4).ajouter(contrat.toString()+ "\n");
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
        ChocolatDeMarque chocoBQ = new ChocolatDeMarque(Chocolat.C_BQ, "Ferrara Rocher", 100);
        
        ChocolatDeMarque[] mesChocolats = {chocoHQ, chocoMQ, chocoBQ};

        for (ChocolatDeMarque choco : mesChocolats) {
            double stockActuel = this.getStock_chocolatDeMarque(choco);

            double quantiteDejaPromise = 0.0;
            
            for (ExemplaireContratCadre c : this.mesContratsEnCours) {
                // On vérifie qu'on est bien le vendeur sur ce contrat et que c'est le bon chocolat
                if (c.getVendeur().equals(this) && c.getProduit().equals(choco)) {
                    quantiteDejaPromise += c.getQuantiteRestantALivrer();
                }
            }

            double stockVraimentDisponible = stockActuel - quantiteDejaPromise;

            if (stockVraimentDisponible > 2000.0) {
                List<IAcheteurContratCadre> acheteurs = supCC.getAcheteurs(choco);

                if (!acheteurs.isEmpty()) {
                    IAcheteurContratCadre acheteur = acheteurs.get(0);
                    
                    double quantiteAVendreParTour = (stockVraimentDisponible * 0.80) / 5;
                    Echeancier echeancier = new Echeancier(Filiere.LA_FILIERE.getEtape() + 1, 5, quantiteAVendreParTour);
                    
                    supCC.demandeVendeur(acheteur, this, choco, echeancier, cryptogramme, false);
                }
            }
        }
        this.mesContratsEnCours.removeIf(c -> c.getQuantiteRestantALivrer() == 0);
    }
}