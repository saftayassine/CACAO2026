package abstraction.eqXRomu.contratsCadres;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import abstraction.eqXRomu.bourseCacao.IAcheteurBourse;
import abstraction.eqXRomu.bourseCacao.IVendeurBourse;
import abstraction.eqXRomu.filiere.*;
import abstraction.eqXRomu.general.*;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.IProduit;
import java.util.concurrent.RejectedExecutionException;

public class SuperviseurVentesContratCadre implements IActeur, IAssermente {
	public static final int MAX_PRIX_NEGO = 14 ; // Les negociations sur le prix s'arretent apres au plus MAX_PRIX_NEGO propositions de prix
	public static final double QUANTITE_MIN_ECHEANCIER = 100.0; // Il ne peut pas etre propose de contrat avec un echeancier de moins de QUANTITE_MIN_ECHEANCIER
	public static int NB_SUPRVISEURS_CONTRAT_CADRE = 0;
	private int numero;
	private Journal journal, journalNegoF, journalNegoCM, journalQVQCM, journalQVQF;
	private List<ContratCadre> contratsEnCours;
	private List<ContratCadre> contratsTermines;
	private HashMap<IActeur, Integer> cryptos;
	private Variable aff; // variable de la bourse precisant si on affiche ou non les donnees.
	private HashMap<Feve, HashMap<Integer, Double>> livraisonsFeves;

	private HashMap<IActeur, HashMap<Integer, Double>> chocolatALivrerStep, chocolatALivrerTotale; // pour chaque vendeur et chaque step la quantite qu'il doit livre au prochain step.
	
	public static final int MAX_MEME_VENDEUR_PAR_STEP = 15; 
	// Au cours d'un meme step un acheteur peut negocier au plus MAX_MEME_VENDEUR_PAR_STEP
	// fois avec le meme vendeur. Si l'acheteur demande a negocier avec le vendeur v alors qu'il a 
	// deja negocie MAX_MEME_VENDEUR_PAR_STEP fois avec v durant le step courant alors l'acheteur
	// ne pourra plus negocier durant le step.

	public SuperviseurVentesContratCadre() {
		NB_SUPRVISEURS_CONTRAT_CADRE++;
		this.numero = NB_SUPRVISEURS_CONTRAT_CADRE;
		this.journal       = new Journal("J. "+this.getNom() +" Gestion ", this); // la gestion des contrats en cours
		this.journalNegoCM = new Journal("J. "+this.getNom() +" Negos.CM", this); // les negociations de chocolat de marque
		this.journalNegoF  = new Journal("J. "+this.getNom() +" Negos.F ", this); // les negociations de feves
		this.journalQVQCM  = new Journal("J. "+this.getNom() +" QuiAV.CM", this); // Qui vend/achete le chocolat de marque
		this.journalQVQF   = new Journal("J. "+this.getNom() +" QuiAV.F ", this); // Qui vend/achete les feves
		this.contratsEnCours= new ArrayList<ContratCadre>();
		this.contratsTermines= new ArrayList<ContratCadre>();
		this.livraisonsFeves = new HashMap<Feve, HashMap<Integer, Double>> ();
		for (Feve f : Feve.values()) {
			this.livraisonsFeves.put(f,  new HashMap<Integer, Double>());
		}
		this.chocolatALivrerStep = new HashMap<IActeur, HashMap<Integer,Double>>();
		this.chocolatALivrerTotale = new HashMap<IActeur, HashMap<Integer,Double>>();
	}
	public String getNom() {
		return "Sup."+(this.numero>1?this.numero+"":"")+"CCadre";
	}

	public void initialiser() {
		aff = Filiere.LA_FILIERE.getIndicateur("BourseCacao Aff.Graph.");
		for (IActeur ac : Filiere.LA_FILIERE.getActeurs()) {
			if (ac instanceof IVendeurContratCadre) {
				this.chocolatALivrerStep.put(ac,  new HashMap<Integer, Double>());
				this.chocolatALivrerTotale.put(ac,  new HashMap<Integer, Double>());
			}
		}
	}

	public List<IVendeurContratCadre> getVendeurs(IProduit produit) {
		List<IVendeurContratCadre> vendeurs = new LinkedList<IVendeurContratCadre>();
		List<IActeur> acteurs = Filiere.LA_FILIERE.getActeursSolvables();
		for (IActeur acteur : acteurs) {
			if (acteur instanceof IVendeurContratCadre && ((IVendeurContratCadre)acteur).vend(produit)) {
				if (produit instanceof ChocolatDeMarque && !Filiere.LA_FILIERE.getMarquesDistributeur().contains(((ChocolatDeMarque)produit).getMarque()) && !Filiere.LA_FILIERE.getProprietaireMarque(((ChocolatDeMarque)produit).getMarque()).equals(acteur)) {
					System.err.println(acteur.getNom()+" dit vendre du "+produit+" alors qu'il ne s'agit pas d'une marque distributeur et qu'elle est la proprietee de "+Filiere.LA_FILIERE.getProprietaireMarque(((ChocolatDeMarque)produit).getMarque()));
					System.err.println("mise en faillite de "+acteur.getNom());
					Filiere.LA_FILIERE.getBanque().faireFaillite(acteur, this, cryptos.get(this));
				}
				vendeurs.add(((IVendeurContratCadre)acteur));
			}
		}
		return vendeurs;
	}
	public List<IAcheteurContratCadre> getAcheteurs(IProduit produit) {
		List<IAcheteurContratCadre> acheteurs = new LinkedList<IAcheteurContratCadre>();
		List<IActeur> acteurs = Filiere.LA_FILIERE.getActeursSolvables();
		for (IActeur acteur : acteurs) {
			//System.out.println("SupCC "+acteur.getNom()+""+(acteur instanceof IAcheteurContratCadre)+" "+(acteur instanceof IAcheteurContratCadre? ((IAcheteurContratCadre)acteur).achete(produit):"nan"));
			if (acteur instanceof IAcheteurContratCadre && ((IAcheteurContratCadre)acteur).achete(produit)) {
				acheteurs.add(((IAcheteurContratCadre)acteur));
			}
		}
		return acheteurs;
	}

