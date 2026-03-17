package abstraction.eqXRomu.acteurs;

	import java.awt.Color;
	import java.util.ArrayList;
	import java.util.HashMap;
	import java.util.LinkedList;
	import java.util.List;

	import abstraction.eqXRomu.filiere.Filiere;
	import abstraction.eqXRomu.filiere.IActeur;
	import abstraction.eqXRomu.filiere.IFabricantChocolatDeMarque;
	import abstraction.eqXRomu.general.Journal;
	import abstraction.eqXRomu.general.Variable;
	import abstraction.eqXRomu.general.VariablePrivee;
	import abstraction.eqXRomu.produits.Chocolat;
	import abstraction.eqXRomu.produits.ChocolatDeMarque;
	import abstraction.eqXRomu.produits.IProduit;

	/**
	 * Distributeur additionnel trivial (*) representant une faible part
	 * de marche dont le but est de fournir un interlocuteur aux
	 * transformateurs sans attendre que les realisations des equipes
	 *  8 et 9 soient operationnelles.
	 * 
	 * (*) trop trivial puisqu'il ne respecte pas toutes les contraintes imposees,
	 * et c'est pourquoi sa part de marche initiale est faible.
	 */
	public class DistributeurXActeur  implements IActeur, IFabricantChocolatDeMarque {

		protected Journal journal;

		protected int cryptogramme;
		private double coutStockage;

		private List<ChocolatDeMarque>chocosProduits;
		protected HashMap<ChocolatDeMarque, Double> stockChocoMarque;
		protected List<ChocolatDeMarque> chocolatsVillors;
		protected Variable totalStocksChocoMarque;  // La qualite totale de stock de chocolat de marque 


		public DistributeurXActeur() {
			this.chocosProduits = new LinkedList<ChocolatDeMarque>();
			this.journal = new Journal("DX Journal", this);
			this.totalStocksChocoMarque = new VariablePrivee("EqXDStockChocoMarque", "<html>Quantite totale de chocolat de marque en stock</html>",this, 0.0, 1000000.0, 0.0);

		}

		public void initialiser() {
			this.coutStockage = Filiere.LA_FILIERE.getParametre("cout moyen stockage producteur").getValeur()*16;

			this.stockChocoMarque=new HashMap<ChocolatDeMarque,Double>();
			chocosProduits= Filiere.LA_FILIERE.getChocolatsProduits();
			for (ChocolatDeMarque cm : chocosProduits) {
				this.stockChocoMarque.put(cm, 40000.0);
				this.journal.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_BROWN," stock("+cm+")->"+this.stockChocoMarque.get(cm));
				this.totalStocksChocoMarque.ajouter(this,  40000, cryptogramme);
			}
		}

		public String getNom() {
			return "EQXD";
		}

		public String toString() {
			return this.getNom();
		}

		////////////////////////////////////////////////////////
		//         En lien avec l'interface graphique         //
		////////////////////////////////////////////////////////


		public void next() {
			this.journal.ajouter("=== STOCKS === ");
			if (this.stockChocoMarque.keySet().size()>0) {
				for (ChocolatDeMarque cm : this.stockChocoMarque.keySet()) {
					this.journal.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_BROWN,"Stock de "+Journal.texteSurUneLargeurDe(cm+"", 15)+" = "+this.stockChocoMarque.get(cm));
				}
			}
			double montantStockage = (this.totalStocksChocoMarque.getValeur(cryptogramme))*this.coutStockage;
			if (montantStockage>0.0) {
				Filiere.LA_FILIERE.getBanque().payerCout(this, cryptogramme, "Stockage",montantStockage );
			}
			}


		public Color getColor() {
			return new Color(165,165,165);
		}

		public String getDescription() {
			return "Distributeur X";
		}

		public List<Variable> getIndicateurs() {
			List<Variable> res =  new ArrayList<Variable>();
			res.add(totalStocksChocoMarque);
			return res;
		}

		public List<Variable> getParametres() {
			List<Variable> res=new ArrayList<Variable>();
			return res;
		}

		public List<Journal> getJournaux() {
			List<Journal> res=new ArrayList<Journal>();
			res.add(journal);
			return res;
		}

		public void setCryptogramme(Integer crypto) {
			this.cryptogramme = crypto;
		}

		public void notificationFaillite(IActeur acteur) {
		}

		public void notificationOperationBancaire(double montant) {
		}

		protected double getSolde() {
			return Filiere.LA_FILIERE.getBanque().getSolde(Filiere.LA_FILIERE.getActeur(getNom()), this.cryptogramme);
		}

		public List<String> getNomsFilieresProposees() {
			ArrayList<String> filieres = new ArrayList<String>();
			return(filieres);
		}

		public Filiere getFiliere(String nom) {
			return Filiere.LA_FILIERE;
		}

		public double getQuantiteEnStock(IProduit p, int cryptogramme) {
			if (this.cryptogramme==cryptogramme) { // c'est donc bien un acteur assermente qui demande a consulter la quantite en stock
				if (p instanceof ChocolatDeMarque) {
					if (this.stockChocoMarque.keySet().contains(p)) {
						return this.stockChocoMarque.get(p);
					} else {
						return 0.0;
					}
				} else {
					return 0.0;
				}
			} else {
				return 0; // Les acteurs non assermentes n'ont pas a connaitre notre stock
			}
		}


		public List<ChocolatDeMarque> getChocolatsProduits() {
			if (this.chocosProduits.size()==0) {
				for (Chocolat c : Chocolat.values()) {
					int pourcentageCacao =  (int) (Filiere.LA_FILIERE.getParametre("pourcentage min cacao "+c.getGamme()).getValeur());
					this.chocosProduits.add(new ChocolatDeMarque(c, "Villors", pourcentageCacao));
				}
			}
			return this.chocosProduits;
		}
	}
