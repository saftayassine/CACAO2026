package abstraction.eq5Transformateur2;

import java.util.List;

import abstraction.eqXRomu.contratsCadres.*;
import abstraction.eqXRomu.produits.IProduit;
import abstraction.eqXRomu.produits.ChocolatDeMarque;


/**
 * @author Pierre GUTTIEREZ
 */
 
public class Transformateur2VendeurCC extends Transformateur2AchatCC implements IVendeurContratCadre{

    public Transformateur2VendeurCC() {
        super();
    }

	public boolean vend(IProduit produit){
		if (produit instanceof ChocolatDeMarque){ 
			ChocolatDeMarque cdm = (ChocolatDeMarque) produit;
			if (!cdm.getMarque().equals("Ferrara Rocher")) {
        	return false; // On ne vend pas les marques des concurrents !
			}
			if (cdm.getChocolat().isEquitable()){ // On extrait le chocolat générique pour tester
				return false;
			} else {
			    return true;
			}
		} else {
			return false;
		}
	}

	public Echeancier contrePropositionDuVendeur(ExemplaireContratCadre contrat) {
        Echeancier echeancier = contrat.getEcheancier();
        
        // 1. Notre capacité de production maximale par étape (9000 employés * 8.4 T)
        double capaciteProdParEtape = 500 * 8.4; // 75 600 Tonnes

        int nbEcheancesPropose = echeancier.getNbEcheances();
        boolean modification = false;

        // 2. On limite le nombre d'échéances à 5 maximum
        if (nbEcheancesPropose > 5) {
            nbEcheancesPropose = 5;
            modification = true;
        }

        // 3. On calcule la quantité demandée par étape par le distributeur
        double quantiteDemandeeParEcheance = contrat.getQuantiteTotale() / echeancier.getNbEcheances();
        double quantiteParEcheance = quantiteDemandeeParEcheance;
        
        // 4. SÉCURITÉ : Si on nous demande plus que notre capacité max, on plafonne !
        // (Vous pouvez même diviser capaciteProdParEtape par 2 ou 3 si vous voulez 
        // garder de la place pour plusieurs contrats en même temps)
        if (quantiteDemandeeParEcheance > capaciteProdParEtape) {
            quantiteParEcheance = capaciteProdParEtape;
            modification = true;
        }

        // 5. Si on a dû modifier la durée OU la quantité, on renvoie une contre-proposition
        if (modification) {
            int etapeDebut = echeancier.getStepDebut();
            Echeancier nouvelEcheancier = new Echeancier(etapeDebut); 
            
            for (int i = 0; i < nbEcheancesPropose; i++) {
                nouvelEcheancier.ajouter(quantiteParEcheance);
            }
            return nouvelEcheancier;
            
        } else {
            // Sinon, l'échéancier respecte nos règles de durée et de quantité, on accepte !
            return echeancier;
        }
    }
	
	public double propositionPrix(ExemplaireContratCadre contrat){
        if (!(contrat.getProduit() instanceof ChocolatDeMarque)) {
            return 0.0;
        }
    
        ChocolatDeMarque cdm = (ChocolatDeMarque) contrat.getProduit();
    
        // 2. On définit un prix plancher (minimum absolu) à la tonne selon la gamme
        // N'hésitez pas à ajuster ces valeurs selon vos stratégies !
        double prixPlancherTonne;
        switch (cdm.getChocolat()) {
            case C_HQ: 
                prixPlancherTonne = 10000.0; // Le HQ est très cher
                break;
            case C_MQ: 
                prixPlancherTonne = 7500.0; // Le MQ est standard
                break;
            case C_BQ: 
                prixPlancherTonne = 5000.0; // Le BQ est abordable
                break;
            default:   
                prixPlancherTonne = 3000.0;
                break;
        }

        
        double coutDeRevient = prix_MP; 
        double prixCalculeTonne = coutDeRevient * 1.55;

        // 4. Stratégie : On prend le plus haut entre notre prix calculé et notre prix plancher absolu
        double prixFinalTonne = Math.max(prixCalculeTonne, prixPlancherTonne);

        // 5. On retourne le prix TOTAL demandé par le contrat
        return prixFinalTonne;
        }

	public double contrePropositionPrixVendeur(ExemplaireContratCadre contrat){
        // 1. On récupère notre prix plancher selon la gamme demandée
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
        
        // 2. Si le prix proposé par l'acheteur est déjà supérieur à notre minimum, 
        // c'est parfait, on accepte tout de suite !
        if (contrat.getPrix() >= prixPlancherTonne) {
            return contrat.getPrix();
        }

        // 3. Sinon, on négocie
        List<Double> listePrix = contrat.getListePrix();
        
        if (listePrix.size() < 2) {
            // Premier tour de négo : On demande notre prix plancher + 20% pour avoir de la marge
            return prixPlancherTonne * 1.20; 
        }
        
        // Tours suivants : On fait un pas vers l'acheteur (on coupe la poire en deux)
        double notreDerniereOffre = listePrix.get(listePrix.size() - 2);
        double nouvelleOffre = (contrat.getPrix() + notreDerniereOffre) / 2;
        
        if (contrat.getPrix() >= nouvelleOffre) {
            return contrat.getPrix();
        } else{
        return nouvelleOffre;
        }
        }

	public double livrer(IProduit produit, double quantite, ExemplaireContratCadre contrat){
		if (produit instanceof ChocolatDeMarque){ 
        ChocolatDeMarque cdm = (ChocolatDeMarque) produit;
        double stockDispo = this.getStock_chocolatDeMarque(cdm);

        if (stockDispo >= quantite){
            this.remove_chocolatDeMarque(cdm, quantite); // <--- LIGNE CRITIQUE
            return quantite;
        } else {
            // On livre ce qu'il nous reste
            this.remove_chocolatDeMarque(cdm, stockDispo); // <--- LIGNE CRITIQUE
            return stockDispo; 
        }
    }
    return 0.0;
	}
}