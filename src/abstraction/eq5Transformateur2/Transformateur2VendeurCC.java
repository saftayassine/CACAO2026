package abstraction.eq5Transformateur2;

import java.applet.Applet;
import java.awt.Color;
import java.util.List;

import abstraction.eqXRomu.contratsCadres.*;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IActeur;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.produits.IProduit;
import abstraction.eqXRomu.produits.Chocolat; 
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.bourseCacao.BourseCacao;


/**
 * @author Pierre GUTTIEREZ
 
public class Transformateur2VendeurCC extends Transformateur2AcheteurBourse implements IVendeurContratCadre{

    public Transformateur2VendeurCC() {
        super();
    }

	public boolean vend(IProduit produit){
		if (produit instanceof Chocolat){
			Chocolat c = (Chocolat) produit;
			if (c.isEquitable()){
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

	}

	public double contrePropositionPrixVendeur(ExemplaireContratCadre contrat);
	

	public void notificationNouveauContratCadre(ExemplaireContratCadre contrat);

	public double livrer(IProduit produit, double quantite, ExemplaireContratCadre contrat);
}
*/