	/**
	 * 
	 * @param acheteur l'acheteur appelant la methode
	 * @param vendeur le vendeur avec qui l'acheteur souhaite etablir un contrat cadre
	 * @param produit le produit vendu via ce contrat cadre
	 * @param echeancier un Echeancier precisant les quantites a livre a chaque step
	 * @param cryptogramme le cryptogramme de l'acheteur afin de verifier l'identite de l'acteur appelant la methode
	 * @param tg true si l'acheteur est un distributeur et que le produit devra 
	 *           obligatoirement etre vendu en tete de gondole. False dans tous les autres
	 *           cas (l'acheteur n'est pas un distributeur et/ou le produit pourra ne 
	 *           pas etre commercialise en tete de gondole)
	 * @return L'exemplaire de contrat cadre conclu en cas d'accord (null si un accord n'a pas pu etre trouve)
	 */
	public ExemplaireContratCadre demandeAcheteur(IAcheteurContratCadre acheteur, IVendeurContratCadre vendeur, IProduit produit, Echeancier echeancier, int cryptogramme, boolean tg) {
        Journal journal = (produit instanceof ChocolatDeMarque)? journalNegoCM : journalNegoF;
		if (acheteur==null) {
			throw new IllegalArgumentException(" appel de demandeAcheteur(...) de SuperViseurVentesContratCadre avec null pour acheteur");
		}
		if (vendeur==null) {
			throw new IllegalArgumentException(" appel de demandeAcheteur(...) de SuperViseurVentesContratCadre avec null pour vendeur");
		}
		if (produit==null) {
			throw new IllegalArgumentException(" appel de demandeAcheteur(...) de SuperViseurVentesContratCadre avec null pour produit");
		}
		if (echeancier==null) {
			throw new IllegalArgumentException(" appel de demandeAcheteur(...) de SuperViseurVentesContratCadre avec null pour echeancier");
		}
		if (!echeancier.echeancierAcceptable()) {
			System.out.println(" appel de demandeAcheteur(...) par "+acheteur.getNom()+" en fournissant un echeancier ne respectant pas les conditions du document des distributeurs : "+echeancier);
			Filiere.LA_FILIERE.getBanque().faireFaillite(acheteur, this, cryptos.get(this));	
			//throw new  IllegalArgumentException(" appel de demandeAcheteur(...) par "+acheteur.getNom()+" en fournissant un echeancier ne respectant pas les conditions du document des distributeurs : "+echeancier);
		}
		if (acheteur==vendeur) {
			throw new IllegalArgumentException(" appel de demandeAcheteur(...) de SuperViseurVentesContratCadre avec vendeur==acheteur. On ne peut pas faire un contrat cadre avec soi meme");
		}
		if (!Filiere.LA_FILIERE.getBanque().verifier(acheteur, cryptogramme)) {
			throw new IllegalArgumentException(" appel de demandeAcheteur(...) de SuperViseurVentesContratCadre par l'acheteur "+acheteur.getNom()+" avec un cryptogramme qui n'est pas le sien");
		}
		if (!(acheteur instanceof IDistributeurChocolatDeMarque) && tg) {
			throw new IllegalArgumentException(" appel de demandeAcheteur(...) de SuperViseurVentesContratCadre par l'acheteur "+acheteur.getNom()+" avec tg==true alors que l'acheteur n'est pas un distributeur (seuls les distributeurs peuvent s'engager a vendre en tete de gondole)");
		}
		if (produit instanceof ChocolatDeMarque && !Filiere.LA_FILIERE.getMarquesDistributeur().contains(((ChocolatDeMarque)produit).getMarque()) && !Filiere.LA_FILIERE.getProprietaireMarque(((ChocolatDeMarque)produit).getMarque()).equals(vendeur)) {
			System.err.println(acheteur.getNom()+" souhaite acheter du "+produit+" a "+vendeur.getNom()+" alors qu'il ne s'agit pas d'une marque distributeur et qu'elle est la proprietee de "+Filiere.LA_FILIERE.getProprietaireMarque(((ChocolatDeMarque)produit).getMarque()));
			Filiere.LA_FILIERE.getBanque().faireFaillite(acheteur, this, cryptos.get(this));
		}
		if (echeancier.getQuantiteTotale()<QUANTITE_MIN_ECHEANCIER) {
			if (Filiere.LA_FILIERE.getActeursSolvables().contains(acheteur)) {
				System.err.println("supCC : mise en faillite de "+acheteur.getNom()+" qui met en vente une quentite inferieure aux accords (on ne peut pas creer de contrat de moins de "+QUANTITE_MIN_ECHEANCIER+")");
				Filiere.LA_FILIERE.getBanque().faireFaillite(acheteur, this, cryptos.get(this));
				journal.ajouter("!!! "+acheteur.getNom()+" appel de demandeAcheteur(...) de SuperViseurVentesContratCadre avec un echeancier d'un volume total de moins de "+QUANTITE_MIN_ECHEANCIER+" T");
			}
			return null;
		}
		if (echeancier.getStepDebut()<=Filiere.LA_FILIERE.getEtape()) {
			if (Filiere.LA_FILIERE.getActeursSolvables().contains(acheteur)) {
				System.err.println("supCC : mise en faillite de "+acheteur.getNom()+" qui fait un contrat avec un debut inferieur a l'etape courante");
				Filiere.LA_FILIERE.getBanque().faireFaillite(acheteur, this, cryptos.get(this));
				journal.ajouter("!!! "+acheteur.getNom()+" appel de demandeAcheteur(...) de SuperViseurVentesContratCadre avec un echeancier commencant a l'etape "+echeancier.getStepDebut()+" a l'etape "+Filiere.LA_FILIERE.getEtape());
			}
			return null;			
		}
		ContratCadre contrat = new ContratCadre(acheteur, vendeur, produit, echeancier, cryptogramme, tg);
		journal.ajouter(Journal.texteColore(acheteur, "==>"+acheteur.getNom())+" lance le contrat #"+contrat.getNumero()+" de "+contrat.getQuantiteTotale()+" T de "+contrat.getProduit()+" a "+Journal.texteColore(vendeur, vendeur.getNom()));
		return negociations(acheteur, vendeur, produit, echeancier, cryptogramme, tg, contrat,acheteur);
	}

