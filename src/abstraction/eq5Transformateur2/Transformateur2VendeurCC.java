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

        // --- LE VERROU INDUSTRIEL ULTIME (Couverture des risques) ---
            
            // 1. Chocolat physique déjà en stock
            double chocoDispo = this.getStock_chocolatDeMarque(choco);
            
            // 2. Fèves physiques en stock (converties en capacité chocolat)
            double prodPossibleStock = 0.0;
            if (choco.getChocolat() == Chocolat.C_HQ) {
                prodPossibleStock = Math.min(this.getStock_feve(Feve.F_HQ)/0.49, this.getStock_feve(Feve.F_MQ)/0.51);
            } else if (choco.getChocolat() == Chocolat.C_MQ) {
                prodPossibleStock = Math.min(this.getStock_feve(Feve.F_MQ)/0.26, this.getStock_feve(Feve.F_BQ)/0.74);
            } else if (choco.getChocolat() == Chocolat.C_BQ) {
                prodPossibleStock = this.getStock_feve(Feve.F_BQ)/0.45;
            }

            // 3. Fèves promises par NOS fournisseurs dans nos contrats d'ACHAT (Le Juste-à-Temps)
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

            // 4. Chocolat qu'on doit déjà livrer à nos clients (Ventes)
            double quantiteDejaPromise = 0.0;
            for (ExemplaireContratCadre c : this.mesContratsEnCours) {
                if (c.getVendeur().equals(this) && c.getProduit().equals(choco)) {
                    quantiteDejaPromise += c.getQuantiteRestantALivrer();
                }
            }

            // 5. Le vrai espace libre couvert par des approvisionnements !
            double capaciteSecuriseeTotale = chocoDispo + prodPossibleStock + prodPossibleAvenir;
            double espaceLibre = Math.max(0.0, capaciteSecuriseeTotale - quantiteDejaPromise);

            // On garde quand même un plafond absolu (ex: 150 000T) pour ne pas saturer l'usine si on a trop de fèves
            double PLAFOND_CARNET_COMMANDE = 50000.0;
            espaceLibre = Math.min(espaceLibre, Math.max(0.0, PLAFOND_CARNET_COMMANDE - quantiteDejaPromise));
            // -------------------------------------------------------------

        // Si le carnet est plein, on refuse
        if (espaceLibre <= 100.0) {
            return null;
        }

        double quantiteDemandee = contrat.getQuantiteTotale();
        boolean modification = false;
        int nbEcheancesPropose = Math.min(echeancier.getNbEcheances(), 5);

        // Sécurité : On accepte de gros contrats, mais on limite à 20 000 T d'un coup max 
        // pour ne pas saturer brutalement la chaîne logistique
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

	public double livrer(IProduit produit, double quantite, ExemplaireContratCadre contrat) {
        double quantiteALivrer = 0.0;
        
        if (produit instanceof ChocolatDeMarque) {
            ChocolatDeMarque choco = (ChocolatDeMarque) produit;
            double stockActuel = this.getStock_chocolatDeMarque(choco);
            
            // DYNAMIQUE : On livre ce que le contrat demande (quantite), 
            // mais bridé par ce qu'on a vraiment en stock pour ne pas passer en négatif
            quantiteALivrer = Math.min(quantite, stockActuel);
            
            // On retire UNIQUEMENT ce qu'on met dans le camion
            if (quantiteALivrer > 0) {
                this.remove_chocolatDeMarque(choco, quantiteALivrer); 
            }
        }
        
        // On renvoie la vraie quantité livrée au superviseur
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

        // LE CARNET DE COMMANDES : On s'autorise 150 000 T de promesses en cours par gamme
        double PLAFOND_CARNET_COMMANDE = 50000.0;

        for (ChocolatDeMarque choco : mesChocolats) {
            
            // --- LE VERROU INDUSTRIEL ULTIME (Couverture des risques) ---
            
            // 1. Chocolat physique déjà en stock
            double chocoDispo = this.getStock_chocolatDeMarque(choco);
            
            // 2. Fèves physiques en stock (converties en capacité chocolat)
            double prodPossibleStock = 0.0;
            if (choco.getChocolat() == Chocolat.C_HQ) {
                prodPossibleStock = Math.min(this.getStock_feve(Feve.F_HQ)/0.49, this.getStock_feve(Feve.F_MQ)/0.51);
            } else if (choco.getChocolat() == Chocolat.C_MQ) {
                prodPossibleStock = Math.min(this.getStock_feve(Feve.F_MQ)/0.26, this.getStock_feve(Feve.F_BQ)/0.74);
            } else if (choco.getChocolat() == Chocolat.C_BQ) {
                prodPossibleStock = this.getStock_feve(Feve.F_BQ)/0.45;
            }

            // 3. Fèves promises par NOS fournisseurs dans nos contrats d'ACHAT (Le Juste-à-Temps)
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

            // 4. Chocolat qu'on doit déjà livrer à nos clients (Ventes)
            double quantiteDejaPromise = 0.0;
            for (ExemplaireContratCadre c : this.mesContratsEnCours) {
                if (c.getVendeur().equals(this) && c.getProduit().equals(choco)) {
                    quantiteDejaPromise += c.getQuantiteRestantALivrer();
                }
            }

            // 5. Le vrai espace libre couvert par des approvisionnements !
            double capaciteSecuriseeTotale = chocoDispo + prodPossibleStock + prodPossibleAvenir;
            double espaceLibre = Math.max(0.0, capaciteSecuriseeTotale - quantiteDejaPromise);

            // On garde quand même un plafond absolu (ex: 150 000T) pour ne pas saturer l'usine si on a trop de fèves
            espaceLibre = Math.min(espaceLibre, Math.max(0.0, PLAFOND_CARNET_COMMANDE - quantiteDejaPromise));
            // -------------------------------------------------------------

            // Si on a de la place, on démarche !
            if (espaceLibre > 2000.0) {
                List<IAcheteurContratCadre> acheteurs = supCC.getAcheteurs(choco);

                if (!acheteurs.isEmpty()) {
                    for (IAcheteurContratCadre acheteur : acheteurs){
                    
                    // Sécurité anti-faillite client : on propose max 5000T d'un coup
                    double quantiteAProposer = espaceLibre ; 
                    double quantiteParTour = quantiteAProposer / 5.0;
                    
                    Echeancier echeancier = new Echeancier(Filiere.LA_FILIERE.getEtape() + 1, 5, quantiteParTour);
                    supCC.demandeVendeur(acheteur, this, choco, echeancier, cryptogramme, false);
                }
            }
            }
        }
        
        // Nettoyage comptable
        this.mesContratsEnCours.removeIf(c -> c.getQuantiteRestantALivrer() == 0 || c.getAcheteur().getNom().toLowerCase().contains("faillite"));
    }
}