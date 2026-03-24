package abstraction.eqXRomu.filiere;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import abstraction.eqXRomu.bourseCacao.IAcheteurBourse;
import abstraction.eqXRomu.bourseCacao.IVendeurBourse;
import abstraction.eqXRomu.clients.ClientFinal;
import abstraction.eqXRomu.contratsCadres.ContratCadre;
import abstraction.eqXRomu.general.Courbe;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.general.VariableReadOnly;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.IProduit;
import presentation.FenetrePrincipale;
import presentation.secondaire.FenetreGraphique;

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Classe modelisant une filiere vue comme un regroupement d'acteurs, 
 * d'indicateurs, de parametres et de journaux. 
 * 
 * Les acteurs/indicateurs/parametres/journaux que vous creerez devront etre
 * ajoutes a l'unique instance de cette classe designee par la 
 * variable LA_FILIERE. 
 *
 * @author Romuald Debruyne
 */
public class Filiere implements IAssermente {

	public static Random random;

	public static Filiere LA_FILIERE; // Filiere.LA_FILIERE reference l'unique instance de Filiere
	public static double SEUIL_EN_TETE_DE_GONDOLE_POUR_IMPACT = 0.05; // Si les produits d'une marque m representent au moins 5% de la quantite totale en tete de gondole alors il y a un impact positif sur l'attrait de la marque.
	private static final int DUREE_HISTORIQUE_ECHANGES = 12;

	// le carcul des parts de marche s'appuie sur les achats/ventes enregistrees sur les 
	// DUREE_EN_ETAPES_CALCUL_PARTS_DE_MARCHE dernieres etapes.
	private static final int DUREE_EN_ETAPES_CALCUL_PARTS_DE_MARCHE = 6;



	private int etape;                   // Le numero d'etape en cours
	private Banque laBanque;             // L'unique banque. Tous les acteurs ont un compte a cette banque
	private List<IActeur> acteurs;       // La liste des acteurs
	private List<IActeur> acteursSolvables;// La liste des acteurs n'ayant pas fait faillite
	private List<ClientFinal> clientsFinaux;
	private HashMap<String, Variable> indicateurs;// Table associant a chaque nom d'indicateur la variable qui la represente
	private HashMap<IActeur, List<Variable>> indicateursParActeur; // Table associant a chaque acteur sa liste d'indicateurs
	private HashMap<String, Variable> parametres;// Table associant a chaque nom de parametre la variable qui la represente
	private HashMap<IActeur, List<Variable>> parametresParActeur; // Table associant a chaque acteur sa liste de parametres
	private HashMap<String, Journal> journaux;      // La liste des journaux
	private HashMap<IActeur, List<Journal>> journauxParActeur; // Table associant a chaque acteur sa liste de journaux
	private PropertyChangeSupport pcs;        // Pour notifier les observers des changements de step 
	protected Journal journalFiliere;
	private HashMap<String, IActeur> marquesDeposees; // Associe a chaque marque de chocolat deposee l'acteur qui la possede
	//private List<IFabricantChocolatDeMarque> fabricantsDeChocolatDeMarque; // Tous
	private List<ChocolatDeMarque> chocolatsProduits; // La liste de tous les types de chocolat de marque produits.
	private HashMap<ChocolatDeMarque, List<IFabricantChocolatDeMarque>> fabricantsChocolatDeMarque; // Associe a chaque chocolat de marque la liste des acteurs qui le produise 
	private HashMap<String, Double> qualiteMoyenneMarque; // Associe a une marque la qualite moyenne des produits portant cette marque
	private List<Echange> echanges; // Historique des echanges 
	private HashMap<IActeur, HashMap<Feve, VariableReadOnly>> ventesFeves, achatsFeves;
	private HashMap<IActeur, HashMap<Chocolat, VariableReadOnly>> ventesChocolats, achatsChocolats, ventesChocolatsMarques;


	// Influence des tetes de gondole sur la notoriete des marques
	private List<String> presenceEnTG; // A chaque step, si les produits d'une marque representent au moins 5% des quantites mis 
	//en vente en tete de gondole alors la marque est ajoutee a cette liste. Seuls les 100 derniers ajouts sont conserves.
	private HashMap<String, Integer> nbPresencesEnTg; // nombre d'occurrence de la marque dans la liste presenceEnTG;
	private HashMap<IActeur, Integer> cryptos;
	public HashMap<String, Long> tempsEquipes = new HashMap<String, Long>();
	private HashMap<Feve, FenetreGraphique> graphiqueVentesFeves, graphiqueAchatsFeves;
	private HashMap<Chocolat, FenetreGraphique> graphiqueVentesChocolats, graphiqueAchatsChocolats, graphiqueVentesChocolatsMarque;
	private HashMap<Feve, HashMap<IActeur, Courbe>> courbeVentesFeves, courbeAchatsFeves;
    private HashMap<Chocolat, HashMap<IActeur, Courbe>> courbeVentesChocolats, courbeAchatsChocolats, courbeVentesChocolatsMarque;
	/**
	 * Initialise la filiere de sorte que le numero d'etape soit 0, 
	 * et qu'il n'y ait pour l'heure que la Banque pour unique acteur. 
	 * Les constructeurs des sous-classes de Filiere devront ajouter les autres acteurs
	 */
	public Filiere(long seed) {
		this.etape=0;
		Filiere.random=new Random(seed);
		this.acteurs=new ArrayList<IActeur>();
		this.acteursSolvables=new ArrayList<IActeur>();
		this.clientsFinaux=new ArrayList<ClientFinal>();
		this.indicateurs=new HashMap<String, Variable>();
		this.parametres=new HashMap<String, Variable>();
		this.indicateursParActeur=new HashMap<IActeur, List<Variable>>();
		this.parametresParActeur=new HashMap<IActeur, List<Variable>>();
		this.journaux=new HashMap<String, Journal>();
		this.journauxParActeur=new HashMap<IActeur, List<Journal>>();
		this.pcs = new  PropertyChangeSupport(this);
		this.laBanque = new Banque();
		this.journalFiliere = this.laBanque.getJournaux().get(0);
		this.marquesDeposees = new HashMap<String, IActeur>();
		this.chocolatsProduits = new ArrayList<ChocolatDeMarque>();
		this.fabricantsChocolatDeMarque = new HashMap<ChocolatDeMarque, List<IFabricantChocolatDeMarque>>();
		this.qualiteMoyenneMarque = new HashMap<String, Double>();
		this.presenceEnTG = new LinkedList<String>();
		this.nbPresencesEnTg = new HashMap<String, Integer>();
		this.echanges = new ArrayList<Echange>();
		this.ventesFeves = new HashMap<IActeur, HashMap<Feve, VariableReadOnly>>();
		this.achatsFeves = new HashMap<IActeur, HashMap<Feve, VariableReadOnly>>();	
		this.ventesChocolats = new HashMap<IActeur, HashMap<Chocolat, VariableReadOnly>>();
		this.achatsChocolats = new HashMap<IActeur, HashMap<Chocolat, VariableReadOnly>>();
		this.ventesChocolatsMarques = new HashMap<IActeur, HashMap<Chocolat, VariableReadOnly>>();
		this.ajouterActeur(this.laBanque);
	}