	/**
	 * 
	 * @param acheteur l'acheteur avec qui l'acheteur souhaite etablir un contrat cadre
	 * @param vendeur le vendeur appelant la methode
	 * @param produit le produit vendu via ce contrat cadre
	 * @param echeancier un Echeancier precisant les quantites a livre a chaque step
	 * @param cryptogramme le cryptogramme du vendeur afin de verifier l'identite de l'acteur appelant la methode
	 * @param tg true si l'acheteur est un distributeur et que le produit devra 
	 *           obligatoirement etre vendu en tete de gondole. False dans tous les autres
	 *           cas (l'acheteur n'est pas un distributeur et/ou le produit pourra ne 
	 *           pas etre commercialise en tete de gondole)
	 * @return L'exemplaire de contrat cadre conclu en cas d'accord (null si un accord n'a pas pu etre trouve)
	 */
	public ExemplaireContratCadre demandeVendeur(IAcheteurContratCadre acheteur, IVendeurContratCadre vendeur, IProduit produit, Echeancier echeancier, int cryptogramme, boolean tg) {
       Journal journal = (produit instanceof ChocolatDeMarque)? journalNegoCM : journalNegoF;
		if (acheteur==null) {
			throw new IllegalArgumentException(" appel de demandeVendeur(...) de SuperViseurVentesContratCadre avec null pour acheteur");
		}
		if (vendeur==null) {
			throw new IllegalArgumentException(" appel de demandeVendeur(...) de SuperViseurVentesContratCadre avec null pour vendeur");
		}
		if (!Filiere.LA_FILIERE.getActeursSolvables().contains(vendeur)) {
			return null; // Le vendeur n'est pas un acteur solvable 
		}
		if (produit==null) {
			throw new IllegalArgumentException(" appel de demandeVendeur(...) de SuperViseurVentesContratCadre avec null pour produit");
		}
		if (echeancier==null) {
			throw new IllegalArgumentException(" appel de demandeVendeur(...) de SuperViseurVentesContratCadre avec null pour echeancier");
		}
		if (!echeancier.echeancierAcceptable()) {
			throw new  IllegalArgumentException(" appel de demandeVendeur(...) par "+vendeur.getNom()+" en fournissant un echeancier ne respectant pas les conditions du document des distributeurs : "+echeancier);
		}
		if (echeancier.getQuantiteTotale()<QUANTITE_MIN_ECHEANCIER) {
			throw new IllegalArgumentException(" appel de demandeVendeur(...) de SuperViseurVentesContratCadre avec un echeancier d'un volume total de moins de "+QUANTITE_MIN_ECHEANCIER+" T");
		}
		if (echeancier.getStepDebut()<=Filiere.LA_FILIERE.getEtape()) {
			if (Filiere.LA_FILIERE.getActeursSolvables().contains(vendeur)) {
				journal.ajouter("!!! "+acheteur.getNom()+" appel de demandeVendeur(...) de SuperViseurVentesContratCadre avec un echeancier commencant a l'etape "+echeancier.getStepDebut()+" a l'etape "+Filiere.LA_FILIERE.getEtape());
				Filiere.LA_FILIERE.getBanque().faireFaillite(vendeur, this, cryptos.get(this));
			}
			return null;			
		}		
		if (acheteur==vendeur) {
			throw new IllegalArgumentException(" appel de demandeVendeur(...) de SuperViseurVentesContratCadre avec vendeur==acheteur. On ne peut pas faire un contrat cadre avec soi meme");
		}
		if (!Filiere.LA_FILIERE.getBanque().verifier(vendeur, cryptogramme)) {
			throw new IllegalArgumentException(" appel de demandeVendeur(...) de SuperViseurVentesContratCadre par l'acheteur "+acheteur.getNom()+" avec un cryptogramme qui n'est pas le sien");
		}
		if (!(acheteur instanceof IDistributeurChocolatDeMarque) && tg) {
			throw new IllegalArgumentException(" appel de demandeVendeur(...) de SuperViseurVentesContratCadre par l'acheteur "+acheteur.getNom()+" avec tg==true alors que l'acheteur n'est pas un distributeur (seuls les distributeurs peuvent s'engager a vendre en tete de gondole)");
		}
		if (produit instanceof ChocolatDeMarque && !Filiere.LA_FILIERE.getMarquesDistributeur().contains(((ChocolatDeMarque)produit).getMarque()) && !Filiere.LA_FILIERE.getProprietaireMarque(((ChocolatDeMarque)produit).getMarque()).equals(vendeur)) {
			System.err.println(vendeur.getNom()+" souhaite vendre du "+produit+" alors qu'il ne s'agit pas d'une marque distributeur et qu'elle est la proprietee de "+Filiere.LA_FILIERE.getProprietaireMarque(((ChocolatDeMarque)produit).getMarque()));
			Filiere.LA_FILIERE.getBanque().faireFaillite(vendeur, this, cryptos.get(this));
		}
		if (!vendeur.vend(produit)) {
			System.err.println(vendeur.getNom()+" veut lancer un contrat cadre de "+produit+" mais sa methode vend("+produit+") retourne false");
			System.err.println("mis en faillite de "+vendeur);
			Filiere.LA_FILIERE.getBanque().faireFaillite(vendeur, this, cryptos.get(this));
		} else {
		//	System.out.println(vendeur+" lance et vend "+produit);
		}
		
		ContratCadre contrat = null;

		contrat = new ContratCadre(acheteur, vendeur, produit, echeancier, cryptogramme, tg);

		Echeancier contrePropositionA;
		journal.ajouter(Journal.texteColore(vendeur, "==>"+vendeur.getNom())+" lance le contrat #"+contrat.getNumero()+" de "+contrat.getQuantiteTotale()+" T de "+contrat.getProduit()+" a "+Journal.texteColore(acheteur, acheteur.getNom()));
		contrePropositionA=acheteur.contrePropositionDeLAcheteur(new ExemplaireContratCadre(contrat));

		if (contrePropositionA==null) {
			journal.ajouter("   "+Journal.texteColore(acheteur, acheteur.getNom()+" retourne null pour echeancier : arret des negociations"));
			journal.ajouter("contrat #"+contrat.getNumero()+Journal.texteColore(Color.RED,  Color.white, " ANNULE "));
			return null;// arret des negociations
		}
		if (!contrePropositionA.echeancierAcceptable()) {
			throw new SecurityException(" appel de contrePropositionDeLAcheteur(...) sur "+acheteur.getNom()+" retourne un echeancier qui ne respecte pas les conditions du document des distributeurs : "+contrePropositionA);
		}
		if (contrePropositionA.getQuantiteTotale()<SuperviseurVentesContratCadre.QUANTITE_MIN_ECHEANCIER) {
			journal.ajouter("   "+Journal.texteColore(acheteur, acheteur.getNom()+" retourne un echeancier dont la quantite totale est inferieure a "+SuperviseurVentesContratCadre.QUANTITE_MIN_ECHEANCIER)+" Arret des negos");
			return null;// arret des negociations
		}
		contrat.ajouterEcheancier(contrePropositionA);
		if (!contrat.accordSurEcheancier()) {
			journal.ajouter("   "+Journal.texteColore(acheteur, acheteur.getNom())+" propose un echeancier different de "+Journal.doubleSur(contrat.getQuantiteTotale(),4)+" T "+contrat.getEcheancier());
		}		
		return negociations(acheteur, vendeur, produit, echeancier, cryptogramme, tg, contrat,vendeur);
	}

