package abstraction.eq5Transformateur2;

import java.util.List;

import abstraction.eqXRomu.contratsCadres.*;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.IProduit;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Feve;


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

            double chocoDispo = this.getStock_chocolatDeMarque(choco);

            double prodPossibleStock = 0.0;
            if (choco.getChocolat() == Chocolat.C_HQ) {
                prodPossibleStock = Math.min(this.getStock_feve(Feve.F_HQ)/0.49, this.getStock_feve(Feve.F_MQ)/0.51);
            } else if (choco.getChocolat() == Chocolat.C_MQ) {
                prodPossibleStock = Math.min(this.getStock_feve(Feve.F_MQ)/0.26, this.getStock_feve(Feve.F_BQ)/0.74);
            } else if (choco.getChocolat() == Chocolat.C_BQ) {
                prodPossibleStock = this.getStock_feve(Feve.F_BQ)/0.45;
            }


            double fevesHQAvenir = 0.0;
            double fevesMQAvenir = 0.0;
            double fevesBQAvenir = 0.0;
            for (ExemplaireContratCadre c : this.mesContratsEnCours) {
                if (c.getAcheteur().equals(this)) { // Si on est l'acheteur (donc c'est des fèves)
                    if (c.getProduit().equals(Feve.F_HQ)) fevesHQAvenir += c.getQuantiteRestantALivrer();
                    else if (c.getProduit().equals(Feve.F_MQ)) fevesMQAvenir += c.getQuantiteRestantALivrer();
                    else if (c.getProduit().equals(Feve.F_BQ)) fevesBQAvenir += c.getQuantiteRestantALivrer();
                }
            }
            
            double prodPossibleAvenir = 0.0;
            if (choco.getChocolat() == Chocolat.C_HQ) {
                prodPossibleAvenir = Math.min(fevesHQAvenir/0.49, fevesMQAvenir/0.51);
            } else if (choco.getChocolat() == Chocolat.C_MQ) {
                prodPossibleAvenir = Math.min(fevesMQAvenir/0.26, fevesBQAvenir/0.74);
            } else if (choco.getChocolat() == Chocolat.C_BQ) {
                prodPossibleAvenir = fevesBQAvenir/0.45;
            }

            double quantiteDejaPromise = 0.0;
            for (ExemplaireContratCadre c : this.mesContratsEnCours) {
                if (c.getVendeur().equals(this) && c.getProduit().equals(choco)) {
                    quantiteDejaPromise += c.getQuantiteRestantALivrer();
                }
            }

  
            double capaciteSecuriseeTotale = chocoDispo + prodPossibleStock + prodPossibleAvenir;
            double espaceLibre = Math.max(0.0, capaciteSecuriseeTotale - quantiteDejaPromise);

  
            double PLAFOND_CARNET_COMMANDE = 50000.0;
            if (Filiere.LA_FILIERE.getEtape() <= 10){
                PLAFOND_CARNET_COMMANDE = 50000.0;
            }
            else {
                PLAFOND_CARNET_COMMANDE = 150000.0;
            }
            espaceLibre = Math.min(espaceLibre, Math.max(0.0, PLAFOND_CARNET_COMMANDE - quantiteDejaPromise));


        // On refuse si on a déjà trop de contrats
        if (espaceLibre <= 100.0) {
            return null;
        }

        double quantiteDemandee = contrat.getQuantiteTotale();
        boolean modification = false;
        int nbEcheancesPropose = Math.min(echeancier.getNbEcheances(), 5);

        double limiteParContrat = espaceLibre;

        if (quantiteDemandee > limiteParContrat) {
            quantiteDemandee = limiteParContrat;
            modification = true;
        }
        if (echeancier.getNbEcheances() > 5) {
            modification = true;
        }

        if (quantiteDemandee < 100.0) {
            return null; 
        }

        if (modification) { 
            int etapeDebut = echeancier.getStepDebut();
            Echeancier nouvelEcheancier = new Echeancier(etapeDebut); 
            double quantiteParEcheance = quantiteDemandee / nbEcheancesPropose;
            for (int i = 0; i < nbEcheancesPropose; i++) {
                nouvelEcheancier.ajouter(quantiteParEcheance);
            }
            if(nouvelEcheancier.echeancierAcceptable()){
                return nouvelEcheancier;
            }
            else{return null;}
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
                prixPlancherTonne = 15000.0;
                break;
            case C_MQ: 
                prixPlancherTonne = 10000.0; 
                break;
            case C_BQ: 
                prixPlancherTonne = 7000.0; 
                break;
            default:   
                prixPlancherTonne = 5000.0;
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
                case C_HQ: prixPlancherTonne = 8000.0; break;
                case C_MQ: prixPlancherTonne = 5500.0; break;
                case C_BQ: prixPlancherTonne = 2000.0; break;
                default:   prixPlancherTonne = 2000.0; break;
            }
        } else {
            return -1000; 
        }
        
        if (contrat.getPrix() >= prixPlancherTonne) {
            return contrat.getPrix();
        }

        List<Double> listePrix = contrat.getListePrix();
        if (listePrix.size() < 2) {
            return prixPlancherTonne * 1.20; 
        }
        
        double notreDerniereOffre = listePrix.get(listePrix.size() - 2);
        double nouvelleOffre = (4*contrat.getPrix() + 6*notreDerniereOffre) / 10;
        
        double offreFinale = Math.max(nouvelleOffre, prixPlancherTonne);
        
        if (contrat.getPrix() >= offreFinale) {            return contrat.getPrix();
        } else {
            return offreFinale;
        }
    }

	public double livrer(IProduit produit, double quantite, ExemplaireContratCadre contrat) {
        double quantiteALivrer = 0.0;
        
        if (produit instanceof ChocolatDeMarque) {
            ChocolatDeMarque choco = (ChocolatDeMarque) produit;
            double stockActuel = this.getStock_chocolatDeMarque(choco);
            
            // On livre au max ce qu'il y a en stock
            quantiteALivrer = Math.min(quantite, stockActuel);
            

            if (quantiteALivrer > 0) {
                this.remove_chocolatDeMarque(choco, quantiteALivrer); 
            }
        }
        
        return quantiteALivrer;
    }

    @Override
    public void next() {
        super.next(); 

        SuperviseurVentesContratCadre supCC = (SuperviseurVentesContratCadre) Filiere.LA_FILIERE.getActeur("Sup.CCadre");

        ChocolatDeMarque chocoHQ = new ChocolatDeMarque(Chocolat.C_HQ, "Ferrara Rocher", 100);
        ChocolatDeMarque chocoMQ = new ChocolatDeMarque(Chocolat.C_MQ, "Ferrara Rocher", 100);
        ChocolatDeMarque chocoBQ = new ChocolatDeMarque(Chocolat.C_BQ, "Ferrara Rocher", 45); 
        
        ChocolatDeMarque[] mesChocolats = {chocoHQ, chocoMQ, chocoBQ};

        double PLAFOND_CARNET_COMMANDE = 50000.0;
            if (Filiere.LA_FILIERE.getEtape() <= 10){
                PLAFOND_CARNET_COMMANDE = 50000.0;
            }
            else {
                PLAFOND_CARNET_COMMANDE = 150000.0;
            }

        for (ChocolatDeMarque choco : mesChocolats) {
        
            
            //Chocolat physique déjà en stock
            double chocoDispo = this.getStock_chocolatDeMarque(choco);
            
            // 2. Fèves en stock
            double prodPossibleStock = 0.0;
            if (choco.getChocolat() == Chocolat.C_HQ) {
                prodPossibleStock = Math.min(this.getStock_feve(Feve.F_HQ)/0.49, this.getStock_feve(Feve.F_MQ)/0.51);
            } else if (choco.getChocolat() == Chocolat.C_MQ) {
                prodPossibleStock = Math.min(this.getStock_feve(Feve.F_MQ)/0.26, this.getStock_feve(Feve.F_BQ)/0.74);
            } else if (choco.getChocolat() == Chocolat.C_BQ) {
                prodPossibleStock = this.getStock_feve(Feve.F_BQ)/0.45;
            }

            // Fèves promises que nous allons recevoir
            double fevesHQAvenir = 0.0;
            double fevesMQAvenir = 0.0;
            double fevesBQAvenir = 0.0;
            for (ExemplaireContratCadre c : this.mesContratsEnCours) {
                if (c.getAcheteur().equals(this)) { // Si on est l'acheteur (donc c'est des fèves)
                    if (c.getProduit().equals(Feve.F_HQ)) fevesHQAvenir += c.getQuantiteRestantALivrer();
                    else if (c.getProduit().equals(Feve.F_MQ)) fevesMQAvenir += c.getQuantiteRestantALivrer();
                    else if (c.getProduit().equals(Feve.F_BQ)) fevesBQAvenir += c.getQuantiteRestantALivrer();
                }
            }
            
            double prodPossibleAvenir = 0.0;
            if (choco.getChocolat() == Chocolat.C_HQ) {
                prodPossibleAvenir = Math.min(fevesHQAvenir/0.49, fevesMQAvenir/0.51);
            } else if (choco.getChocolat() == Chocolat.C_MQ) {
                prodPossibleAvenir = Math.min(fevesMQAvenir/0.26, fevesBQAvenir/0.74);
            } else if (choco.getChocolat() == Chocolat.C_BQ) {
                prodPossibleAvenir = fevesBQAvenir/0.45;
            }

            //Chocolat qu'on doit déjà livrer à nos clients
            double quantiteDejaPromise = 0.0;
            for (ExemplaireContratCadre c : this.mesContratsEnCours) {
                if (c.getVendeur().equals(this) && c.getProduit().equals(choco)) {
                    quantiteDejaPromise += c.getQuantiteRestantALivrer();
                }
            }


            double capaciteSecuriseeTotale = chocoDispo + prodPossibleStock + prodPossibleAvenir;
            double espaceLibre = Math.max(0.0, capaciteSecuriseeTotale - quantiteDejaPromise);


            espaceLibre = Math.min(espaceLibre, Math.max(0.0, PLAFOND_CARNET_COMMANDE - quantiteDejaPromise));


            //On propose un contrat si on peut se le permettre
            if (espaceLibre > 2000.0) {
                List<IAcheteurContratCadre> acheteurs = supCC.getAcheteurs(choco);

                if (!acheteurs.isEmpty()) {
                    for (IAcheteurContratCadre acheteur : acheteurs){
                    
                    // Sécurité anti-faillite client : on propose max 5000T d'un coup
                    double quantiteAProposer = espaceLibre ; 
                    double quantiteParTour = quantiteAProposer / 5.0;
                    
                    Echeancier echeancier = new Echeancier(Filiere.LA_FILIERE.getEtape() + 1, 5, quantiteParTour);
                    ExemplaireContratCadre Contrat = supCC.demandeVendeur(acheteur, this, choco, echeancier, cryptogramme, false);
                    if (Contrat != null){
                        this.notificationNouveauContratCadre(Contrat);
                    }
                }
            }
            }
        }
        
        // Nettoyage comptable
        this.mesContratsEnCours.removeIf(c -> c.getQuantiteRestantALivrer() == 0 || c.getAcheteur().getNom().toLowerCase().contains("faillite"));
    }
}