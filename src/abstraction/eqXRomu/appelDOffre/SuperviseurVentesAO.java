package abstraction.eqXRomu.appelDOffre;

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


public class SuperviseurVentesAO implements IActeur, IAssermente {
	private Journal journal;
	private HashMap<IActeur, Integer> cryptos;
	private Banque laBanque;
	private List<IVendeurAO> vendeurs; 

	public SuperviseurVentesAO() {
		this.journal = new Journal("Journal "+this.getNom(), this);
		this.vendeurs = new LinkedList<IVendeurAO>();
	}
	public String getNom() {
		return "Sup.AO";
	}

	public void initialiser() {
		List<IActeur> acteurs = Filiere.LA_FILIERE.getActeurs();
		for (IActeur a : acteurs) {
			if (a instanceof IVendeurAO) {
				this.vendeurs.add((IVendeurAO)a);
			}
		}
		this.laBanque = Filiere.LA_FILIERE.getBanque();
	}

	/**
	 * 
	 * @param acheteur, l'acheteur appelant cette methode
	 * @param cryptogrammeAcheteur
	 * @param produit le type de produit desire
	 * @param quantiteT en tonnes
	 * @return La propositionVenteOA retenue au terme du processus de vente
	 */
	public OffreVente acheterParAO(IAcheteurAO acheteur, int cryptogrammeAcheteur, IProduit produit, double quantiteT, boolean TG) {
		if (acheteur==null) {
			throw new IllegalArgumentException(" appel de acheterParAO(...) de SuperviseurVentesOA avec null pour acheteur");
		}
		if (produit==null) {
			throw new IllegalArgumentException(" appel de acheterParAO(...) de SuperviseurVentesOA avec null pour chocolat");
		}
		if (!Filiere.LA_FILIERE.getBanque().verifier(acheteur, cryptogrammeAcheteur)) {
			throw new IllegalArgumentException(" appel de acheterParAO(...) de SuperviseurVentesOA par l'acheteur "+acheteur.getNom()+" avec un cryptogramme qui n'est pas le sien");
		}
		if (quantiteT<AppelDOffre.AO_QUANTITE_MIN) {
			throw new IllegalArgumentException(" appel de acheterParAO(...) de SuperviseurVentesOA avec "+quantiteT+" pour quantite (min="+AppelDOffre.AO_QUANTITE_MIN+"))");
		}
		AppelDOffre offre = new AppelDOffre(acheteur, produit, quantiteT, TG);
		journal.ajouter(Journal.texteColore(acheteur, acheteur.getNom())+" veut acheter "+Journal.doubleSur(quantiteT, 2)+ " de "+produit+(TG? " qui sront en tete de gondole":""));

		List<OffreVente> propositions = new LinkedList<OffreVente>();
		if (vendeurs.size()>0) {
			for (IVendeurAO v : vendeurs) {
				OffreVente prop = v.proposerVente(offre);
				if (prop==null || prop.getPrixT()<=0.0) {
					journal.ajouter( Journal.texteColore(v, "   "+v.getNom()+" n'est pas interesse par l'offre "+offre));
				} else {
					journal.ajouter( Journal.texteColore(v, "   "+v.getNom()+" est interesse par l'offre "+offre));
					if (!prop.getVendeur().equals(v)) {
						throw new IllegalArgumentException("la methode proposerVente(...) de "+v.getNom()+" retourne une proposition dont le vendeur n'est pas lui meme");
					} //else if (!prop.getProduit().equals(offre.getProduit())) {
					//	throw new IllegalArgumentException("la methode proposerVente(...) de "+v.getNom()+" retourne une proposition dont le chocolat de marque ne correspond pas au chocolat de l'offre d'achat");
					//}
					propositions.add( prop);
				}
			}
			if (propositions.size()>0) {
				Collections.sort(propositions);
				journal.ajouter(" propositions : "+propositions);
				OffreVente retenue = acheteur.choisirOV(propositions);
				if (retenue != null) {
					journal.ajouter( Journal.texteColore(acheteur, acheteur.getNom()+" choisit la proposition "+retenue));
					// le virement peut se faire car on a verifie au prealable la solvabilite de l'acheteur
					if (!this.laBanque.verifierCapacitePaiement((IActeur)acheteur, cryptos.get((IActeur)acheteur), retenue.getPrixT()*retenue.getOffre().getQuantiteT())) {
						journal.ajouter( Journal.texteColore(acheteur, acheteur.getNom()+" a retenu la proposition "+retenue+" mais ne peut pas payer "+retenue.getPrixT()));
						return null;
					} else { // peut  payer
						this.laBanque.virer(acheteur, this.cryptos.get(acheteur), retenue.getVendeur(), retenue.getPrixT()*retenue.getOffre().getQuantiteT());
						// On notifie l'acheteur que sa proposition a ete retenue afin qu'il mette a jour ses stocks. 
						retenue.getVendeur().notifierVenteAO(retenue);
						Filiere.LA_FILIERE.ajouterEchange(this, this.cryptos.get(this), acheteur, produit, quantiteT, "AO");
						Filiere.LA_FILIERE.ajouterEchange(this, this.cryptos.get(this), retenue.getVendeur(), produit, -quantiteT, "AO");
						// on avertit les autres acheteurs que leur proposition n'a pas ete retenue
						for (OffreVente p : propositions) {
							if (!p.equals(retenue)) {
								p.getVendeur().notifierPropositionNonRetenueAO(p);
							}
						}
					}
				} else {
					journal.ajouter( Journal.texteColore(acheteur, acheteur.getNom()+" ne retient aucune des propositions "));
					// Aucune proposition retenue --> on avertit tous les acheteurs qui ont fait une proposition
					for (OffreVente p : propositions) {
						p.getVendeur().notifierPropositionNonRetenueAO(p);
					}
				}
				return retenue;
			} else {
				journal.ajouter( Journal.texteColore(acheteur, acheteur.getNom()+" n'obtient aucune proposition de vente acceptable"));
				return null;
			}
		} else {
			journal.ajouter(" il n'existe aucun vendeur");
			return null;
		}
	}
	public OffreVente acheterParAO(IAcheteurAO acheteur, int cryptogrammeAcheteur, IProduit produit, double quantiteT) {
		return acheterParAO( acheteur,  cryptogrammeAcheteur,  produit,  quantiteT, false);
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
		if (vendeurs.contains(acteur)) {
			vendeurs.remove(acteur);
			this.journal.ajouter(" le vendeur "+acteur.getNom()+" fait faillite et est retire de la liste des vendeurs par offre d'achat");
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
			return 0; // La acteur non assermente n'ont pas a connaitre notre stock
	}
}
