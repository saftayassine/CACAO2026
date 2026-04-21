package abstraction.eq6Transformateur3;

import java.util.HashMap;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.general.VariablePrivee;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.IProduit;


/**@author : Selma Ben Abdelkader */

/**
 * Classe pour gérer les stocks de Transformateur3
 */
public class Transformateur3Stocks {
	
	private HashMap<Feve, Double> stockFeves;
	private HashMap<Chocolat, Double> stockChoco;
	private HashMap<ChocolatDeMarque, Double> stockChocoMarque;
	private Variable totalStockVolume;
	private Journal journal;
	
	public Transformateur3Stocks(Transformateur3Acteur acteur) {
		this.stockFeves = new HashMap<Feve, Double>();
		this.stockChoco = new HashMap<Chocolat, Double>();
		this.stockChocoMarque = new HashMap<ChocolatDeMarque, Double>();
		this.journal = acteur.journal;
		this.totalStockVolume = new VariablePrivee("Eq6TotalStockVolume", "<html>Volume total de stock (toutes catégories)</html>", acteur, 0.0, 1000000.0, 0.0);
	}
	
	public void initialiserStocks(Transformateur3Acteur acteur, int cryptogramme) {
		// Initialize 
		for (Feve f : Feve.values()) {
			this.stockFeves.put(f, 10000.0);
		}
		for (Chocolat c : Chocolat.values()) {
			this.stockChoco.put(c, 50000.0);
		}
		// Pour ChocolatDeMarque, vous pouvez ajouter des marques spécifiques si nécessaire
		
		updateTotalStockVolume(acteur, cryptogramme);
	}
	
	private void updateTotalStockVolume(Transformateur3Acteur acteur, int cryptogramme) {
		double total = 0.0;
		for (Double q : this.stockFeves.values()) {
			total += q;
		}
		for (Double q : this.stockChoco.values()) {
			total += q;
		}
		for (Double q : this.stockChocoMarque.values()) {
			total += q;
		}
		this.totalStockVolume.setValeur(acteur, total, cryptogramme);
	}
	
	public double getQuantiteEnStock(IProduit p) {
		if (p instanceof Feve) {
			return this.stockFeves.getOrDefault((Feve)p, 0.0);
		} else if (p instanceof Chocolat) {
			return this.stockChoco.getOrDefault((Chocolat)p, 0.0);
		} else if (p instanceof ChocolatDeMarque) {
			return this.stockChocoMarque.getOrDefault((ChocolatDeMarque)p, 0.0);
		} else {
			return 0.0;
		}
	}
	
	public void ajouterStock(IProduit p, double quantite, Transformateur3Acteur acteur, int cryptogramme) {
		if (p instanceof Feve) {
			this.stockFeves.put((Feve)p, this.stockFeves.getOrDefault((Feve)p, 0.0) + quantite);
			this.journal.ajouter("Ajout de " + quantite + " kg de " + p.getType() + " " + ((Feve)p).getGamme() + " " + (((Feve)p).isEquitable() ? "équitable" : "non équitable"));
		} else if (p instanceof Chocolat) {
			this.stockChoco.put((Chocolat)p, this.stockChoco.getOrDefault((Chocolat)p, 0.0) + quantite);
			this.journal.ajouter("Ajout de " + quantite + " kg de " + p.getType() + " " + ((Chocolat)p).getGamme() + " " + (((Chocolat)p).isEquitable() ? "équitable" : "non équitable"));
		} else if (p instanceof ChocolatDeMarque) {
			this.stockChocoMarque.put((ChocolatDeMarque)p, this.stockChocoMarque.getOrDefault((ChocolatDeMarque)p, 0.0) + quantite);
			this.journal.ajouter("Ajout de " + quantite + " kg de " + p.getType() + " " + ((ChocolatDeMarque)p).getMarque());
		}
		updateTotalStockVolume(acteur, cryptogramme);
	}
	
	public boolean retirerStock(IProduit p, double quantite, Transformateur3Acteur acteur, int cryptogramme) {
		double currentStock = getQuantiteEnStock(p);
		if (currentStock >= quantite) {
			if (p instanceof Feve) {
				this.stockFeves.put((Feve)p, currentStock - quantite);
				this.journal.ajouter("Retrait de " + quantite + " kg de " + p.getType() + " " + ((Feve)p).getGamme() + " " + (((Feve)p).isEquitable() ? "équitable" : "non équitable"));
			} else if (p instanceof Chocolat) {
				this.stockChoco.put((Chocolat)p, currentStock - quantite);
				this.journal.ajouter("Retrait de " + quantite + " kg de " + p.getType() + " " + ((Chocolat)p).getGamme() + " " + (((Chocolat)p).isEquitable() ? "équitable" : "non équitable"));
			} else if (p instanceof ChocolatDeMarque) {
				this.stockChocoMarque.put((ChocolatDeMarque)p, currentStock - quantite);
				this.journal.ajouter("Retrait de " + quantite + " kg de " + p.getType() + " " + ((ChocolatDeMarque)p).getMarque());
			}
			updateTotalStockVolume(acteur, cryptogramme);
			return true;
		}
		return false;
	}
	
	public Variable getTotalStockVolume() {
		return this.totalStockVolume;
	}
	
	public HashMap<Feve, Double> getStockFeves() {
		return new HashMap<>(this.stockFeves);
	}
	
	public HashMap<Chocolat, Double> getStockChoco() {
		return new HashMap<>(this.stockChoco);
	}
	
	public HashMap<ChocolatDeMarque, Double> getStockChocoMarque() {
		return new HashMap<>(this.stockChocoMarque);
	}
}