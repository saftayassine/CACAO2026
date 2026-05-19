package abstraction.eq5Transformateur2;

import abstraction.eqXRomu.produits.IProduit;
import abstraction.eqXRomu.bourseCacao.BourseCacao;
import abstraction.eqXRomu.encheres.Enchere;
import abstraction.eqXRomu.encheres.IAcheteurAuxEncheres;
import abstraction.eqXRomu.encheres.MiseAuxEncheres;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.Feve;

/**
 * @Auteur Maxence
 */



public class Transformateur2AchatEncheres extends Transformateur2VendeurAppelOffre implements IAcheteurAuxEncheres {


    public Transformateur2AchatEncheres() {
        super();
    }


	public double proposerPrix(MiseAuxEncheres offre) {
		IProduit produit = offre.getProduit();
		if (produit instanceof Feve){
			Feve f=(Feve) produit;
			Double quantite = offre.getQuantiteT();
			if(f==Feve.F_BQ){
				Double cours = ((BourseCacao) (Filiere.LA_FILIERE.getActeur("BourseCacao"))).getCours(Feve.F_BQ).getValeur();
				if (quantite>this.DemandeChocolat().get(Chocolat.C_BQ)*0.1){
					return 0.85*cours;
				}
				else{
					return 0.9*cours;
				}
			}
			if(f==Feve.F_MQ){
				Double cours = ((BourseCacao) (Filiere.LA_FILIERE.getActeur("BourseCacao"))).getCours(Feve.F_MQ).getValeur();
				if (quantite>this.DemandeChocolat().get(Chocolat.C_MQ)*0.1){
					return 0.85*cours;
				}
				else{
					return 0.9*cours;
				}
			}
			else{
				Double cours = ((BourseCacao) (Filiere.LA_FILIERE.getActeur("BourseCacao"))).getCours(Feve.F_HQ).getValeur();
				if (quantite>this.DemandeChocolat().get(Chocolat.C_HQ)*0.1){
					return 0.85*cours;
				}
				else{
					return 0.9*cours;
				}
			}
		}
		else{
			return 0.0;
		}
	}

	public void notifierAchatAuxEncheres(Enchere propositionRetenue) {
		Double prixTonne=propositionRetenue.getPrixTonne();
		Double quantiteEnT=propositionRetenue.getQuantiteT();
		IProduit produit = propositionRetenue.getProduit();
		Feve f=(Feve) produit;
		this.getJournaux().get(1).ajouter("Achat effectué de: "+quantiteEnT+" fèves "+f+" au prix/tonne de "+prixTonne);
		this.getJournaux().get(4).ajouter("Achat effectué de: "+quantiteEnT+" fèves "+f+" au prix/tonne de "+prixTonne);

		this.add_feve(quantiteEnT,f);
	}

	public void notifierEnchereNonRetenue(Enchere propositionNonRetenue) {
		Double prixTonne=propositionNonRetenue.getPrixTonne();
		Double quantiteEnT=propositionNonRetenue.getQuantiteT();
		IProduit produit = propositionNonRetenue.getProduit();

		this.getJournaux().get(4).ajouter("L'achat de: "+quantiteEnT+" de "+produit+" au prix/tonne de "+prixTonne+" n'a pas été retenue");
	}
}
