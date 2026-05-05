package abstraction.eq5Transformateur2;

import java.awt.Color;
import java.util.List;

import abstraction.eqXRomu.contratsCadres.*;
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

	public boolean vend(IProduit produit){
		if (produit instanceof ChocolatDeMarque){ 
			ChocolatDeMarque cdm = (ChocolatDeMarque) produit;
			if (!cdm.getNom().equals("Ferrara Rocher")) {
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

	public Echeancier contrePropositionDuVendeur(ExemplaireContratCadre contrat){
		return contrat.getEcheancier();
	}
	
	public double propositionPrix(ExemplaireContratCadre contrat){
		return 1.35*contrat.getQuantiteTotale()*(prix_MP+2);
	}

	public double contrePropositionPrixVendeur(ExemplaireContratCadre contrat){
		return ((contrat.getPrix() + contrat.getListePrix().get(contrat.getListePrix().size() - 2))/2)*1.20;
	}

	public void notificationNouveauContratCadre(ExemplaireContratCadre contrat){
		if (contrat.getVendeur().equals(this)) {
			this.getJournaux().get(4).ajouter(contrat.toString()+ "\n");
		}
		else{
		this.getJournaux().get(3).ajouter(contrat.toString()+ "\n");
		}
	}

	public double livrer(IProduit produit, double quantite, ExemplaireContratCadre contrat){
		if (produit instanceof ChocolatDeMarque){ 
			ChocolatDeMarque cdm = (ChocolatDeMarque) produit;
			Chocolat c = cdm.getChocolat();
            
            double stockDispo = this.getStock_chocolatDeMarque(cdm); // On regarde le bon stock
			
            if (stockDispo >= quantite){
				this.remove_chocolatDeMarque(cdm, quantite);
				return quantite;
			} else {
				this.ProductionChocolat(c, quantite - stockDispo);
			    this.remove_chocolatDeMarque(cdm, stockDispo);
			    return stockDispo; // Correction : on ne peut renvoyer que ce qu'on livre vraiment
			}
		} else {
			return 0;
		}
	}
}