	private ExemplaireContratCadre negociations(IAcheteurContratCadre acheteur, IVendeurContratCadre vendeur, Object produit, Echeancier echeancier, int cryptogramme, boolean tg, ContratCadre contrat, IActeur initiateur) {
        Journal journal = (produit instanceof ChocolatDeMarque)? journalNegoCM : journalNegoF;
		int maxNego = 5 + (int)(Filiere.random.nextDouble()*11); // Le nombre maximum de contrepropositions est compris dans [5, 15]

		// NEGOCIATIONS SUR L'ECHEANCIER
		Echeancier contrePropositionV, contrePropositionA;
		journal.ajouter(" negociations echeancier contrat #"+contrat.getNumero()+" vendeur="+Journal.texteColore(vendeur, vendeur.getNom())+" acheteur="+Journal.texteColore(acheteur, acheteur.getNom())+" de "+contrat.getQuantiteTotale()+" T de "+contrat.getProduit()+" a "+Journal.texteColore(vendeur, vendeur.getNom()));
		int numNego=0;
		do { 
			numNego++;
			contrePropositionV= vendeur.contrePropositionDuVendeur(new ExemplaireContratCadre(contrat));
			if (contrePropositionV==null) {
				journal.ajouter("   "+Journal.texteColore(vendeur, vendeur.getNom()+" retourne null pour echeancier : arret des negociations"));
				journal.ajouter("contrat #"+contrat.getNumero()+Journal.texteColore(Color.RED,  Color.white, " ANNULE "));
				return null;// arret des negociations
			}
			if (!contrePropositionV.echeancierAcceptable()) {
	//			throw new SecurityException(" appel de contrePropositionDuVendeur(...) sur "+vendeur.getNom()+" retourne un echeancier qui ne respecte pas les conditions du document des distributeurs : "+contrePropositionV);
				System.err.println(" appel de contrePropositionDuVendeur(...) sur "+vendeur.getNom()+" retourne un echeancier qui ne respecte pas les conditions du document des distributeurs : "+contrePropositionV+"\nmise en faillite du vendeur");
				Filiere.LA_FILIERE.getBanque().faireFaillite(vendeur, this, cryptos.get(this));
				return null;
			}
			contrat.ajouterEcheancier(contrePropositionV);
			if (!contrat.accordSurEcheancier()) {
				journal.ajouter("   "+Journal.texteColore(vendeur, vendeur.getNom())+" propose un echeancier different de "+Journal.doubleSur(contrat.getQuantiteTotale(),4)+" T "+contrat.getEcheancier());
				contrePropositionA=acheteur.contrePropositionDeLAcheteur(new ExemplaireContratCadre(contrat));
				if (contrePropositionA==null) {
					journal.ajouter("   "+Journal.texteColore(acheteur, acheteur.getNom()+" retourne null pour echeancier : arret des negociations"));
					journal.ajouter("contrat #"+contrat.getNumero()+Journal.texteColore(Color.RED,  Color.white, " ANNULE "));
					return null;// arret des negociations
				}
				if (!contrePropositionA.echeancierAcceptable()) {
					throw new SecurityException(" appel de contrePropositionDeLAcheteur(...) sur "+acheteur.getNom()+" retourne un echeancier qui ne respecte pas les conditions du document des distributeurs : "+contrePropositionA);
				}				
				contrat.ajouterEcheancier(contrePropositionA);
				if (!contrat.accordSurEcheancier()) {
					journal.ajouter("   "+Journal.texteColore(acheteur, acheteur.getNom())+" propose un echeancier different de "+Journal.doubleSur(contrat.getQuantiteTotale(),4)+" T "+contrat.getEcheancier());
				}
			}
		} while (numNego<maxNego && !contrat.accordSurEcheancier());
		if (!contrat.accordSurEcheancier()) {
			journal.ajouter("   aucun accord sur l'echeancier n'a pu etre trouve en moins de "+maxNego+" etapes : arret des negociations");
			journal.ajouter("contrat #"+contrat.getNumero()+Journal.texteColore(Color.RED,  Color.white, " ANNULE "));
			return null;
		} else {
			journal.ajouter("   accord sur l'echeancier : "+contrat.getEcheancier());
		}
		// NEGOCIATIONS SUR LE PRIX
		double propositionV = vendeur.propositionPrix(new ExemplaireContratCadre(contrat));
		journal.ajouter("   "+Journal.texteColore(vendeur, vendeur.getNom())+" propose un prix de "+Journal.doubleSur(propositionV,4));
		if (propositionV<=0.0) {
			journal.ajouter("   arret des negociations");
			journal.ajouter("contrat #"+contrat.getNumero()+Journal.texteColore(Color.RED,  Color.white, " ANNULE "));
			return null;// arret des negociations
		}
		contrat.ajouterPrix(propositionV);
		double propositionA;
		numNego=0;
		do {
			numNego++;
			propositionA = acheteur.contrePropositionPrixAcheteur(new ExemplaireContratCadre(contrat));
			journal.ajouter("   "+Journal.texteColore(acheteur, acheteur.getNom())+" propose un prix de "+Journal.doubleSur(propositionA,4));
			if (propositionA<=0.0) {
				journal.ajouter("   arret des negociations");
				journal.ajouter("contrat #"+contrat.getNumero()+Journal.texteColore(Color.RED,  Color.white, " ANNULE "));
				return null;// arret des negociations
			}
			contrat.ajouterPrix(propositionA);
			if (!contrat.accordSurPrix()) {
				propositionV = vendeur.contrePropositionPrixVendeur(new ExemplaireContratCadre(contrat));
				journal.ajouter("   "+Journal.texteColore(vendeur, vendeur.getNom())+" propose un prix de "+Journal.doubleSur(propositionV,4));
				if (propositionV<=0.0) {
					journal.ajouter("   arret des negociations");
					journal.ajouter("contrat #"+contrat.getNumero()+Journal.texteColore(Color.RED,  Color.white, " ANNULE "));
					return null;// arret des negociations
				}
				contrat.ajouterPrix(propositionV);
			}
		} while (numNego<maxNego && !contrat.accordSurPrix());

		if (!contrat.accordSurPrix()) {
			journal.ajouter("   aucun accord sur le prix n'a pu etre trouve en moins de "+maxNego+" etapes : arret des negociations");
			journal.ajouter("contrat #"+contrat.getNumero()+Journal.texteColore(Color.RED,  Color.white, " ANNULE "));
			return null;
		}
		contrat.signer();// accord : on realise les previsionnels de livraison et paiement
		// On notifie l'acteur qui n'est pas a l'origine de l'appel qu'un contrat vient d'etre signe
		if (initiateur==acheteur) {
			vendeur.notificationNouveauContratCadre(new ExemplaireContratCadre(contrat));
		} else {
			acheteur.notificationNouveauContratCadre(new ExemplaireContratCadre(contrat));
		}
		this.contratsEnCours.add(contrat);
		journal.ajouter(Journal.texteColore(Color.GREEN, Color.BLACK,"   contrat #"+contrat.getNumero()+" entre ")+Journal.texteColore(vendeur, vendeur.getNom())+" et "+Journal.texteColore(acheteur, acheteur.getNom())+" sur "+Journal.doubleSur(contrat.getQuantiteTotale(),4)+" de "+contrat.getProduit()+" etales sur "+contrat.getEcheancier());
		return new ExemplaireContratCadre(contrat);
	}

