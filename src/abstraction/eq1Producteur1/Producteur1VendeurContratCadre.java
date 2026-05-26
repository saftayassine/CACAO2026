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

	// FIX #10 : contrainte imposée par les distributeurs : un échéancier doit
	// faire au minimum 100 tonnes au total. Sous ce seuil, on refuse la négociation.
	protected static final double QTE_MIN_ECHEANCIER = 100.0;

	// pourcentageAVendre[f] = % de la RÉCOLTE ANNUELLE déjà engagée en CC POUR CETTE ANNÉE
	// (et non plus la quantité totale des contrats, qui peut s'étaler sur plusieurs années)
	protected HashMap<Feve , Double > pourcentageAVendre = new HashMap<Feve , Double>();
	private SuperviseurVentesContratCadre supCC;
	protected List<ExemplaireContratCadre> contratsEnCours;
	private List<ExemplaireContratCadre> contratsTermines;
	protected Journal journalCC;
	protected int periode = 8 ;
	protected HashMap<Feve , Double > prixTonne = new HashMap<Feve , Double>();
	protected HashMap<Feve , Double > prixMinTonne = new HashMap<Feve , Double>();
	protected Journal journallivraisonCC ;

	// Stock de référence sauvegardé en début de cycle (période 0)
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

	/**
	 * Calcule le plafond d'engagement (en pourcentage) pour une fève donnée, 
	 * de sorte à conserver exactement 500 tonnes de marge de sécurité.
	 */
	protected double getPlafondEngagement(Feve f) {
		double stockRef = this.stockDebutAnnee.getOrDefault(f, 0.0);
		
		if (stockRef <= 500.0) {
			return 0.0; // On ne vend rien si le stock est inférieur ou égal à 500t
		}
		
		double pourcentageMarge = (500.0 / stockRef) * 100.0;
		return 100.0 - pourcentageMarge;
	}

	public void initialiser() {
		super.initialiser();
		this.supCC = (SuperviseurVentesContratCadre)(Filiere.LA_FILIERE.getActeur("Sup.CCadre"));
		this.prixMin();

		// Initialisation du stock de référence à la période 0
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
			return this.pourcentageAVendre.get(produit) < this.getPlafondEngagement((Feve)produit);
		}
		return false;
	}

	public Echeancier contrePropositionDuVendeur(ExemplaireContratCadre contrat){
		Echeancier e = contrat.getEcheancier();
		Feve f = (Feve) contrat.getProduit();

		if(this.onPeutVendre(f, e)){
			return e;
		}

		List<Echeancier> lastEcheanciers = contrat.getEcheanciers();
		if(lastEcheanciers.size() > 6
				&& lastEcheanciers.get(lastEcheanciers.size()-3) == e
				&& lastEcheanciers.get(lastEcheanciers.size()-5) == e){
			return null;
		}

		Echeancier newEcheancier = this.correctionEcheancier(e, f);

		// FIX #10 : on refuse si l'échéancier corrigé est sous le seuil minimum (100t)
		// imposé par les distributeurs. Mieux vaut renvoyer null (refus propre) qu'un
		// échéancier non conforme qui entraînerait la mise en faillite.
		if(newEcheancier.getQuantiteTotale() < QTE_MIN_ECHEANCIER){
			journalCC.ajouter(Color.ORANGE, Color.white,
				"   contre-proposition refusée : qté corrigée ("
				+ String.format("%.1f", newEcheancier.getQuantiteTotale())
				+ " t) < seuil minimum (" + QTE_MIN_ECHEANCIER + " t)");
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
		Feve f = (Feve) contrat.getProduit();
		
		// 1. On ajoute le contrat à notre liste officielle
		this.contratsEnCours.add(contrat);

		// 2. On calcule l'impact sur l'année en cours
		double stockRef = this.stockDebutAnnee.getOrDefault(f, 0.0);
	
		double qteCetteAnnee = this.getQuantiteCetteAnnee(contrat.getEcheancier());
		double pourcent = stockRef > 0 ? (qteCetteAnnee / stockRef) * 100 : 0;

		// 3. On met à jour notre jauge d'engagement
		this.pourcentageAVendre.put(f, this.pourcentageAVendre.getOrDefault(f, 0.0) + pourcent);
		
		// 4. On l'inscrit dans le journal pour le suivi
		this.logContratSigne(contrat, f, contrat.getQuantiteTotale(), contrat.getAcheteur());
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
		if (acheteurs.size()>0 && this.pourcentageAVendre.get(f) <= this.getPlafondEngagement(f)) {
			IAcheteurContratCadre acheteur = acheteurs.get(Filiere.random.nextInt(acheteurs.size()));
			journalCC.ajouter("   "+acheteur.getNom()+" retenu comme acheteur parmi "+acheteurs.size()+" acheteurs potentiels");
			if(this.isContratdEnCours(acheteur, f)){
				ExemplaireContratCadre contrat = null;

				for (int i = 0; i < this.contratsEnCours.size(); i++) {
					ExemplaireContratCadre contratAct = this.contratsEnCours.get(i);
					if(contratAct.getAcheteur()==acheteur && contratAct.getProduit()==f && this.pourcentageAVendre.get(f) <= this.getPlafondEngagement(f)){
						contrat = contratAct;
					}
				}
				int tempsRestant = Filiere.LA_FILIERE.getEtape()-contrat.getEcheancier().getStepFin();
				if(Filiere.LA_FILIERE.getEtape()%24 >= tempsRestant){
					this.renouvellementContratCadre(acheteur, f,contrat);
				}
			}
			else{
				this.propositionContratCadre(acheteur, f);
			}
		} else {
			journalCC.ajouter(" pas d'acheteur " + f + " ou plafond atteint ("+this.pourcentageAVendre.get(f)+"%)");
		}
	}


	public void propositionContratCadre(IAcheteurContratCadre acheteur, Feve f){
		if (f == null) return;

		int etape = Filiere.LA_FILIERE.getEtape();
		double stockRef = this.stockDebutAnnee.getOrDefault(f, 0.0);

		double pourcentRestant = Math.max(0, this.getPlafondEngagement(f) - this.pourcentageAVendre.get(f));
		double quantiteAnnuelleMax = stockRef * (pourcentRestant / 100.0);

		// Plafonds par défaut sur les volumes négociés
		double plafondAbsolu;
		switch (f) {
			case F_BQ: plafondAbsolu = 255000; break;
			case F_MQ: plafondAbsolu = 45000;  break;
			default: return;
		}

		double quantiteAnnuelle = Math.min(plafondAbsolu, quantiteAnnuelleMax);
		// FIX #10 : on ne lance pas de négociation si on ne peut pas atteindre les 100t minimum
		if (quantiteAnnuelle < QTE_MIN_ECHEANCIER) {
			journalCC.ajouter(Color.ORANGE, Color.white,
				"   pas de nouveau contrat sur " + f
				+ " : marge dispo (" + String.format("%.1f", quantiteAnnuelle)
				+ " t) < seuil minimum (" + QTE_MIN_ECHEANCIER + " t)");
			return;
		}

		// FIX #2 : contrat sur exactement 24 étapes (1 an pile)
		// Le renouvellement automatique prendra le relais l'année suivante
		// On ne déborde plus sur l'année prochaine pour éviter d'engager des récoltes futures
		int temps = 24;
		double quantiteTot = quantiteAnnuelle;  // tout sera livré dans l'année

		ArrayList<Double> quantites = new ArrayList<>();
		for (int k = 0; k < temps; k++) {
			quantites.add(quantiteTot / temps);
		}

		Echeancier echeancier = new Echeancier(etape + 1, quantites);

		if (this.onPeutVendre(f, echeancier) && quantiteTot >= 1) {
			ExemplaireContratCadre contrat = supCC.demandeVendeur(acheteur, this, f, echeancier, this.cryptogramme, false);
			if (contrat == null) {
				journalCC.ajouter(Color.RED, Color.white, "   echec des negociations");
			} else {
				this.contratsEnCours.add(contrat);

				// FIX #1 : on compte UNIQUEMENT la quantité livrée DANS L'ANNÉE EN COURS
				// (avant : on comptait quantiteTot complète, ce qui faisait sauter à 95% en 1 contrat)
				double quantiteCetteAnnee = this.getQuantiteCetteAnnee(contrat.getEcheancier());
				double pourcent = stockRef > 0 ? (quantiteCetteAnnee / stockRef) * 100 : 0;
				this.pourcentageAVendre.put(f, this.pourcentageAVendre.get(f) + pourcent);

				this.logContratSigne(contrat, f, quantiteTot, acheteur);
			}
		}
	}


	public void renouvellementContratCadre(IAcheteurContratCadre acheteur, Feve f, ExemplaireContratCadre contrat){
		int temps = 24;

		double stockRef = this.stockDebutAnnee.getOrDefault(f, 0.0);
		double pourcentRestant = Math.max(0, this.getPlafondEngagement(f) - this.pourcentageAVendre.get(f));
		double quantiteAnnuelleMax = stockRef * (pourcentRestant / 100.0);
		double quantiteTot = Math.min(contrat.getQuantiteTotale(), quantiteAnnuelleMax);

		// FIX #10 : pas de renouvellement si la quantité proposée est sous 100t
		if (quantiteTot < QTE_MIN_ECHEANCIER) {
			journalCC.ajouter(Color.RED, Color.white,
				"   renouvellement annulé pour " + f
				+ " : qté (" + String.format("%.1f", quantiteTot)
				+ " t) < seuil minimum (" + QTE_MIN_ECHEANCIER + " t)");
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

			// FIX #3 : on compte UNIQUEMENT la quantité livrée dans l'année qui démarre
			double quantiteCetteAnnee = this.getQuantiteCetteAnnee(contratAct.getEcheancier());
			double pourcent = stockRef > 0 ? (quantiteCetteAnnee / stockRef) * 100 : 0;
			this.pourcentageAVendre.put(f, this.pourcentageAVendre.get(f) + pourcent);

			this.logContratSigne(contratAct, f, quantiteTot, acheteur);
		}
	}
	/////////////////////////////////
	//         Indicateurs         //
	/////////////////////////////////

	public boolean isContratdEnCours(IAcheteurContratCadre acheteur, Feve f){
		for (int i = 0; i < this.contratsEnCours.size(); i++) {
			ExemplaireContratCadre contrat = this.contratsEnCours.get(i);
			if(contrat.getAcheteur()==acheteur && contrat.getProduit()==f){
				return true;
			}
		}
		return false;
	}


	public boolean onPeutVendre(Feve f, Echeancier e){
		int etape = Filiere.LA_FILIERE.getEtape();
		int tempsRestant = 24 - etape%24;
		double quantiteRestante = e.getQuantiteJusquA(tempsRestant);

		double stockRef = this.stockDebutAnnee.getOrDefault(f, 0.0);
		double pourcent = stockRef > 0 ? (quantiteRestante/stockRef)*100 : 0;

		boolean cetteAnnee = pourcent < this.getPlafondEngagement(f) - this.pourcentageAVendre.get(f);

		double quantiteApres = e.getQuantiteAPartirDe(etape +tempsRestant + 1) ;
		if(e.getStepFin()> etape + 48 - etape%24){
			quantiteApres +=  - e.getQuantiteAPartirDe(etape + 48 - etape%24) ;
		}

		boolean anneeApres = quantiteApres < 10000000 ;

		return anneeApres && cetteAnnee;

	}

	/////////////////////////////////
	//         Utilitaires         //
	/////////////////////////////////


	public Echeancier correctionEcheancier(Echeancier echeancier, Feve f){
		int etape = Filiere.LA_FILIERE.getEtape();
		int finAnnee = etape + 23 - (etape % 24);

		double stockDispo = this.stockDebutAnnee.getOrDefault(f, 0.0);
		double pourcentDispo = Math.max(0, this.getPlafondEngagement(f) - this.pourcentageAVendre.getOrDefault(f, 0.0));
		double maxCetteAnnee = stockDispo * (pourcentDispo / 100.0);

		double demandeCetteAnnee = this.getQuantiteCetteAnnee(echeancier);

		double ratioCetteAnnee = 1.0;
		if (demandeCetteAnnee > maxCetteAnnee && demandeCetteAnnee > 0) {
			ratioCetteAnnee = maxCetteAnnee / demandeCetteAnnee;
		}

		List<Double> quantites = new ArrayList<>();

		for (int step = echeancier.getStepDebut(); step <= echeancier.getStepFin(); step++) {
			double qteDemandee = echeancier.getQuantite(step);
			if (step <= finAnnee) {
				quantites.add(qteDemandee * ratioCetteAnnee);
			} else {
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

		double stock  = this.stockDebutAnnee.getOrDefault(f, 0.0);
		// On log à la fois la quantité totale du contrat ET la quantité de l'année en cours (plus pertinente)
		double quantiteCetteAnnee = this.getQuantiteCetteAnnee(contrat.getEcheancier());
		double pourcentAnnee = stock > 0 ? (quantiteCetteAnnee / stock) * 100 : 0;

		journalCC.ajouter(Color.GREEN, acheteur.getColor(), "   contrat signé avec " + acheteur.getNom());
		journalCC.ajouter(Color.GREEN, acheteur.getColor(),
			"     produit : " + f
			+ " | qté totale contrat : " + String.format("%.1f", quantiteTot) + " t"
			+ " | qté livrée cette année : " + String.format("%.1f", quantiteCetteAnnee) + " t"
			+ " | période : étape " + stepDebut + " → " + stepFin + " (" + duree + " steps)"
			+ " | % de la récolte engagé cette année : " + String.format("%.1f", pourcentAnnee) + "%"
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

			// On actualise le stock de référence pour la nouvelle année qui commence
			for(Feve f : this.pourcentageAVendre.keySet()){
				this.stockDebutAnnee.put(f, this.getStock(f));
			}

			for(Feve f : this.pourcentageAVendre.keySet()){
				double quantiteEngageeCetteAnnee = 0;

				for(ExemplaireContratCadre contrat : this.contratsEnCours){
					if(contrat.getProduit() == f){
						double qteAnnee = this.getQuantiteCetteAnnee(contrat.getEcheancier());
						quantiteEngageeCetteAnnee += qteAnnee;

						journalCC.ajouter("  Contrat avec "+contrat.getAcheteur().getNom()
							+" | produit : "+f
							+" | qté livrée cette année : "+String.format("%.1f", qteAnnee)+" t"
							+" | qté totale restante : "+contrat.getQuantiteTotale()+" t"
							+" | période : étape "+contrat.getEcheancier().getStepDebut()
							+" → "+contrat.getEcheancier().getStepFin());
					}
				}

				double stock = this.stockDebutAnnee.getOrDefault(f, 0.0);
				double pourcent = stock > 0 ? (quantiteEngageeCetteAnnee / stock) * 100 : 0;
				this.pourcentageAVendre.put(f, pourcent);

				journalCC.ajouter("  ["+f+"] quantité engagée cette année : "+String.format("%.1f", quantiteEngageeCetteAnnee)+" t"
					+" | stock de référence : "+String.format("%.1f", stock)+" t"
					+" | % engagé : "+String.format("%.1f", pourcent)+"%"
					+" | % disponible : "+String.format("%.1f", Math.max(0, this.getPlafondEngagement(f) - pourcent))+"%");
			}
		}

		if(etape % 24 <= this.periode){
			this.initialisationContratCadre(Feve.F_BQ);
			this.initialisationContratCadre(Feve.F_MQ);
		}
	}
}