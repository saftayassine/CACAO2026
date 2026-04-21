package abstraction.eq3Producteur3;

import abstraction.eqXRomu.bourseCacao.IVendeurBourse;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.Gamme;
import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

/** @author Victor Vannier-Moreau */
public class Producteur3VendeurBourse extends Producteur3Acteur implements IVendeurBourse {

	protected List<ExemplaireContratCadre> contratsEnCours;

    public Producteur3VendeurBourse() {
		super();
		this.contratsEnCours = new LinkedList<ExemplaireContratCadre>();
	}

	public double offre(Feve f, double cours) {
		// 1. Calculer la quantité totale que l'on doit livrer ce step pour tous les CC
		double quantiteReserveeCC = 0;
		
		if (this.contratsEnCours != null) {
			for (ExemplaireContratCadre c : this.contratsEnCours) {
				if (c.getProduit().equals(f)) {
					quantiteReserveeCC += c.getQuantiteALivrerAuStep();
				}
			}
		}

		// 2. Calculer le surplus réel disponible pour la bourse
		double stockActuel = this.stock.getStock(f);
		double surplus = Math.max(0, stockActuel - quantiteReserveeCC);

    	// 3. Logique de vente en bourse basée sur le surplus
    	if (f.getGamme() == Gamme.MQ) {
        	double offre = surplus / 2.0; 
        	if (offre > 0) {
				journal_vente_bouse.ajouter(new Color(204, 54, 0), Color.black, 
				Filiere.LA_FILIERE.getEtape() + " : Bourse (MQ) surplus: " + offre + " T");
				}
					return offre;
				}

		else {
		if (f.getGamme()==Gamme.BQ){
			double offre= this.stock.getStock(Feve.F_BQ);
			journal_vente_bouse.ajouter(new Color(255, 255, 0), Color.black,Filiere.LA_FILIERE.getEtape()+" : je met en vente "+offre+" T de "+f);
				return offre;
			}
		else{
				return 0.0;
			}
			
		}
	}

	public double notificationVente(Feve f, double quantiteEnT, double coursEnEuroParT) {
		double retire = Math.min(this.stock.getStock(f), quantiteEnT);
		this.stock.retireStock(f, retire);
		this.mettreAJourIndicateurStock();
		this.journal_vente_bouse.ajouter(new Color(255, 128, 0), Color.black,"période "+Filiere.LA_FILIERE.getEtape()+" : vente de "+ retire + " tonnes de fève de  "+ f.toString() );
		return retire;
	}

	public void notificationBlackList(int dureeEnStep) {

	}

}

    

