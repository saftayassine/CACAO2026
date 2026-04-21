package abstraction.eq2Producteur2;

import java.util.HashMap;
import java.util.List;

import abstraction.eqXRomu.bourseCacao.IVendeurBourse;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.IProduit;

/**@author Simon */

public class Producteur2Bourse extends Sechage implements IVendeurBourse {
	protected HashMap<Feve, Variable> stocks;
	protected Journal journalBourse;

	public Producteur2Bourse() {
		super();
		this.stocks = new HashMap<Feve, Variable>();
		for (Feve f : Feve.values()) {
			this.stocks.put(f, new Variable("Stock " + f, this, 0.0));
			this.stocks.get(f).setValeur(this, this.stock_initial.get(f));
		}
		this.journalBourse = new Journal("Journal Bourse Eq2", this);
	}

	@Override
	public void next() {
		super.next();
		for (Feve f : Feve.values()) {
			Variable v = this.stockvar.get(f);
			Variable stockBourse = this.stocks.get(f);
			if (v != null && stockBourse != null) {
				stockBourse.setValeur(this, v.getValeur());
			}
		}
	}

	@Override
	public double offre(Feve f, double cours) {
		this.setStockMin(0.1);

		double offre = 0;

		if (this.stockvar.containsKey(f) && this.cout_unit_t.containsKey(f) && this.seuil_stock.containsKey(f)) {
			
			double stockActuel = this.stockvar.get(f).getValeur();
			double quantiteAGarder = this.restantDu(f);
			
			// Calcul du prix minimal voulu : marge de 20% (x1.2) par défaut
			double marge = 1.2;
			
			double prixMinimal = this.cout_unit_t.get(f) * marge;

			this.journalBourse.ajouter("Valeur du cours de la feve " + f + " : " + cours + "\nValeur du prix minimal voulu : " + prixMinimal);

			if ((stockActuel - quantiteAGarder > this.seuil_stock.get(f)) && (prixMinimal < cours)) {
				offre = stockActuel - quantiteAGarder - this.seuil_stock.get(f);
				this.journalBourse.ajouter(Filiere.LA_FILIERE.getEtape() + " Je mets en vente " + offre + " T de " + f);
			}
		}

		return offre;
	}

	public double restantDu(Feve f) {
		return 0.0;
	}

	@Override
	public double notificationVente(Feve f, double quantiteEnT, double coursEnEuroParT) {
		double retire = this.retirerDuStock(f, quantiteEnT);
		Variable stockBourse = this.stocks.get(f);
		Variable stockInterne = this.stockvar.get(f);
		if (stockBourse != null && stockInterne != null) {
			stockBourse.setValeur(this, stockInterne.getValeur());
		}
		this.journalBourse.ajouter(
				Filiere.LA_FILIERE.getEtape() + " : vente de " + quantiteEnT + " T de " + f + " -> retrait effectif " + retire);
		return retire;
	}

	@Override
	public void notificationBlackList(int dureeEnStep) {
		this.journalBourse
				.ajouter(Filiere.LA_FILIERE.getEtape() + " : blacklist bourse pendant " + dureeEnStep + " step(s)");
	}

	@Override
	public List<Journal> getJournaux() {
		List<Journal> res = super.getJournaux();
		res.add(this.journalBourse);
		return res;
	}

	@Override
	public double getQuantiteEnStock(IProduit p, int cryptogramme) {
		if (this.cryptogramme == cryptogramme && p instanceof Feve) {
			Variable v = this.stocks.get((Feve) p);
			return v != null ? v.getValeur(this.cryptogramme) : 0.0;
		}
		return 0.0;
	}
}