	public void initialiser() {
		// Depot des marques de chocolat
		for (IActeur a : this.acteurs) {
			if (a instanceof IMarqueChocolat) {
				List<String> marques = ((IMarqueChocolat)a).getMarquesChocolat();
				if (marques!=null && marques.size()>0) {
					if ((a instanceof IDistributeurChocolatDeMarque) && marques.size()>1) {
						throw new IllegalStateException("Le distributeur "+a.getNom()+" a une methode getMarquesChocolat qui retourne une liste de longueur "+marques.size()+" (un distribteur ne peut avoir qu'une marque de chocolat)");
					}
					for (String m : marques) {
						if (this.marquesDeposees.get(m)!=null) {
							throw new IllegalStateException("L'acteur "+a.getNom()+" tente de deposer la marque "+m+" qui est deja la propriete de "+marquesDeposees.get(m).getNom());
						} else {
							this.marquesDeposees.put(m, a);
						}
					}
				}
			}
			tempsEquipes.put(a.getNom(), (long) 0);
		}
		this.journalFiliere.ajouter("Marques deposees : "+this.marquesDeposees);
		this.journalFiliere.ajouter("Marques distributeurs : "+this.getMarquesDistributeur());

		// Determination de la liste de tous les chocolats de marques produits

		List<IFabricantChocolatDeMarque> fabricants = new ArrayList<IFabricantChocolatDeMarque>();
		for (IActeur a : this.acteurs) {
			if ((a instanceof IFabricantChocolatDeMarque)) {
				fabricants.add((IFabricantChocolatDeMarque)a);
			}
		}
		for (IFabricantChocolatDeMarque f : fabricants) {
			List<ChocolatDeMarque> produits = f.getChocolatsProduits();
			if (produits==null || produits.size()==0) {
				System.out.println(""+f+" est un IFabricantChocolatDeMarque ne fabriquant aucun produit");
			} else {
				for (ChocolatDeMarque c : produits) {
					if (!this.chocolatsProduits.contains(c)) {
						this.chocolatsProduits.add(c);
					}
				}
			}
		}
		this.journalFiliere.ajouter("Chocolats de marque produits : "+this.chocolatsProduits);

		// Association a chaque chocolat de marque produit de la liste des acteurs qui le produisent
		for (ChocolatDeMarque c : this.chocolatsProduits) {
			this.fabricantsChocolatDeMarque.put(c,  new ArrayList<IFabricantChocolatDeMarque>());
		}
		for (IFabricantChocolatDeMarque f : fabricants) {
			List<ChocolatDeMarque> produits = f.getChocolatsProduits();
			if (produits==null || produits.size()==0) {
				System.out.println(f+" est un IFabricantChocolatDeMarque qui ne produit aucun chocolat");
			} else {
				for (ChocolatDeMarque c : produits) {
					this.fabricantsChocolatDeMarque.get(c).add(f);
				}
			}
		}
		for (ChocolatDeMarque c : this.chocolatsProduits) {
			this.journalFiliere.ajouter("Producteurs de "+c+" : "+this.fabricantsChocolatDeMarque.get(c));
		}


		// Association a chaque marque de la qualite moyenne des produits portant cette marque
		for (String marque : getMarquesChocolat()) {
			int nbProduits=0;
			double totalQualites=0.0;
			for (ChocolatDeMarque choco : chocolatsProduits) {
				if (choco.getMarque().equals(marque)) {
					totalQualites+=choco.getChocolat().qualite();
					nbProduits++;
				}
			}
			if (nbProduits==0) {
				throw new IllegalStateException("la marque "+marque+" est deposee mais il n'y a aucune production d'un chocolat portant cette marque");
			}
			this.qualiteMoyenneMarque.put(marque, totalQualites/nbProduits);
			this.journalFiliere.ajouter("qualite moyenne de la marque "+marque+" = "+this.qualiteMoyenneMarque.get(marque));
		}

		// Creation des variable memorisant les volumes de ventes/achats des differents acteurs pour les differents produits (utile pour le calcul des parts de marche)
			IActeur romu = getActeur("EQX");
			for (IActeur acteur : this.getActeurs()) {
				if (acteur instanceof IVendeurBourse) { // un producteur
					this.ventesFeves.put(acteur, new HashMap<Feve, VariableReadOnly>());
					for (Feve ff : Feve.values()) {
					    this.ventesFeves.get(acteur).put(ff, new VariableReadOnly("VF_"+acteur.getNom()+"_"+ff.name(), romu,0.0 ));
						//m.out.println("creation variable ventesFeves "+acteur+" "+ff);
					}
				} else if (acteur instanceof IMarqueChocolat || acteur instanceof IAcheteurBourse) { // un transformateur
					this.achatsFeves.put(acteur, new HashMap<Feve, VariableReadOnly>());
					for (Feve ff : Feve.values()) {
					    this.achatsFeves.get(acteur).put(ff, new VariableReadOnly("AF_"+acteur.getNom()+"_"+ff.name(), romu,0.0 ));
						//System.out.println("creation variable achatssFeves "+acteur+" "+ff);
					}
					this.ventesChocolats.put(acteur, new HashMap<Chocolat, VariableReadOnly>());
					for (Chocolat cc : Chocolat.values()) {
					    this.ventesChocolats.get(acteur).put(cc, new VariableReadOnly("VC_"+acteur.getNom()+"_"+cc.name(), romu,0.0 ));
						//System.out.println("creation variable ventesChocolats "+acteur+" "+cc);
					}
				} else if (acteur instanceof IDistributeurChocolatDeMarque) {
					this.achatsChocolats.put(acteur, new HashMap<Chocolat, VariableReadOnly>());
					this.ventesChocolatsMarques.put(acteur, new HashMap<Chocolat, VariableReadOnly>());	
					for (Chocolat cc : Chocolat.values()) {
					    this.achatsChocolats.get(acteur).put(cc, new VariableReadOnly("AC_"+acteur.getNom()+"_"+cc.name(), romu,0.0 ));
						//System.out.println("creation variable achatsChocolats "+acteur+" "+cc);
					    this.ventesChocolatsMarques.get(acteur).put(cc, new VariableReadOnly("VCM_"+acteur.getNom()+"_"+cc.name(), romu,0.0 ));
						//System.out.println("creation variable ventesChocolatsMarques "+acteur+" "+cc);
					}				
				}
			}
		

		// Creation des courbes pour les parts de marche
	this.graphiqueVentesFeves=new HashMap<Feve, FenetreGraphique>();
	this.graphiqueAchatsFeves=new HashMap<Feve, FenetreGraphique>();
	this.graphiqueVentesChocolats=new HashMap<Chocolat, FenetreGraphique>();
	this.graphiqueAchatsChocolats=new HashMap<Chocolat, FenetreGraphique>();
	this.graphiqueVentesChocolatsMarque=new HashMap<Chocolat, FenetreGraphique>();
	this.courbeVentesFeves=new HashMap<Feve, HashMap<IActeur, Courbe>>();
	this.courbeAchatsFeves=new HashMap<Feve, HashMap<IActeur, Courbe>>();
	this.courbeVentesChocolats=new HashMap<Chocolat, HashMap<IActeur, Courbe>>();
	this.courbeAchatsChocolats=new HashMap<Chocolat, HashMap<IActeur, Courbe>>();
	this.courbeVentesChocolatsMarque=new HashMap<Chocolat, HashMap<IActeur, Courbe>>();

	int numMarque=0;
	for (Feve ff : Feve.values()) {
		this.graphiqueVentesFeves.put(ff, new FenetreGraphique("Parts de marche des ventes de feve "+ff, 500,400));
		this.graphiqueAchatsFeves.put(ff, new FenetreGraphique("Parts de marche des achats de feve "+ff, 500,400));
		this.courbeVentesFeves.put(ff,new HashMap<IActeur, Courbe>());
		this.courbeAchatsFeves.put(ff,new HashMap<IActeur, Courbe>());
		for (IActeur acteur : this.getActeurs()) {
				if (acteur instanceof IVendeurBourse) { // un producteur
					Courbe c = new Courbe("PM_"+acteur+"_"+ff);
					c.setCouleur(acteur.getColor());
					c.setMarque(numMarque);
					numMarque+=43;
					this.courbeVentesFeves.get(ff).put(acteur, c);
					this.graphiqueVentesFeves.get(ff).ajouter(c);
				} else if (acteur instanceof IMarqueChocolat || acteur instanceof IAcheteurBourse) {
					Courbe c = new Courbe("PM_"+acteur+"_"+ff);
					c.setCouleur(acteur.getColor());
					c.setMarque(numMarque);
					numMarque+=43;
					this.courbeAchatsFeves.get(ff).put(acteur, c);
					this.graphiqueAchatsFeves.get(ff).ajouter(c);
				}
		}

	}
	for (Chocolat cc : Chocolat.values()) {
		this.graphiqueVentesChocolats.put(cc, new FenetreGraphique("Parts de marche des ventes de chocolat "+cc, 500,400));
		this.graphiqueAchatsChocolats.put(cc, new FenetreGraphique("Parts de marche des achats de chocolat "+cc, 500,400));
		this.graphiqueVentesChocolatsMarque.put(cc, new FenetreGraphique("Parts de marche des ventes de chocolat de marque "+cc, 500,400));		
		this.courbeVentesChocolats.put(cc,new HashMap<IActeur, Courbe>());
		this.courbeAchatsChocolats.put(cc,new HashMap<IActeur, Courbe>());
		this.courbeVentesChocolatsMarque.put(cc,new HashMap<IActeur, Courbe>());
		for (IActeur acteur : this.getActeurs()) {
				if (acteur instanceof IMarqueChocolat || acteur instanceof IAcheteurBourse) { // un producteur
					Courbe c = new Courbe("PM_"+acteur+"_"+cc);
					c.setCouleur(acteur.getColor());
					c.setMarque(numMarque);
					numMarque+=43;
					this.courbeVentesChocolats.get(cc).put(acteur, c);
					this.graphiqueVentesChocolats.get(cc).ajouter(c);
				} else if (acteur instanceof IDistributeurChocolatDeMarque) {
					Courbe c = new Courbe("PM_"+acteur+"_"+cc);
					c.setCouleur(acteur.getColor());
					c.setMarque(numMarque);
					numMarque+=43;
					this.courbeAchatsChocolats.get(cc).put(acteur, c);
					this.graphiqueAchatsChocolats.get(cc).ajouter(c);
					Courbe c2 = new Courbe("PM_"+acteur+"_"+cc);
					c2.setCouleur(acteur.getColor());
					c2.setMarque(numMarque);
					numMarque+=43;
					//System.out.println(" distributeur "+acteur+" graphique vente chocoloat marque "+cc);
					this.courbeVentesChocolatsMarque.get(cc).put(acteur, c2);
					this.graphiqueVentesChocolatsMarque.get(cc).ajouter(c2);
				}
		}

	}


		// Initialisation de la presence en TG
		for (String marque : getMarquesChocolat()) {
			this.nbPresencesEnTg.put(marque, 0);
		}

		// Initialisation de chaque acteur
		this.journalFiliere.ajouter("Initialiser()");
		for (IActeur a : this.acteurs) {
			a.initialiser();
		}
	}



