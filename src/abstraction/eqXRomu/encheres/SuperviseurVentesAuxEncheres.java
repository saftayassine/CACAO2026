package abstraction.eqXRomu.encheres;

import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import abstraction.eqXRomu.filiere.Banque;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IActeur;
import abstraction.eqXRomu.filiere.IAssermente;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.produits.IProduit;

public class SuperviseurVentesAuxEncheres implements IActeur, IAssermente {
	private Journal journal;
	private HashMap<IActeur, Integer> cryptos;
	private Banque laBanque;
	private List<IAcheteurAuxEncheres> acheteurs; 

	public SuperviseurVentesAuxEncheres() {
		this.journal = new Journal("Journal "+this.getNom(), this);
		this.acheteurs = new LinkedList<IAcheteurAuxEncheres>();
	}
	public String getNom() {
		return "Sup.Encheres";
	}

	public void initialiser() {
		List<IActeur> acteurs = Filiere.LA_FILIERE.getActeurs();
		for (IActeur a : acteurs) {
			if (a instanceof IAcheteurAuxEncheres) {
				this.acheteurs.add((IAcheteurAuxEncheres)a);
			}
		}
		this.laBanque = Filiere.LA_FILIERE.getBanque();
	}

	/**
	 * Sans precision de la tete de gondole, c'est implicitement sans mise en tete de gondole
	 * @param vendeur
	 * @param cryptogrammeVendeur
	 * @param produit
	 * @param quantiteT
	 * @param TG
	 * @return
	 */
	public Enchere vendreAuxEncheres(IVendeurAuxEncheres vendeur, int cryptogrammeVendeur, IProduit produit, double quantiteT) {
		return vendreAuxEncheres( vendeur,  cryptogrammeVendeur,  produit,  quantiteT,false);
	}

/**
 * 
 * @param vendeur
 * @param cryptogrammeVendeur
 * @param produit le produit de la proposition
 * @param quantiteT doit etre superieur a MiseAuxEncheres.QUANTITE_MIN
 * @return
 */
	public Enchere vendreAuxEncheres(IVendeurAuxEncheres vendeur, int cryptogrammeVendeur, IProduit produit, double quantiteT, boolean TG) {
		if (vendeur==null) {
			throw new IllegalArgumentException(" appel de vendreAuxEncheres(...) de SuperviseurVentesAuxEncheres avec null pour vendeur");
		}
		if (produit==null) {
			throw new IllegalArgumentException(" appel de vendreAuxEncheres(...) de SuperviseurVentesAuxEncheres avec null pour produit");
		}
		if (!Filiere.LA_FILIERE.getBanque().verifier(vendeur, cryptogrammeVendeur)) {
			throw new IllegalArgumentException(" appel de vendreAuxEncheres(...) de SuperviseurVentesAuxEncheres par le vendeur "+vendeur.getNom()+" avec un cryptogramme qui n'est pas le sien");
		}
		if (quantiteT<MiseAuxEncheres.QUANTITE_MIN) {
			throw new IllegalArgumentException(" appel de vendreAuxEncheres(...) de SuperviseurVentesAuxEncheres avec "+quantiteT+" pour quantite (min="+MiseAuxEncheres.QUANTITE_MIN+"))");
		}
		MiseAuxEncheres miseAuxEncheres = new MiseAuxEncheres(vendeur, produit, quantiteT, TG);
		journal.ajouter(Journal.texteColore(vendeur, vendeur.getNom())+" veut vendre aux encheres "+Journal.doubleSur(quantiteT, 2)+ " de "+produit+(TG?" en tete de gondole":""));

		List<Enchere> encheres = new LinkedList<Enchere>();
		if (acheteurs.size()>0) {
			for (IAcheteurAuxEncheres a : acheteurs) {
				double prix = a.proposerPrix(miseAuxEncheres);
				if (prix<=0.0) {
					journal.ajouter( Journal.texteColore(a, a.getNom()+" n'est pas interesse par l'offre "+miseAuxEncheres));
				} else {
					if (this.laBanque.verifierCapacitePaiement((IActeur)a, cryptos.get((IActeur)a), prix*quantiteT)) {
						journal.ajouter( Journal.texteColore(a, a.getNom()+" propose un prix de "+prix));
						encheres.add( new Enchere(miseAuxEncheres, a, prix));
					} else { // a ne peut pas payer
						journal.ajouter( Journal.texteColore(a, a.getNom()+" a propose un prix de "+prix+" mais n'est pas en mesure de payer un tel prix"));
					}
				}
			}
			if (encheres.size()>0) {
				Collections.sort(encheres);
				Collections.reverse(encheres);
				journal.ajouter(" propositions : "+encheres);
				Enchere retenue = vendeur.choisir(encheres);
				if (retenue != null) {
					journal.ajouter( Journal.texteColore(vendeur, vendeur.getNom()+" choisit l'enchere "+retenue));
					// le virement peut se faire car on a verifie au prealable la solvabilite de l'acheteur
					this.laBanque.virer(retenue.getAcheteur(), this.cryptos.get(retenue.getAcheteur()), vendeur, retenue.getPrixTonne()*retenue.getMiseAuxEncheres().getQuantiteT());
					// On notifie l'acheteur que sa proposition a ete retenue afin qu'il mette a jour ses stocks. 
					retenue.getAcheteur().notifierAchatAuxEncheres(retenue);
					Filiere.LA_FILIERE.ajouterEchange(this, this.cryptos.get(this), retenue.getAcheteur(), produit, quantiteT, "enchere");
					Filiere.LA_FILIERE.ajouterEchange(this, this.cryptos.get(this), vendeur, produit, -quantiteT, "enchere");

					// on avertit les autres acheteurs que leur proposition n'a pas ete retenue
					for (Enchere p : encheres) {
						if (!p.equals(retenue)) {
							p.getAcheteur().notifierEnchereNonRetenue(p);
						}
					}
				} else {
					// Aucune proposition retenue --> on avertit tous les acheteurs qui ont fait une proposition
					for (Enchere p : encheres) {
						p.getAcheteur().notifierEnchereNonRetenue(p);
					}
				}
				return retenue;
			} else {
				journal.ajouter( Journal.texteColore(vendeur, vendeur.getNom()+" ne retient aucune proposition "));
				return null;
			}
		} else {
			journal.ajouter(" il n'existe aucun acheteur");
			return null;
		}
	}

