package abstraction.eq1Producteur1;

import abstraction.eqXRomu.bourseCacao.BourseCacao;
import abstraction.eqXRomu.contratsCadres.Echeancier;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.contratsCadres.IAcheteurContratCadre;
import abstraction.eqXRomu.contratsCadres.IVendeurContratCadre;
import abstraction.eqXRomu.contratsCadres.SuperviseurVentesContratCadre;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.IProduit;
import java.awt.Color;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Producteur1VendeurContratCadre extends Producteur1Cooperative implements IVendeurContratCadre{
	// Passage en protected pour y accéder depuis Producteur1VendeurBourse
	protected HashMap<Feve , Double > pourcentageAVendre = new HashMap<Feve , Double>();
	private SuperviseurVentesContratCadre supCC;
	protected List<ExemplaireContratCadre> contratsEnCours;
	private List<ExemplaireContratCadre> contratsTermines;
	protected Journal journalCC;
	protected int periode = 8 ;
	protected HashMap<Feve , Double > prixTonne = new HashMap<Feve , Double>();
	protected HashMap<Feve , Double > prixMinTonne = new HashMap<Feve , Double>();
	protected Journal journallivraisonCC ;
	
	// NOUVEAU : Stock de référence sauvegardé en début de cycle (période 0)
	protected HashMap<Feve, Double> stockDebutAnnee = new HashMap<Feve, Double>();


    public Producteur1VendeurContratCadre(){
        super();
		this.contratsEnCours=new LinkedList<ExemplaireContratCadre>();
		this.contratsTermines=new LinkedList<ExemplaireContratCadre>();
		this.journalCC = new Journal("Journal " +this.getNom()+" CC", this);
		this.journallivraisonCC = new Journal("Journal " + this.getNom()+" livraison CC", this);
		this.pourcentageAVendre.put(Feve.F_BQ,0.);
        this.pourcentageAVendre.put(Feve.F_BQ_E,0.);
        this.pourcentageAVendre.put(Feve.F_MQ,0.);
        this.pourcentageAVendre.put(Feve.F_MQ_E,0.);
        this.pourcentageAVendre.put(Feve.F_HQ,0.);
        this.pourcentageAVendre.put(Feve.F_HQ_E,0.);

		this.prixTonne.put(Feve.F_BQ, 3250.);
		this.prixTonne.put(Feve.F_BQ_E,3950.);
        this.prixTonne.put(Feve.F_MQ,3950.);
        this.prixTonne.put(Feve.F_MQ_E,4500.);
        this.prixTonne.put(Feve.F_HQ,4500.);
        this.prixTonne.put(Feve.F_HQ_E,5000.);

    }

	public void initialiser() {
		super.initialiser();
		this.supCC = (SuperviseurVentesContratCadre)(Filiere.LA_FILIERE.getActeur("Sup.CCadre"));
		this.prixMin();
		
		// NOUVEAU : Initialisation du stock de référence à la période 0
		for (Feve f : this.pourcentageAVendre.keySet()) {
			this.stockDebutAnnee.put(f, this.getStock(f));
		}
	}

	public void prixMin(){
		BourseCacao b =(BourseCacao) Filiere.LA_FILIERE.getActeur("BourseCacao");

		double prix = b.getCours(Feve.F_BQ).getValeur();
		this.prixMinTonne.put(Feve.F_BQ, prix*0.9);
		this.prixMinTonne.put(Feve.F_BQ_E, prix*0.9);

		prix = b.getCours(Feve.F_MQ).getValeur();
		this.prixMinTonne.put(Feve.F_MQ, prix*0.9);
		this.prixMinTonne.put(Feve.F_MQ_E, prix*0.9);

		prix = b.getCours(Feve.F_HQ).getValeur();
		this.prixMinTonne.put(Feve.F_HQ, prix*0.9);
		this.prixMinTonne.put(Feve.F_HQ_E, prix*0.9);
	}

	public boolean vend(IProduit produit){
		if(produit == Feve.F_BQ || produit == Feve.F_MQ){
			return this.pourcentageAVendre.get(produit) < 70;
		}
		return false;
	}

	public Echeancier contrePropositionDuVendeur(ExemplaireContratCadre contrat){
		Echeancier e = contrat.getEcheancier();
		Feve f = (Feve) contrat.getProduit();

		// on regarde si on peut vendre tel quel
		if(this.onPeutVendre(f, e)){
			return e;
		}

		// on regarde si c'est 3 fois la même proposition en face
		List<Echeancier> lastEcheanciers = contrat.getEcheanciers();
		if(lastEcheanciers.size() > 6
				&& lastEcheanciers.get(lastEcheanciers.size()-3) == e
				&& lastEcheanciers.get(lastEcheanciers.size()-5) == e){
			return null;
		}

		// On propose une alternative que l'on peut tenir
		Echeancier newEcheancier = this.correctionEcheancier(e, f);

		// 🚨 Si la correction donne un échéancier vide, on abandonne proprement
		if(newEcheancier.getQuantiteTotale() < 1.0){
			return null;
		}

		return newEcheancier;
	}

	public double propositionPrix(ExemplaireContratCadre contrat){
        return this.prixTonne.get((Feve) contrat.getProduit());
    }

	public double contrePropositionPrixVendeur(ExemplaireContratCadre contrat){
		Feve f =(Feve) contrat.getProduit();

		if(contrat.getPrix()< this.prixMinTonne.get(f)){
			// on regarde si c'est 3 fois la même proposition en face
			List<Echeancier> lastEcheanchiers = contrat.getEcheanciers();
			Echeancier e = contrat.getEcheancier();
			if(lastEcheanchiers.size() > 6
					&& lastEcheanchiers.get(lastEcheanchiers.size()-3) == e
					&& lastEcheanchiers.get(lastEcheanchiers.size()-5) == e){
			return 0.;
			}
			return this.prixMinTonne.get(f);
		}

        return contrat.getPrix();
    }

	public void notificationNouveauContratCadre(ExemplaireContratCadre contrat){

    }

	public double livrer(IProduit produit, double quantite, ExemplaireContratCadre contrat){
        double vrai_quantite= Math.min(quantite,getStock((Feve)produit));
        this.takeFeve((Feve)produit, vrai_quantite);
		this.journallivraisonCC.ajouter("livraison de " + vrai_quantite + " tonnes de " + produit + " pour le contrat avec " + contrat.getAcheteur().getNom());
        return vrai_quantite;
    }


	/////////////////////////////////////////
	//         Demande de contrats        //
	/////////////////////////////////////////

	public void initialisationContratCadre(Feve f){

		List<IAcheteurContratCadre> acheteurs = this.supCC.getAcheteurs(f);
		if (acheteurs.size()>0 && this.pourcentageAVendre.get(f)<=70) {
			IAcheteurContratCadre acheteur = acheteurs.get(Filiere.random.nextInt(acheteurs.size()));
			journalCC.ajouter("   "+acheteur.getNom()+" retenu comme acheteur parmi "+acheteurs.size()+" acheteurs potentiels");
			if(this.isContratdEnCours(acheteur, f)){
				//si on a déjà un contrat on le récupère
				ExemplaireContratCadre contrat = null;

				for (int i = 0; i < this.contratsEnCours.size(); i++) {
					ExemplaireContratCadre contratAct = this.contratsEnCours.get(i);
					if(contratAct.getAcheteur()==acheteur && contratAct.getProduit()==f && this.pourcentageAVendre.get(f)<=70){
						contrat = contratAct;
					}
				}
				// on regarde si le contrat se fini dans l'année
				int tempsRestant = Filiere.LA_FILIERE.getEtape()-contrat.getEcheancier().getStepFin();
				if(Filiere.LA_FILIERE.getEtape()%24 >= tempsRestant){
					this.renouvellementContratCadre(acheteur, f,contrat);
				}
			}
			else{
				this.propositionContratCadre(acheteur, f);
			}
		} else {
			journalCC.ajouter(" pas d'acheteur " + f);
		}
	}


	public void propositionContratCadre(IAcheteurContratCadre acheteur, Feve f){
		double quantiteTot = 0;

		if (null == f) {
			return;
		} else {
			// NOUVEAU : On utilise stockDebutAnnee
			double stockRef = this.stockDebutAnnee.getOrDefault(f, 0.0);
			switch (f) {
				case F_BQ:
					quantiteTot = Math.min(255000, stockRef * (100 - this.pourcentageAVendre.get(f) - 5) / 100);
					break;
				case F_MQ:
					quantiteTot = Math.min(45000, stockRef * (100 - this.pourcentageAVendre.get(f) - 5) / 100);
					break;
				default:
					return;
			}

			if (quantiteTot < 1.0) return;
		}

		int temps = 24 + this.periode - Filiere.LA_FILIERE.getEtape() % 24;

		ArrayList<Double> quantites = new ArrayList<>();
		for (int k = 1; k < temps + 1; k++) {
			quantites.add(quantiteTot / temps);
		}

		Echeancier echeancier = new Echeancier(Filiere.LA_FILIERE.getEtape() + 1, quantites);
		if (this.onPeutVendre(f, echeancier) && quantiteTot >=1) {
			ExemplaireContratCadre contrat = supCC.demandeVendeur(acheteur, this, f, echeancier, this.cryptogramme, false);
			if (contrat == null || quantiteTot < 1.0) {
				journalCC.ajouter(Color.RED, Color.white, "   echec des negociations");
			} else {
				this.contratsEnCours.add(contrat);
				// NOUVEAU : On utilise stockDebutAnnee pour le calcul du pourcentage
				double stockRef = this.stockDebutAnnee.getOrDefault(f, 0.0);
				double pourcent = stockRef > 0 ? (quantiteTot / stockRef) * 100 : 0;
				this.pourcentageAVendre.put(f, pourcent + this.pourcentageAVendre.get(f));
				this.logContratSigne(contrat, f, quantiteTot, acheteur);
			}
		}
	}


	public void renouvellementContratCadre(IAcheteurContratCadre acheteur, Feve f, ExemplaireContratCadre contrat){
		int temps = 24; // durée fixe d'un renouvellement, comme dans propositionContratCadre
		
		// NOUVEAU : On utilise stockDebutAnnee
		double stockRef = this.stockDebutAnnee.getOrDefault(f, 0.0);
		double quantiteTot = Math.min(contrat.getQuantiteTotale(), stockRef * (100 - this.pourcentageAVendre.get(f) - 5) / 100);

		if (quantiteTot <= 0) {
			journalCC.ajouter(Color.RED, Color.white, "   renouvellement annulé : quantité disponible nulle pour " + f);
			return;
		}

		ArrayList<Double> quantites = new ArrayList<>();
		for (int k = 0; k < temps; k++) {
			quantites.add(quantiteTot / temps);
		}

		Echeancier echeancier = new Echeancier(contrat.getEcheancier().getStepFin() + 1, quantites);
		ExemplaireContratCadre contratAct = supCC.demandeVendeur(acheteur, this, f, echeancier, this.cryptogramme, false);
		if (contratAct == null) {
			journalCC.ajouter(Color.RED, Color.white, "   echec des negociations");
		} else {
			this.contratsEnCours.add(contratAct);
			// NOUVEAU : On utilise stockDebutAnnee
			double pourcent = stockRef > 0 ? (quantiteTot / stockRef) * 100 : 0;
			this.pourcentageAVendre.put(f, pourcent + this.pourcentageAVendre.get(f));
			this.logContratSigne(contratAct, f, quantiteTot, acheteur);
		}
	}
	/////////////////////////////////
	//         Indicateurs         //
	/////////////////////////////////

	public boolean isContratdEnCours(IAcheteurContratCadre acheteur, Feve f){ //vérifie si l'acheteur a un conctract avec nous en cours sur un certain produit
		for (int i = 0; i < this.contratsEnCours.size(); i++) {
			ExemplaireContratCadre contrat = this.contratsEnCours.get(i);
			if(contrat.getAcheteur()==acheteur && contrat.getProduit()==f){
				return true;
			}
		}
		return false;
	}


	public boolean onPeutVendre(Feve f, Echeancier e){ //on regarde si on peut vendre sur les 2 prochaines années
		int etape = Filiere.LA_FILIERE.getEtape();
		int tempsRestant = 24 - etape%24;
		double quantiteRestante = e.getQuantiteJusquA(tempsRestant);
		
		// NOUVEAU : On utilise stockDebutAnnee
		double stockRef = this.stockDebutAnnee.getOrDefault(f, 0.0);
		double pourcent = stockRef > 0 ? (quantiteRestante/stockRef)*100 : 0;

		boolean cetteAnnee = pourcent < 100 - this.pourcentageAVendre.get(f);

		double quantiteApres = e.getQuantiteAPartirDe(etape +tempsRestant + 1) ;
		if(e.getStepFin()> etape + 48 - etape%24){
			quantiteApres +=  - e.getQuantiteAPartirDe(etape + 48 - etape%24) ;
		}

		boolean anneeApres = quantiteApres < 10000000 ; // < this.nextProd

		return anneeApres && cetteAnnee;

	}

	/////////////////////////////////
	//         Utilitaires         //
	/////////////////////////////////


	public Echeancier correctionEcheancier(Echeancier echeancier, Feve f){
		int etape = Filiere.LA_FILIERE.getEtape();
		int finAnnee = etape + 23 - (etape % 24);

		double stockDispo = this.stockDebutAnnee.getOrDefault(f, 0.0);
		double pourcentDispo = Math.max(0, 70 - this.pourcentageAVendre.getOrDefault(f, 0.0));
		double maxCetteAnnee = stockDispo * (pourcentDispo / 100.0);

		double demandeCetteAnnee = this.getQuantiteCetteAnnee(echeancier);
		
		// On calcule un ratio de réduction si on ne peut pas satisfaire toute la demande de l'année
		double ratioCetteAnnee = 1.0;
		if (demandeCetteAnnee > maxCetteAnnee && demandeCetteAnnee > 0) {
			ratioCetteAnnee = maxCetteAnnee / demandeCetteAnnee;
		}

		List<Double> quantites = new ArrayList<>();
		
		// On reconstruit l'échéancier en gardant les bons index (stepDebut à stepFin)
		for (int step = echeancier.getStepDebut(); step <= echeancier.getStepFin(); step++) {
			double qteDemandee = echeancier.getQuantite(step);
			if (step <= finAnnee) {
				// On applique la réduction si besoin pour cette année
				quantites.add(qteDemandee * ratioCetteAnnee);
			} else {
				// On accepte la quantité telle quelle pour l'année prochaine
				quantites.add(qteDemandee);
			}
		}

		return new Echeancier(echeancier.getStepDebut(), quantites);
	}
	


	public List<Journal> getJournaux() {
		List<Journal> res=super.getJournaux();
		res.add(this.journalCC);
		res.add(this.journallivraisonCC);
		return res;
	}


	private void logContratSigne(ExemplaireContratCadre contrat, Feve f, double quantiteTot, IAcheteurContratCadre acheteur) {
		int stepDebut = contrat.getEcheancier().getStepDebut();
		int stepFin   = contrat.getEcheancier().getStepFin();
		int duree     = stepFin - stepDebut;
		
		// NOUVEAU : On utilise stockDebutAnnee
		double stock  = this.stockDebutAnnee.getOrDefault(f, 0.0);
		double pourcent = stock > 0 ? (quantiteTot / stock) * 100 : 0;

		journalCC.ajouter(Color.GREEN, acheteur.getColor(), "   contrat signé avec " + acheteur.getNom());
		journalCC.ajouter(Color.GREEN, acheteur.getColor(),
			"     produit : " + f
			+ " | qté totale : " + String.format("%.1f", quantiteTot) + " t"
			+ " | période : étape " + stepDebut + " → " + stepFin + " (" + duree + " steps)"
			+ " | % du stock engagé : " + String.format("%.1f", pourcent) + "%"
			+ " | % engagé cumulé : " + String.format("%.1f", this.pourcentageAVendre.get(f)) + "%");
	}


	/**
	 * Calcule la quantité exacte prévue par un échéancier sur l'année en cours
	 */
	protected double getQuantiteCetteAnnee(Echeancier echeancier) {
		int etape = Filiere.LA_FILIERE.getEtape();
		int finAnnee = etape + 23 - (etape % 24);
		double qteFin = echeancier.getQuantiteJusquA(finAnnee);
		double qteDebut = etape > 0 ? echeancier.getQuantiteJusquA(etape - 1) : 0;
		return Math.max(0.0, qteFin - qteDebut);
	}

	/**
	 * Calcule la quantité exacte prévue par un échéancier sur l'année suivante
	 */
	protected double getQuantiteAnneeProchaine(Echeancier echeancier) {
		int etape = Filiere.LA_FILIERE.getEtape();
		int finAnnee = etape + 23 - (etape % 24);
		double qteFinNext = echeancier.getQuantiteJusquA(finAnnee + 24);
		double qteFinThis = echeancier.getQuantiteJusquA(finAnnee);
		return Math.max(0.0, qteFinNext - qteFinThis);
	}


	//////////////////////////////////////////
	//         En fonction du temps         //
	//////////////////////////////////////////


	public void next(){
		super.next();
		int etape = Filiere.LA_FILIERE.getEtape();

		// Réinitialisation annuelle du pourcentage à vendre
		if(etape % 24 == 0){
			journalCC.ajouter("=== Bilan annuel des contrats cadres (étape "+etape+") ===");

			// NOUVEAU : On actualise le stock de référence pour la nouvelle année qui commence
			for(Feve f : this.pourcentageAVendre.keySet()){
				this.stockDebutAnnee.put(f, this.getStock(f));
			}

			for(Feve f : this.pourcentageAVendre.keySet()){
				double quantiteEngagee = 0;
				int stepMin = Integer.MAX_VALUE;
				int stepMax = Integer.MIN_VALUE;

				for(ExemplaireContratCadre contrat : this.contratsEnCours){
					if(contrat.getProduit() == f){
						double qte = contrat.getEcheancier().getQuantiteJusquA(24);
						quantiteEngagee += qte;
						stepMin = Math.min(stepMin, contrat.getEcheancier().getStepDebut());
						stepMax = Math.max(stepMax, contrat.getEcheancier().getStepFin());
						journalCC.ajouter("  Contrat avec "+contrat.getAcheteur().getNom()
							+" | produit : "+f
							+" | qté totale : "+contrat.getQuantiteTotale()+" t"
							+" | période : étape "+contrat.getEcheancier().getStepDebut()
							+" → "+contrat.getEcheancier().getStepFin());
					}
				}

				// NOUVEAU : On utilise le stock mis à jour
				double stock = this.stockDebutAnnee.getOrDefault(f, 0.0);
				double pourcent = stock > 0 ? (quantiteEngagee / stock) * 100 : 0;
				this.pourcentageAVendre.put(f, pourcent);

				journalCC.ajouter("  ["+f+"] quantité engagée cette année : "+quantiteEngagee+" t"
					+" | stock de référence : "+stock+" t"
					+" | % engagé : "+String.format("%.1f", pourcent)+"%"
					+" | % disponible : "+String.format("%.1f", Math.max(0, 70 - pourcent))+"%");
			}
		}

		if(etape % 24 <= this.periode){
			this.initialisationContratCadre(Feve.F_BQ);
			this.initialisationContratCadre(Feve.F_MQ);
		}
	}
}