	public List<FenetreGraphique> getGraphiques() {
		List<FenetreGraphique> res = new ArrayList<FenetreGraphique>();
		for (Feve f : graphiqueVentesFeves.keySet()) {
			res.add(graphiqueVentesFeves.get(f));
		}
		for (Feve f : graphiqueAchatsFeves.keySet()) {
			res.add(graphiqueAchatsFeves.get(f));
		}
		for (Chocolat c : graphiqueVentesChocolats.keySet()) {
			res.add(graphiqueVentesChocolats.get(c));
		}
		for (Chocolat c : graphiqueAchatsChocolats.keySet()) {
			res.add(graphiqueAchatsChocolats.get(c));
		}
		for (Chocolat c : graphiqueVentesChocolatsMarque.keySet()) {
			res.add(graphiqueVentesChocolatsMarque.get(c));
		}
		return res;
	}
	public VariableReadOnly getVentesFeves(IActeur acteur, Feve f ) {
		return this.ventesFeves.get(acteur).get(f);
	}
	/**
	 * @return La liste de toutes les marques de chocolat deposees
	 */
	public List<String> getMarquesChocolat() {
		return new ArrayList<String>(this.marquesDeposees.keySet());
	}

	/**
	 * 
	 * @return La liste des marques deposees par un distributeur
	 */
	public List<String> getMarquesDistributeur() {
		ArrayList<String> marquesDistri = new ArrayList<String>();
		List<String> marques = getMarquesChocolat();
		for (String marque :marques) {
			if (getProprietaireMarque(marque) instanceof IDistributeurChocolatDeMarque) {
				marquesDistri.add(marque);
			}
		}
		return marquesDistri;
	}

	/**
	 * @param marque
	 * @return Le proprietaire de la marque precisee en parametre si il s'agit d'une marque deposee (null si le parametre n'est pas une marque deposee)
	 */
	public IActeur getProprietaireMarque(String marque) {
		return this.marquesDeposees.get(marque);
	}