	public void next() {
	}

	public String getDescription() {
		return this.getNom();
	}

	public Color getColor() {
		return new Color(240, 240, 240);
	}

	public List<String> getNomsFilieresProposees() {
		return new LinkedList<String>();
	}

	public Filiere getFiliere(String nom) {
		return null;
	}

	public List<Variable> getIndicateurs() {
		return new LinkedList<Variable>();
	}

	public List<Variable> getParametres() {
		return new LinkedList<Variable>();
	}

	public List<Journal> getJournaux() {
		List<Journal> res = new LinkedList<Journal>();
		res.add(this.journal);
		return res;
	}

	public void setCryptogramme(Integer crypto) {
	}

	public void notificationFaillite(IActeur acteur) {
		if (acheteurs.contains(acteur)) {
			acheteurs.remove(acteur);
			this.journal.ajouter(" l'acheteur "+acteur.getNom()+" fait faillite et est retire de la liste des achateurs aux encheres");
		}
	}

	public void notificationOperationBancaire(double montant) {
	}

	public void setCryptos(HashMap<IActeur, Integer> cryptos) {
		if (this.cryptos==null) { // Les cryptogrammes ne sont indique qu'une fois par la banque : si la methode est appelee une seconde fois c'est que l'auteur de l'appel n'est pas la banque et qu'on cherche a "pirater" l'acteur
			this.cryptos= cryptos;
		}
	}
	
	public double getQuantiteEnStock(IProduit p, int cryptogramme) {
		return 0;
	}
}