	public void recapitulerContratsEnCours() {
		this.journal.ajouter("Step "+Filiere.LA_FILIERE.getEtape()+" Contrats en cours : ");
		for (ContratCadre cc : this.contratsEnCours) {
			this.journal.ajouter(cc.oneLineHtml());
		}		
	}

	public void memoriserLesLivraisonsAuProchainStep() {
		for (IActeur ac : this.chocolatALivrerStep.keySet()) {
			double al =0.0;
			double alt =0.0;
			for (ContratCadre cc : this.contratsEnCours) {
				if (cc.getVendeur().equals(ac)) {// && cc.getProduit().getType().equals("ChocolatDeMarque")) {
					al+=cc.getQuantiteALivrerAuStep();
					alt+=cc.getQuantiteRestantALivrer();
				}
			}
			this.chocolatALivrerStep.get(ac).put(Filiere.LA_FILIERE.getEtape(), al);
			this.chocolatALivrerTotale.get(ac).put(Filiere.LA_FILIERE.getEtape(), alt);
		}
		if (this.aff.getValeur()!=0.0) {
			List<IActeur> vendeurs=new ArrayList<IActeur>();
			for (IActeur ac : this.chocolatALivrerStep.keySet()) {
				vendeurs.add(ac);
			}
			String entete="STEP;";
			for (IActeur ac : vendeurs) {
				entete+=ac.getNom()+"AuStep;"+ac.getNom()+"Total;";
			}
			try {
				PrintWriter aEcrire= new PrintWriter(new BufferedWriter(new FileWriter("docs"+File.separator+"CC_a_livrer.csv")));
				aEcrire.println(entete);//"STEP;VENDEUR;ACHETEUR;PRODUIT;PRIX;TG;QUANTITE;STEPDEBUT;STEPFIN");
				for (int i=1; i<Filiere.LA_FILIERE.getEtape(); i++) {
					String ligne="";
					ligne+=i+";";
					for (IActeur ac : vendeurs) {
						ligne+=this.chocolatALivrerStep.get(ac).get(i)+";"+this.chocolatALivrerTotale.get(ac).get(i)+";"; //+"AuStep;"+ac.getNom()+"Total;";
					}
					aEcrire.println( ligne );
				}
				aEcrire.close();
			}
			catch (IOException e) {
				throw new Error("Une operation sur les fichiers a leve l'exception "+e) ;
			}
		}
	}
	public void gererLesEcheancesDesContratsEnCours() {
		this.journal.ajouter("Step "+Filiere.LA_FILIERE.getEtape()+" GESTION DES ECHEANCES DES CONTRATS EN COURS ========");
		HashMap<IVendeurContratCadre, HashMap<IProduit,Double>> engageALivrer = new HashMap<IVendeurContratCadre, HashMap<IProduit,Double>>();
		HashMap<IAcheteurContratCadre, Double> engageAPayer = new HashMap<IAcheteurContratCadre, Double>();
		HashMap<IAcheteurContratCadre, Double> totalPaye = new HashMap<IAcheteurContratCadre, Double>();
		HashMap<IVendeurContratCadre, Double> totalRecu = new HashMap<IVendeurContratCadre, Double>();
		double chocoALivrer=0;// au cours de cette etape
		double chocoLivre=0;  // au cours de cette etape
		double fevesALivrer=0;// au cours de cette etape
		double fevesLivrees=0;// au cours de cette etape

		for (ContratCadre cc : this.contratsEnCours) {
			if (!engageALivrer.keySet().contains(cc.getVendeur())) {
				engageALivrer.put(cc.getVendeur(), new HashMap<IProduit,Double>());
			}
			if (!engageAPayer.keySet().contains(cc.getAcheteur())) {
				engageAPayer.put(cc.getAcheteur(), 0.0);
			}
			double resteALivr=0.0;
			if (!engageALivrer.get(cc.getVendeur()).keySet().contains(cc.getProduit())) {
				engageALivrer.get(cc.getVendeur()).put(cc.getProduit(), 0.0);
			} else {
				resteALivr = engageALivrer.get(cc.getVendeur()).get(cc.getProduit()).doubleValue();
			}
			engageALivrer.get(cc.getVendeur()).put(cc.getProduit(),resteALivr+cc.getQuantiteRestantALivrer());
			engageAPayer.put(cc.getAcheteur(),engageAPayer.get(cc.getAcheteur())+cc.getMontantRestantARegler());

			this.journal.ajouter("- contrat :"+cc.oneLineHtml());
			double aLivrer = cc.getQuantiteALivrerAuStep();
			if (aLivrer>0.0) {
				if (cc.getProduit().getType().equals("Feve")) {
					fevesALivrer+=aLivrer;
				} else {
					chocoALivrer+=aLivrer;
				}
				IVendeurContratCadre vendeur = cc.getVendeur();

				double lotLivre = vendeur.livrer(cc.getProduit(), aLivrer, new ExemplaireContratCadre(cc));
				this.journal.ajouter("  a livrer="+String.format("%.3f",aLivrer)+"  livre="+String.format("%.3f",lotLivre));
				if (lotLivre>0.0) {
					if (cc.getProduit().getType().equals("Feve")) {
						fevesLivrees+=lotLivre;
						double dejaLivre = 0;
						if (this.livraisonsFeves.get(cc.getProduit()).keySet().contains(Filiere.LA_FILIERE.getEtape())) {
							dejaLivre = this.livraisonsFeves.get(cc.getProduit()).get(Filiere.LA_FILIERE.getEtape());
						}
						this.livraisonsFeves.get(cc.getProduit()).put(Filiere.LA_FILIERE.getEtape(), dejaLivre+lotLivre);
					} else {
						chocoLivre+=lotLivre;
					}
					IAcheteurContratCadre acheteur = cc.getAcheteur();
					acheteur.receptionner(cc.getProduit(),lotLivre, new ExemplaireContratCadre(cc));
					Filiere.LA_FILIERE.ajouterEchange(this, this.cryptos.get(this), acheteur, cc.getProduit(), lotLivre, "cc");
					Filiere.LA_FILIERE.ajouterEchange(this, this.cryptos.get(this), vendeur, cc.getProduit(), -lotLivre, "cc");
					cc.livrer(lotLivre);
				} else if (lotLivre<0.0) {
					System.err.println(" La methode livrer() du vendeur "+vendeur.getNom()+" retourne un negatif --> mise en faillite");
					Filiere.LA_FILIERE.getBanque().faireFaillite(vendeur, this, cryptos.get(this));
				}
			} else {
				this.journal.ajouter("- rien a livrer a cette etape");
			}
			double aPayer = cc.getPaiementAEffectuerAuStep();
			if (aPayer>0.0) {
				IAcheteurContratCadre acheteur = cc.getAcheteur();
				Banque banque = Filiere.LA_FILIERE.getBanque();
				boolean virementOk = banque.virer(acheteur, cryptos.get(acheteur), cc.getVendeur(),aPayer);
				double effectivementPaye = virementOk ? aPayer : 0.0; 
				if (!totalPaye.keySet().contains(acheteur)) {
					totalPaye.put(acheteur,aPayer);
				} else {
					totalPaye.put(acheteur, totalPaye.get(acheteur)+aPayer);
				}
				if (!totalRecu.keySet().contains(cc.getVendeur())) {
					totalRecu.put(cc.getVendeur(),aPayer);
				} else {
					totalRecu.put(cc.getVendeur(), totalRecu.get(cc.getVendeur())+aPayer);
				}
				this.journal.ajouter("  a payer="+String.format("%.3f",aPayer)+"  paye="+String.format("%.3f",effectivementPaye));
				if (effectivementPaye>0.0) {
					cc.payer(effectivementPaye);
				}
			} else {
				this.journal.ajouter("- rien a payer a cette etape");
			}
			cc.penaliteLivraison();
			cc.penalitePaiement();
		}

		this.journal.ajouter("==== TOTAL LIVRAISONS ====");
		this.journal.ajouter(fevesLivrees+" T de feves sur "+fevesALivrer+" a livrer");
		this.journal.ajouter(chocoLivre+" T de choco sur "+chocoALivrer+" a livrer");
		this.journal.ajouter("===== RESTE A LIVRER =====");
		for (IVendeurContratCadre v : engageALivrer.keySet()) {
			this.journal.ajouter("..== "+v.getNom()+" : ");
			HashMap<IProduit,Double> ral = engageALivrer.get(v);
			for (IProduit p : ral.keySet()) {
				this.journal.ajouter("....-- "+p+" : "+ral.get(p));
			}
		}
		this.journal.ajouter("===== RESTE A PAYER =====");
		for (IAcheteurContratCadre a : engageAPayer.keySet()) {
			this.journal.ajouter("..== "+a.getNom()+" : "+engageAPayer.get(a));
		}
		this.journal.ajouter("===== SOMMES PAYEES AU STEP =====");
		for (IAcheteurContratCadre a : totalPaye.keySet()) {
			this.journal.ajouter("..== "+a.getNom()+" : "+totalPaye.get(a));
		}
		this.journal.ajouter("===== SOMMES RECUES AU STEP =====");
		for (IVendeurContratCadre a : totalRecu.keySet()) {
			this.journal.ajouter("..== "+a.getNom()+" : "+totalRecu.get(a));
		}
	}