	/**
	 * @return Retourne l'unique instance de Banque de la filiere
	 */
	public Banque getBanque() {
		return this.laBanque;
	}


	/**
	 * @return Retourne le numero de l'etape en cours.
	 */
	public int getEtape() {
		return this.etape;
	}

	/** @param etape etape>=0
	 * @return Retourne l'annee de l'etape precisee en parametre */
	public int getAnnee(int etape) {
		return 2026+etape/24;
	}
	/** @return Retourne l'annee de l'etape courante */
	public int getAnnee() {
		return 2026+this.etape/24;
	}

	/** @return Le numero du mois de l'etape precisee en parametre
	 *  (1==Janvier, 2==Fevrier, ... 12==Decembre	 */
	public int getNumeroMois(int etape) {
		return 1+(etape%24)/2;
	}
	/** @return Le numero du mois courant (1==Janvier, 2==Fevrier, ... 12==Decembre	 */
	public int getNumeroMois() {
		return getNumeroMois(this.etape);
	}
	/** @return Retourne le nom du mois de l'etape precisee en parametre */
	public String getMois(int etape) {
		String[] mois= {"janvier", "fevrier", "mars", "avril", "mai", "juin", "juillet", "aout", "septembre", "octobre", "novembre", "decembre"};
		return mois[getNumeroMois(etape)-1];
	}
	/** @return Retourne le nom du mois de l'etape courante */
	public String getMois() {
		String[] mois= {"janvier", "fevrier", "mars", "avril", "mai", "juin", "juillet", "aout", "septembre", "octobre", "novembre", "decembre"};
		return mois[getNumeroMois()-1];
	}

	/** @return Retourne le numero du jour de l'etape precisee en parametre 
	 * (1==debut de mois, 15==milieu de mois) 	 */
	public int getJour(int etape) {
		return (etape%2==0 ? 1 : 15);
	}
	/** @return Retourne le numero du jour de l'etape courante 	 */
	public int getJour() {
		return getJour(this.etape);
	}

	/** @return Retourne une chaine de caracteres correspond a la date de l'etape precisee en parametre	 */
	public String getDate(int etape) {
		return this.getJour(etape)+" "+this.getMois(etape)+" "+this.getAnnee(etape);
	}
	/** @return Retourne une chaine de caracteres correspond a la date de l'etape courante	*/
	public String getDate() {
		return this.getJour()+" "+this.getMois()+" "+this.getAnnee();
	}

	/**
	 * Ajoute l'acteur ac a la filiere si il n'existe pas deja un acteur portant le meme nom
	 * Leve une erreur si le parametre est null ou si le nom d'ac est celui d'un acteur deja
	 * dans la filiere
	 * @param ac, l'acteur a ajouter
	 */
	public void ajouterActeur(IActeur ac) {
		this.journalFiliere.ajouter(Journal.texteColore(ac==null ? Color.white : ac.getColor(), Color.black,"ajouterActeur("+(ac==null?"null":ac.getNom())+")"));
		if (ac==null) {
			erreur("Appel a ajouterActeur de Filiere avec un parametre null");
		} else if (this.getActeur(ac.getNom())==null) {
			this.acteurs.add(ac);
			this.acteursSolvables.add(ac);
			if (ac instanceof ClientFinal) {
				this.clientsFinaux.add((ClientFinal)ac);
			}
		} else {
			erreur("Appel a ajouterActeur de Filiere avec pour parametre le nom d'un acteur deja present dans la filiere");
		}
		this.getBanque().creerCompte(ac);
		this.journalFiliere.ajouter(Journal.texteColore(ac, "- creation du compte bancaire de "+ac.getNom()));
		//		this.initIndicateurs(ac);
		this.indicateursParActeur.put(ac, new ArrayList<Variable>());
		List<Variable> indicateursAAjouter = ac.getIndicateurs();
		for (Variable v : indicateursAAjouter) {
			this.ajouterIndicateur(v);
			this.journalFiliere.ajouter(Journal.texteColore(ac,"- ajout de l'indicateur "+v.getNom()));
		}
		//		this.initParametres(ac);
		this.parametresParActeur.put(ac, new ArrayList<Variable>());
		List<Variable> parametresAAjouter = ac.getParametres();
		//System.out.println("params = "+ac.getParametres());
		for (Variable v : parametresAAjouter) {
			this.ajouterParametre(v);
			this.journalFiliere.ajouter(Journal.texteColore(ac,"- ajout du parametre "+v.getNom()));
		}
		//		this.initJournaux(ac);
		this.journauxParActeur.put(ac, new ArrayList<Journal>());
		List<Journal> journauxAAjouter = ac.getJournaux();
		for (Journal j : journauxAAjouter) {
			this.ajouterJournal(j);
			this.journalFiliere.ajouter(Journal.texteColore(ac,"- ajout du journal "+j.getNom()));
		}
	}

	/**
	 * @return Retourne une copie de la liste des acteurs de la filiere
	 */
	public List<IActeur> getActeurs() {
		return new ArrayList<IActeur>(this.acteurs);
	}

	/**
	 * @return Retourne une copie de la liste des acteurs de la filiere n'ayant pas fait faillite
	 */
	public List<IActeur> getActeursSolvables() {
		return new ArrayList<IActeur>(this.acteursSolvables);
	}

	/**
	 * @param nom Le nom de l'acteur a retourner
	 * @return Si il existe dans la filiere un acteur de nom nom, retourne cet acteur.
	 * Sinon, returne null. 
	 */
	public IActeur getActeur(String nom) {
		int i=0; 
		while (i<this.acteurs.size() && !this.acteurs.get(i).getNom().equals(nom)) {
			i++;
		}
		if (i<this.acteurs.size()) {
			return this.acteurs.get(i);
		} else {
			return null;
		}
	}

	/** 
	 * @return Retourne une copie de la liste des indicateurs de l'acteur
	 */
	public List<Variable> getIndicateurs(IActeur acteur) {
		if (acteur==null) {
			erreur("Appel de getIndicateurs de Filiere avec null pour parametre");
		} else if (!this.indicateursParActeur.keySet().contains(acteur)){
			erreur("Appel de getIndicateurs de Filiere avec pour parametre un acteur non present ");
		} 
		return new ArrayList<Variable>(this.indicateursParActeur.get(acteur));
	}

	/** 
	 * @return Retourne une copie de la liste des parametres de l'acteur
	 */
	public List<Variable> getParametres(IActeur acteur) {
		if (acteur==null) {
			erreur("Appel de getParametres de Filiere avec null pour parametre");
		} else if (!this.parametresParActeur.keySet().contains(acteur)){
			erreur("Appel de getParametres de Filiere avec pour parametre un acteur non present ");
		} 
		return new ArrayList<Variable>(this.parametresParActeur.get(acteur));
	}

