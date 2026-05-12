package abstraction.eq7Transformateur4;

import java.util.List;

import abstraction.eqXRomu.encheres.Enchere;
import abstraction.eqXRomu.encheres.ExempleAbsVendeurAuxEncheres;
import abstraction.eqXRomu.encheres.IVendeurAuxEncheres;
import abstraction.eqXRomu.encheres.SuperviseurVentesAuxEncheres;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.Chocolat;

//Auteur : Paul

public class Transformateur4VendeurAuxEncheres extends Transformateur4Vente implements IVendeurAuxEncheres {
    SuperviseurVentesAuxEncheres superviseur;
	
	public void initialiser() {
		this.superviseur = (SuperviseurVentesAuxEncheres)(Filiere.LA_FILIERE.getActeur("Sup.Encheres"));
		//journal.ajouter("PrixMin=="+6000);

	}
	
	public void next() {
		//journal.ajouter("Etape="+Filiere.LA_FILIERE.getEtape());
        super.next();
		if (Filiere.LA_FILIERE.getEtape()>=1) {
			if (this.get_StockChoco_BQ().getValeur()>200) {
				Enchere retenue = superviseur.vendreAuxEncheres(this, cryptogramme, getChocolatsProduits().get(0), 200.0);
				if (retenue!=null) {
					this.get_StockChoco_BQ().setValeur(this, this.get_StockChoco_BQ().getValeur()-retenue.getMiseAuxEncheres().getQuantiteT());
					//journal.ajouter("vente de "+retenue.getMiseAuxEncheres().getQuantiteT()+" T a "+retenue.getAcheteur().getNom());
				} else {
					//journal.ajouter("pas d'offre retenue");
				}
			}
		}
	}

    @Override
    public Enchere choisir(List<Enchere> propositions) {
        if (propositions.size()>0) {
            return propositions.get(0);
        } else {
            return null;
        }
    }

/* public Enchere choisir(List<Enchere> encheres) {
		this.journal.ajouter("encheres : "+encheres);
		if (encheres==null) {
			return null;
		} else {
			Enchere retenue = encheres.get(0);
			if (retenue.getPrixTonne()>this.prixMin) {
				this.journal.ajouter("  --> je choisis "+retenue);
				return retenue;
			} else {
				this.journal.ajouter("  --> je ne retiens rien");
				return null;
			}
		}
	}
        */
	
 
}