	public void archiverContrats() {
		Banque banque = Filiere.LA_FILIERE.getBanque();
		List<ContratCadre> aArchiver = new ArrayList<ContratCadre>();
		for (ContratCadre cc : this.contratsEnCours) {
			if (banque.aFaitFaillite(cc.getVendeur()) || banque.aFaitFaillite(cc.getAcheteur()) || (cc.getMontantRestantARegler()==0.0 && cc.getQuantiteRestantALivrer()==0.0)) {
				aArchiver.add(cc);
			}
		}
		for (ContratCadre cc : aArchiver) {
			this.journal.ajouter("Archivage du contrat :<br>"+cc.toHtml());
			this.contratsEnCours.remove(cc);
			this.contratsTermines.add(cc);
		}
	}
public String surLargeur(String s, int largeur) {
	String t = s.replace("&nbsp;", " ");
	int caracteresAAjouter = largeur - t.length();
	for (int i=0; i<caracteresAAjouter; i++) {
		s=s+"&nbsp;";
	}
	return s;
}
	public void quiVendQuoi() {
		this.journalQVQCM.ajouter("== Qui vend/achete les chocolats de marque. Etape "+Filiere.LA_FILIERE.getEtape()+"===");
		List<ChocolatDeMarque> c = Filiere.LA_FILIERE.getChocolatsProduits();
		for (ChocolatDeMarque cm : c) {
			String s = "produit "+cm;
			s = surLargeur(s,30);
			s = s+"&nbsp;&nbsp;&nbsp;vendeurs "+getVendeurs(cm);
			s = surLargeur(s, 60);
			s = s+"&nbsp;&nbsp;acheteurs "+getAcheteurs(cm);
			s = surLargeur(s, 90);
			this.journalQVQCM.ajouter(s);
		}
		this.journalQVQF.ajouter("== Qui vend/achete les feves. Etape "+Filiere.LA_FILIERE.getEtape()+"===");
		for (Feve f : Feve.values()) {
			String s = "produit "+f;
			s = surLargeur(s,20);
			s = s+"&nbsp;&nbsp;&nbsp;vendeurs "+getVendeurs(f);
			s = surLargeur(s, 50);
			s = s+"&nbsp;&nbsp;acheteurs "+getAcheteurs(f);
			s = surLargeur(s, 80);
			this.journalQVQF.ajouter(s);
		}
	}
	public void next() {
		quiVendQuoi();
		memoriserLesLivraisonsAuProchainStep();
		recapitulerContratsEnCours();
		gererLesEcheancesDesContratsEnCours();
		archiverContrats();
		eliminerFraudeurs();
		eliminerMauvaisLivreursPayeurs();
		contratsToCSV();
	}