	/**
	 * @param nom le nom de l'indicateur a retourner
	 * @return Si il existe dans le Monde un indicateur de nom nom
	 * retourne cet indicateur. Sinon, affiche un message d'alerte 
	 * et retourne null.
	 */
	public Variable getIndicateur(String nomIndicateur) {
		if (nomIndicateur==null) {
			erreur("Appel de getIndicateur de Filiere avec null pour parametre");
		}
		Variable res = this.indicateurs.get(nomIndicateur);
		if (res==null) {
			System.out.println("  Aie... recherche d'un indicateur en utilisant un nom incorrect : \""+nomIndicateur+"\" n'est pas dans la liste :"+indicateurs.keySet());
			System.out.println("  la variable que vous recherchez est peut etre un parametre plutot qu'un indicateur ?");
			throw new IllegalArgumentException("recherche de >>"+nomIndicateur+"<<");
		}
		return res;
	}

	/**
	 * @param nom le nom de l'indicateur a retourner
	 * @return Si il existe dans le Monde un indicateur de nom nom
	 * retourne cet indicateur. Sinon, affiche un message d'alerte 
	 * et retourne null.
	 */
	public Variable getParametre(String nomParametre) {
		if (nomParametre==null) {
			erreur("Appel de getParametre de Filiere avec null pour parametre");
		} 
		Variable res = this.parametres.get(nomParametre);
		if (res==null) {
			System.out.println("  Aie... recherche d'un parametre en utilisant un nom incorrect : \""+nomParametre+"\" n'est pas dans la liste :"+parametres.keySet());
			System.out.println("  la variable que vous recherchez est peut etre un indicateur plutot qu'un parametre ?");
		}
		return res;
	}

	/**
	 * @return Retourne la liste des journaux de l'acteur specifie
	 */
	public List<Journal> getJournaux(IActeur acteur) {
		if (acteur==null) {
			erreur("Appel de getJournaux de Filiere avec null pour parametre");
		} else if (!this.journauxParActeur.keySet().contains(acteur)){
			erreur("Appel de getJournaux de Filiere avec pour parametre un acteur non present ");
		} 
		return new ArrayList<Journal>(this.journauxParActeur.get(acteur));
	}

	public void erreur(String s) {
		this.journalFiliere.ajouter(Journal.texteColore(Color.RED, Color.WHITE,s));
		throw new Error(s);
	}

	/**
	 * Methode appelee lorsque l'utilisateur clique sur le bouton NEXT de l'interface graphique.
	 * Cette methode incremente le numero d'etape puis appelle la methode next() de chaque acteur du monde.
	 */
	public void next() {
		this.journalFiliere.ajouter("Next() : Passage a l'etape suivante====================== ");
		for (IActeur a : this.acteurs) {
			long startTime = System.currentTimeMillis();

			if (!this.laBanque.aFaitFaillite(a)) {

				this.journalFiliere.ajouter(Journal.texteColore(a, "- "+a.getNom()+".next()"));
				this.journalFiliere.notifyObservers();
				a.next();
				for (Journal j : journauxParActeur.get(a)) {
					j.notifyObservers();
				}

			}

			long endTime = System.currentTimeMillis();
			tempsEquipes.put(a.getNom(), tempsEquipes.get(a.getNom()) + endTime - startTime);
		}
		// Mise a jour de l'impact de la presence en TG sur les marques
		HashMap<String, Double> quantiteEnTG=new HashMap<String,Double>(); // associe a chaque marque la quantite de produit en tete de gondole
		for (String marque : getMarquesChocolat()) {
			quantiteEnTG.put(marque, 0.0);
		}
		ClientFinal cf = this.clientsFinaux.get(0);
		double quantiteTotaleEnTG=0.0;
		for (ChocolatDeMarque choco : getChocolatsProduits()) {
			for (IDistributeurChocolatDeMarque distri : getDistributeurs()) {
				double qtg = cf.getQuantiteEnVenteTG(distri, choco, cryptos.get(cf));
				quantiteEnTG.put(choco.getMarque(), quantiteEnTG.get(choco.getMarque())+qtg);
				quantiteTotaleEnTG+=qtg;
			}
		}
		for (String marque : getMarquesChocolat()) {
			if (quantiteEnTG.get(marque)>=(SEUIL_EN_TETE_DE_GONDOLE_POUR_IMPACT*quantiteTotaleEnTG)) {
				presenceEnTG.add(marque);
				nbPresencesEnTg.put(marque, nbPresencesEnTg.get(marque)+1);
				if (presenceEnTG.size()>100) {
					String premier = presenceEnTG.get(0);
					nbPresencesEnTg.put(premier, nbPresencesEnTg.get(premier)-1);
					presenceEnTG.remove(0);
				}
			}
		}
		updateVariablesEchanges();
		updatePartsMarche();
		purgerEchanges(); // Elimine les echanges trop anciens.
		echangesToCSV();  // Ecrit les echanges dans echanges.csv si le parametre d'affichage est different de 0
		this.incEtape();
	}

	/**
	 * Elimine de l'historique des echanges tous les echanges trop anciens
	 */
	private void purgerEchanges() {
		int limite = Filiere.LA_FILIERE.getEtape() - Filiere.DUREE_HISTORIQUE_ECHANGES;
		while (this.echanges.size()>0 && this.echanges.get(0).getStep()<limite) {
			this.echanges.remove(0);
		}
	}

	private void updateVariablesEchanges() {
		//System.out.println("etape "+Filiere.LA_FILIERE.getEtape()+" dernier echange a etape "+(this.echanges.size()>0?this.echanges.get(this.echanges.size()-1).getStep():"<<aucun>>"));
		int last = this.echanges.size()-1;
		int now = Filiere.LA_FILIERE.getEtape();
		int i=last;
		IActeur romu = this.getActeur("EQX");
		int pwd = this.cryptos.get(romu);
		for (IActeur a : ventesFeves.keySet()) {
			for (Feve f : ventesFeves.get(a).keySet()) {
				ventesFeves.get(a).get(f).setValeur(romu, 0, pwd);
			}
		}
		for (IActeur a : achatsFeves.keySet()) {
			for (Feve f : achatsFeves.get(a).keySet()) {
				achatsFeves.get(a).get(f).setValeur(romu, 0, pwd);
			}
		}
		for (IActeur a : ventesChocolats.keySet()) {
			for (Chocolat f : ventesChocolats.get(a).keySet()) {
				ventesChocolats.get(a).get(f).setValeur(romu, 0, pwd);
			}
		}
		for (IActeur a : achatsChocolats.keySet()) {
			for (Chocolat f : achatsChocolats.get(a).keySet()) {
				achatsChocolats.get(a).get(f).setValeur(romu, 0, pwd);
			}
		}
		for (IActeur a : ventesChocolatsMarques.keySet()) {
			for (Chocolat f : ventesChocolatsMarques.get(a).keySet()) {
				ventesChocolatsMarques.get(a).get(f).setValeur(romu, 0, pwd);
			}
		}		
		while (i>=0 && this.echanges.get(i).getStep()==now) {
			Echange echange = echanges.get(i);
			IProduit produit = echange.getProduit();
			IActeur acteur = echange.getActeur();
			double volume = echange.getVolume();
			if (produit instanceof Feve){
				if (echange.getVolume()<0) {
					ventesFeves.get(acteur).get(produit).setValeur(romu,ventesFeves.get(acteur).get(produit).getValeur() - volume, pwd);
				} else {
					//System.out.println("update achatsFeves "+acteur+" "+produit+" +"+volume);
					achatsFeves.get(acteur).get(produit).setValeur(romu,achatsFeves.get(acteur).get(produit).getValeur() + volume, pwd);
				}
			} else if (produit instanceof ChocolatDeMarque && acteur instanceof IDistributeurChocolatDeMarque){
				Chocolat choco = ((ChocolatDeMarque)produit).getChocolat(); 
				if (echange.getVolume()<0) {
					//System.out.println(now+" update ventesChocolatsMarques "+acteur+" "+choco+" +"+volume);
					ventesChocolatsMarques.get(acteur).get(choco).setValeur(romu,ventesChocolatsMarques.get(acteur).get(choco).getValeur() - volume, pwd);
				} else {
					achatsChocolats.get(acteur).get(choco).setValeur(romu,achatsChocolats.get(acteur).get(choco).getValeur() + volume, pwd);
//					System.out.println("Aie... dans filiere.updateEchange on s'apercoit qu'un distributeur est mentionne avec un volume positif");
//					System.out.println("Aie... "+acteur+" "+produit+" "+volume+" "+echange.getTypeEchange());
				}
			} else { // c'est un chocolat
				Chocolat choco = (produit instanceof Chocolat) ? (Chocolat)produit : ((ChocolatDeMarque)produit).getChocolat(); 
				if (echange.getVolume()<0) {
					ventesChocolats.get(acteur).get(choco).setValeur(romu,ventesChocolats.get(acteur).get(choco).getValeur() - volume, pwd);
				} else {
					achatsChocolats.get(acteur).get(produit).setValeur(romu,achatsChocolats.get(acteur).get(produit).getValeur() + volume, pwd);
				}
			}
			i--;
		}
	}