	public void eliminerFraudeurs() {
		List<IActeur> banqueroutes = new LinkedList<IActeur>();
		List<IActeur> acteurs = Filiere.LA_FILIERE.getActeursSolvables();
		for (IActeur a : acteurs) {
			if (a instanceof IVendeurContratCadre) {
				IVendeurContratCadre v = (IVendeurContratCadre)a;
				List<ChocolatDeMarque> l = Filiere.LA_FILIERE.getChocolatsProduits();
				for (ChocolatDeMarque cm : l) {
					if (!banqueroutes.contains(v)) {
						if (v.vend(cm)) {
							String marque = cm.getMarque();
							if (!Filiere.LA_FILIERE.getMarquesDistributeur().contains(marque)) {
								if (!Filiere.LA_FILIERE.getProprietaireMarque(marque).equals(v)) {
									System.err.println("l'equipe "+v+" vend du "+cm+" : l'entreprise est dissoute");
									banqueroutes.add(v);
								}
							}
						}
					}
				}
			}
		}
		for (IActeur v : banqueroutes) {
			Filiere.LA_FILIERE.getBanque().faireFaillite(v, this, cryptos.get(this));
		}
	}
	
	public void eliminerMauvaisLivreursPayeurs() {
		List<IActeur> banqueroutes= new LinkedList<IActeur>();
		for (ContratCadre cc : this.contratsEnCours) {
			int stepFin = cc.getEcheancier().getStepFin();
			if (Filiere.LA_FILIERE.getEtape()>stepFin+12) { // plus de 6 mois que le CC devrait etre honore
				if (cc.getQuantiteRestantALivrer()>ContratCadre.EPSILON) {
					System.err.println("Etape :"+Filiere.LA_FILIERE.getEtape()+" l'equipe "+cc.getVendeur()+" n'a pas su livrer l'integralite de ce qu'il devait livrer sur le contrat "+cc.getNumero()+" 6 mois apres la fin prevue : l'entreprise est dissoute");
					banqueroutes.add(cc.getVendeur());
				} else if (cc.getMontantRestantARegler()>10){ 
					System.err.println("Etape :"+Filiere.LA_FILIERE.getEtape()+" l'equipe "+cc.getAcheteur()+" n'a pas su payer l'integralite de ce qu'il devait payer sur le contrat "+cc.getNumero()+" 6 mois apres la fin prevue : l'entreprise est dissoute");
					banqueroutes.add(cc.getAcheteur());
				}
			}
		}
		for (IActeur v : banqueroutes) {
			Filiere.LA_FILIERE.getBanque().faireFaillite(v, this, cryptos.get(this));
		}
	}
	