	private void updatePartsMarche() {
		// TODO 
		int now = Filiere.LA_FILIERE.getEtape();
		int start = Math.max(0, now-DUREE_EN_ETAPES_CALCUL_PARTS_DE_MARCHE);
		for (Feve f : Feve.values()) {
			// VENTES DE FEVES =================================================
			HashMap<IActeur, Double> totaux = new HashMap<IActeur, Double>();
			double totalPourCetteFeve = 0.0;
			for (IActeur acteur : ventesFeves.keySet()) {
				double totalActeur = 0.0;
				for (int etape=start; etape<=now; etape++) {
					totalActeur+=ventesFeves.get(acteur).get(f).getValeur(etape);
				}
				totaux.put(acteur, totalActeur);
				totalPourCetteFeve+=totalActeur;
			}
			for (IActeur acteur : ventesFeves.keySet()) {
				courbeVentesFeves.get(f).get(acteur).ajouter(now, totalPourCetteFeve==0?0:(100*totaux.get(acteur))/totalPourCetteFeve);
			}
			// ACHATS DE FEVES =================================================
			totaux = new HashMap<IActeur, Double>();
			totalPourCetteFeve = 0.0;
			for (IActeur acteur : achatsFeves.keySet()) {
				double totalActeur = 0.0;
				for (int etape=start; etape<=now; etape++) {
					totalActeur+=achatsFeves.get(acteur).get(f).getValeur(etape);
				}
				totaux.put(acteur, totalActeur);
				totalPourCetteFeve+=totalActeur;
			}
			for (IActeur acteur : achatsFeves.keySet()) {
				courbeAchatsFeves.get(f).get(acteur).ajouter(now, totalPourCetteFeve==0?0:(100*totaux.get(acteur))/totalPourCetteFeve);
			}
		}
		for (Chocolat c : Chocolat.values()) {
			// VENTES DE CHOCOLATS =================================================
			HashMap<IActeur, Double> totaux = new HashMap<IActeur, Double>();
			double totalPourCeChocolat = 0.0;
			for (IActeur acteur : ventesChocolats.keySet()) {
				double totalActeur = 0.0;
				for (int etape=start; etape<=now; etape++) {
					totalActeur+=ventesChocolats.get(acteur).get(c).getValeur(etape);
				}
				totaux.put(acteur, totalActeur);
				totalPourCeChocolat+=totalActeur;
			}
			for (IActeur acteur : ventesChocolats.keySet()) {
				courbeVentesChocolats.get(c).get(acteur).ajouter(now,totalPourCeChocolat==0?0: (100*totaux.get(acteur))/totalPourCeChocolat);
				//System.out.println(now+" update ventes C : "+c+" acteur "+acteur+" "+Math.round(((100*totaux.get(acteur))/totalPourCeChocolat))+" acteur="+totaux.get(acteur)+" tous="+totalPourCeChocolat);
				//System.out.println("courbe="+courbeVentesChocolats.get(c).get(acteur).toString());
			}
			// ACHATS DE CHOCOLATS =================================================
			totaux = new HashMap<IActeur, Double>();
			totalPourCeChocolat = 0.0;
			for (IActeur acteur : achatsChocolats.keySet()) {
				double totalActeur = 0.0;
				for (int etape=start; etape<=now; etape++) {
					totalActeur+=achatsChocolats.get(acteur).get(c).getValeur(etape);
				}
				totaux.put(acteur, totalActeur);
				totalPourCeChocolat+=totalActeur;
			}
			for (IActeur acteur : achatsChocolats.keySet()) {
				courbeAchatsChocolats.get(c).get(acteur).ajouter(now, totalPourCeChocolat==0?0:Math.round((100*totaux.get(acteur))/totalPourCeChocolat));
				//System.out.println(now+" update achats C : "+c+" acteur "+acteur+" "+Math.round(((100*totaux.get(acteur))/totalPourCeChocolat))+" acteur="+totaux.get(acteur)+" tous="+totalPourCeChocolat);
				//System.out.println("courbe="+courbeAchatsChocolats.get(c).get(acteur).toString());
			}
			// VENTES DE CHOCOLATS DE MARQUE =================================================
			totaux = new HashMap<IActeur, Double>();
			totalPourCeChocolat = 0.0;
			for (IActeur acteur : ventesChocolatsMarques.keySet()) {
				double totalActeur = 0.0;
				for (int etape=start; etape<=now; etape++) {
					totalActeur+=ventesChocolatsMarques.get(acteur).get(c).getValeur(etape);
				}
				totaux.put(acteur, totalActeur);
				totalPourCeChocolat+=totalActeur;
			}
			for (IActeur acteur : ventesChocolatsMarques.keySet()) {
				//System.out.println(now+" update CMarque : "+c+" acteur "+acteur+" "+Math.round(((100*totaux.get(acteur))/totalPourCeChocolat))+" acteur="+totaux.get(acteur)+" tous="+totalPourCeChocolat);
				courbeVentesChocolatsMarque.get(c).get(acteur).ajouter(now, totalPourCeChocolat==0?0:Math.round((100*totaux.get(acteur))/totalPourCeChocolat));
				//System.out.println("courbe="+courbeVentesChocolatsMarque.get(c).get(acteur).toString());
			}
		}
		// if (now==5) {
		// 	graphiqueVentesFeves.get(Feve.F_MQ).setVisible(true);
		// 	graphiqueVentesChocolats.get(Chocolat.C_MQ).setVisible(true);
		// }
// 		System.out.println("etape "+Filiere.LA_FILIERE.getEtape()+" dernier echange a etape "+(this.echanges.size()>0?this.echanges.get(this.echanges.size()-1).getStep():"<<aucun>>"));
// 		int last = this.echanges.size()-1;
// 		int now = Filiere.LA_FILIERE.getEtape();
// 		int i=last;
// 		IActeur romu = this.getActeur("EQX");
// 		int pwd = this.cryptos.get(romu);
// 		for (IActeur a : ventesFeves.keySet()) {
// 			for (Feve f : ventesFeves.get(a).keySet()) {
// 				ventesFeves.get(a).get(f).setValeur(romu, 0, pwd);
// 			}
// 		}
// 		for (IActeur a : achatsFeves.keySet()) {
// 			for (Feve f : achatsFeves.get(a).keySet()) {
// 				achatsFeves.get(a).get(f).setValeur(romu, 0, pwd);
// 			}
// 		}
// 		for (IActeur a : ventesChocolats.keySet()) {
// 			for (Chocolat f : ventesChocolats.get(a).keySet()) {
// 				ventesChocolats.get(a).get(f).setValeur(romu, 0, pwd);
// 			}
// 		}
// 		for (IActeur a : achatsChocolats.keySet()) {
// 			for (Chocolat f : achatsChocolats.get(a).keySet()) {
// 				achatsChocolats.get(a).get(f).setValeur(romu, 0, pwd);
// 			}
// 		}
// 		for (IActeur a : ventesChocolatsMarques.keySet()) {
// 			for (Chocolat f : ventesChocolatsMarques.get(a).keySet()) {
// 				ventesChocolatsMarques.get(a).get(f).setValeur(romu, 0, pwd);
// 			}
// 		}		
// 		while (i>=0 && this.echanges.get(i).getStep()==now) {
// 			Echange echange = echanges.get(i);
// 			IProduit produit = echange.getProduit();
// 			IActeur acteur = echange.getActeur();
// 			double volume = echange.getVolume();
// 			if (produit instanceof Feve){
// 				if (echange.getVolume()<0) {
// 					ventesFeves.get(acteur).get(produit).setValeur(romu,ventesFeves.get(acteur).get(produit).getValeur() - volume, pwd);
// 				} else {
// 					System.out.println("update achatsFeves "+acteur+" "+produit+" +"+volume);
// 					achatsFeves.get(acteur).get(produit).setValeur(romu,achatsFeves.get(acteur).get(produit).getValeur() + volume, pwd);
// 				}
// 			} else if (produit instanceof ChocolatDeMarque && acteur instanceof IDistributeurChocolatDeMarque){
// 				Chocolat choco = ((ChocolatDeMarque)produit).getChocolat(); 
// 				if (echange.getVolume()<0) {
// 					ventesChocolatsMarques.get(acteur).get(choco).setValeur(romu,ventesChocolatsMarques.get(acteur).get(choco).getValeur() - volume, pwd);
// 				} else {
// 					achatsChocolats.get(acteur).get(choco).setValeur(romu,achatsChocolats.get(acteur).get(choco).getValeur() + volume, pwd);
// //					System.out.println("Aie... dans filiere.updateEchange on s'apercoit qu'un distributeur est mentionne avec un volume positif");
// //					System.out.println("Aie... "+acteur+" "+produit+" "+volume+" "+echange.getTypeEchange());
// 				}
// 			} else { // c'est un chocolat
// 				Chocolat choco = (produit instanceof Chocolat) ? (Chocolat)produit : ((ChocolatDeMarque)produit).getChocolat(); 
// 				if (echange.getVolume()<0) {
// 					ventesChocolats.get(acteur).get(choco).setValeur(romu,ventesChocolats.get(acteur).get(choco).getValeur() - volume, pwd);
// 				} else {
// 					achatsChocolats.get(acteur).get(produit).setValeur(romu,achatsChocolats.get(acteur).get(produit).getValeur() + volume, pwd);
// 				}
// 			}
// 			i--;
// 		}
	}

	public void addObserver(PropertyChangeListener obs) {
		pcs.addPropertyChangeListener(obs);
	}

	public void notificationFaillite(IActeur acteur) {
		this.acteursSolvables.remove(acteur);
		this.journalFiliere.ajouter(Journal.texteColore(acteur,"Faillite de "+acteur.getNom()));
		this.journalFiliere.notifyObservers();
		if (FenetrePrincipale.LA_FENETRE_PRINCIPALE!=null) {
			FenetrePrincipale.LA_FENETRE_PRINCIPALE.notificationFaillite(acteur);
		}
	}

	public double qualitePercueMarque(String marque) {
		double bonusTG=0.0;
		if (!this.getMarquesChocolat().contains(marque)) {
			throw new IllegalArgumentException("Appel de qualitePercueMarque("+marque+") alors que les marques deposees sont "+this.getMarquesChocolat());
		}
		double nb = nbPresencesEnTg.get(marque);
		if (nb>0) {
			bonusTG = nb / presenceEnTG.size();
		}
		return this.qualiteMoyenneMarque.get(marque)+bonusTG;
	}

	public List<IDistributeurChocolatDeMarque> getDistributeurs() {
		List<IDistributeurChocolatDeMarque> res = new ArrayList<IDistributeurChocolatDeMarque>();
		for (IActeur a : this.acteurs) {
			if ((a instanceof IDistributeurChocolatDeMarque) && (!this.laBanque.aFaitFaillite(a))) {
				res.add((IDistributeurChocolatDeMarque)a);
			}
		}
		return res;
	}