	public String contratToCSV(ContratCadre cc) {
		return cc.getNumero()+";"+cc.getVendeur()+";"+cc.getAcheteur()+";"+cc.getProduit()+";"+cc.getPrix()+";"+cc.getTeteGondole()+";"+cc.getQuantiteTotale()+";"+cc.getEcheancier().getStepDebut()+";"+cc.getEcheancier().getStepFin()+";";
	}
	public void contratsToCSV() {
		//	Variable aff = Filiere.LA_FILIERE.getIndicateur("BourseCacao Aff.Graph");
		if (this.aff.getValeur()!=0.0) {


			try {
				PrintWriter aEcrire= new PrintWriter(new BufferedWriter(new FileWriter("docs"+File.separator+"CC.csv")));
				aEcrire.println("NUM;VENDEUR;ACHETEUR;PRODUIT;PRIX;TG;QUANTITE;STEPDEBUT;STEPFIN");
				for (ContratCadre cc : this.contratsEnCours) {						
					aEcrire.println( contratToCSV(cc) );
					//							System.out.println(s);
				}
				for (ContratCadre cc : this.contratsTermines) {						
					aEcrire.println( contratToCSV(cc) );
				}
				aEcrire.close();
			}
			catch (IOException e) {
				throw new Error("Une operation sur les fichiers a leve l'exception "+e) ;
			}
			try {
				PrintWriter aEcrire= new PrintWriter(new BufferedWriter(new FileWriter("docs"+File.separator+"CC_livraisonsFeves.csv")));
				Feve[] tfeve = Feve.values();
				String entete="";
				for (int i=0; i<tfeve.length-1; i++) {
					entete=entete+tfeve[i]+";";
				}
				entete=entete+tfeve[tfeve.length-1];
				aEcrire.println(entete);//"NUM;VENDEUR;ACHETEUR;PRODUIT;PRIX;TG;QUANTITE;STEPDEBUT;STEPFIN");
				for (int step=0; step<Filiere.LA_FILIERE.getEtape(); step++) {
					String ligne="";
					for (int i=0; i<tfeve.length-1; i++) {
						ligne=ligne+this.livraisonsFeves.get(tfeve[i]).get(step)+";";
					}
					ligne=ligne+this.livraisonsFeves.get(tfeve[tfeve.length-1]).get(step);
					aEcrire.println( ligne );
				}
				//						for (ContratCadre cc : this.contratsEnCours) {						
				//							aEcrire.println( contratToCSV(cc) );
				////							System.out.println(s);
				//						}
				//						for (ContratCadre cc : this.contratsTermines) {						
				//							aEcrire.println( contratToCSV(cc) );
				//						}
				aEcrire.close();
			}
			catch (IOException e) {
				throw new Error("Une operation sur les fichiers a leve l'exception "+e) ;
			}

		}	

	}

	public String getDescription() {
		return this.getNom();
	}

	public Color getColor() {
		return new Color(190, 190, 190);
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
		res.add(this.journalNegoCM);
		res.add(this.journalNegoF);
		res.add(this.journalQVQCM);
		res.add(this.journalQVQF);
		return res;
	}

	public void setCryptogramme(Integer crypto) {
	}

	public void notificationFaillite(IActeur acteur) {
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