	public List<IFabricantChocolatDeMarque> getFabricantsChocolatDeMarque(ChocolatDeMarque c) {
		return new ArrayList<IFabricantChocolatDeMarque>(this.fabricantsChocolatDeMarque.get(c));
	}
	/**
	 * @return Retourne la liste de tous les chocolats de marque produits par les differents acteurs de la filiere
	 * Remarque : vous ne pouvez pas appeler cette methode dans votre constructeur
	 * (vous obtiendriez une liste vide).
	 */
	public List<ChocolatDeMarque> getChocolatsProduits() {
		return new ArrayList<ChocolatDeMarque>(this.chocolatsProduits);
	}
	public double prixMoyen(ChocolatDeMarque choco, int etape) {
		return clientsFinaux.get(0).prixMoyen(choco, etape);
	}
	public double getVentes(ChocolatDeMarque choco, int etape) {
		return clientsFinaux.get(0).getVentes(etape, choco);
	}
	/*********************************************************************************/
	/**       METHODES ACCESSIBLES UNIQUEMENT AUX ASSERMENTES (superviseurs)        **/
	/*********************************************************************************/
	public void ajouterEchange(IActeur acteurAssermente, int cryptogramme,IActeur acteur,IProduit produit, double volume, String typeEchange) {
		if (acteur==null) {
			erreur(" Appel de ajouterEchange de Filiere avec null pour acteur");
		}  else if (acteurAssermente==null) {
			erreur(" Appel de ajouterEchange de Filiere avec null pour acteur assermente");
		}  else if (produit==null) {
			erreur(" Appel de ajouterEchange de Filiere avec null pour produit");
		}  else if (this.cryptos.get(acteurAssermente)!=cryptogramme) {
			erreur(" Appel de ajouterEchange de Filiere avec un cryptogramme qui n'est pas celui de l'acteur acredite");
		} else if (!(acteurAssermente instanceof IAssermente)) {
			System.err.println(" Appel de ajouterEchange de Filiere par un acteur non assermente");
			Filiere.LA_FILIERE.getBanque().faireFaillite(acteur, this, cryptogramme);
		}  else  {
			this.echanges.add(new Echange(this.etape, acteur, produit, volume, typeEchange));
		} 
	}
	private void echangesToCSV() {
		//	Variable aff = Filiere.LA_FILIERE.getIndicateur("BourseCacao Aff.Graph");
		if (getIndicateur("BourseCacao Aff.Graph.").getValeur()!=0.0) {


			try {
				PrintWriter aEcrire= new PrintWriter(new BufferedWriter(new FileWriter("docs"+File.separator+"Echanges.csv")));
				aEcrire.println("ETAPE;ACTEUR;PRODUIT;VOLUME");
				for (Echange ec : this.echanges) {						
					aEcrire.println( ec.toCSV() );
					//							System.out.println(s);
				}
				aEcrire.close();
			}
			catch (IOException e) {
				throw new Error("Une operation sur les fichiers a leve l'exception "+e) ;
			}
		}	
	}

	/***********************************************************************/
	/**                METHODES INTERNES (non accessibles)                **/
	/***********************************************************************/

	/**
	 * Ajoute l'indicateur i a la filiere
	 * @param i l'idicateur a ajouter
	 */
	private void ajouterIndicateur(Variable i) {
		if (i==null) {
			erreur("Appel a ajouterIndicateur de Filiere avec null pour parametre");
		} else if (this.indicateurs.get(i.getNom())!=null) {
			erreur("Appel a ajouterIndicateur(v) de Filiere alors qu'il existe deja dans la filiere un indicateur portant le meme nom que v (\""+i.getNom()+"\")");
		} else {
			this.indicateurs.put(i.getNom(), i);
			List<Variable> indicateursActuels = this.indicateursParActeur.get(i.getCreateur());
			if (indicateursActuels==null) {
				indicateursActuels=new ArrayList<Variable>();
			}
			indicateursActuels.add(i);
			this.indicateursParActeur.put(i.getCreateur(), indicateursActuels);
		}
	}

	/**
	 * Ajoute le parametre i a la filiere
	 * @param i le parametre a ajouter
	 */
	private void ajouterParametre(Variable i) {
		if (i==null) {
			erreur("Appel a ajouterParametre de Filiere avec null pour parametre");
		} else if (this.parametres.get(i.getNom())!=null) {
			erreur("Appel a ajouterParametre(v) de Filiere alors qu'il existe deja dans la filiere un parametre portant le meme nom que v (\""+i.getNom()+"\")");
		} else {
			this.parametres.put(i.getNom(), i);
			List<Variable> parametresActuels = this.parametresParActeur.get(i.getCreateur());
			if (parametresActuels==null) {
				parametresActuels=new ArrayList<Variable>();
			}
			parametresActuels.add(i);
			this.parametresParActeur.put(i.getCreateur(), parametresActuels);
		}
	}


	/**
	 * Ajoute le journal j au monde
	 * @param j le journal a ajouter
	 */
	private void ajouterJournal(Journal j) {
		if (j==null) {
			erreur("Appel a ajouterJournal de Filiere avec null pour parametre");
		} else if (this.journaux.get(j.getNom())!=null) {
			erreur("Appel a ajouterJournal(j) de Filiere alors qu'il existe deja dans la filiere un journal portant le meme nom que j (\""+j.getNom()+"\")");
		} else {
			this.journaux.put(j.getNom(), j);
			List<Journal> journauxActuels = this.journauxParActeur.get(j.getCreateur());
			if (journauxActuels==null) {
				journauxActuels=new ArrayList<Journal>();
			}
			journauxActuels.add(j);
			this.journauxParActeur.put(j.getCreateur(), journauxActuels);
		}
	}


	/**
	 * Methode interne (non accessible) permettant de passer a l'etape suivante
	 * en notifiant les observateurs
	 */
	private void incEtape() {
		int old = this.etape;
		this.etape++;
		pcs.firePropertyChange("Etape",old,this.etape);
	}

	public void setCryptos(HashMap<IActeur, Integer> cryptos) {
		this.cryptos = cryptos;
	}
}
class Echange {
	private int step;
	private IActeur acteur;
	private IProduit produit;
	private double volume;
	private String typeEchange;
	public Echange(int step, IActeur acteur, IProduit produit, double volume, String typeEchange) {
		this.step = step;
		this.acteur = acteur;
		this.produit = produit;
		this.volume = volume;	
		this.typeEchange = typeEchange;	
	}
	public int getStep() {
		return step;
	}
	public IActeur getActeur() {
		return acteur;
	}
	public IProduit getProduit() {
		return produit;
	}
	public double getVolume() {
		return volume;
	}
	public String getTypeEchange() {
		return typeEchange;
	}
	public String toCSV() {
		return step + ";" + acteur.getNom() + ";" + produit + ";" + volume+";" + typeEchange;
	}
}