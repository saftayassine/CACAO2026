package abstraction.eq1Producteur1;

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
	private HashMap<Feve , Double > pourcentageAVendre = new HashMap<Feve , Double>();
	private SuperviseurVentesContratCadre supCC;
	protected List<ExemplaireContratCadre> contratsEnCours;
	private List<ExemplaireContratCadre> contratsTermines;
	protected Journal journalCC;
	protected int periode = 8 ;
	protected HashMap<Feve , Double > prixTonne = new HashMap<Feve , Double>();
	protected HashMap<Feve , Double > prixMinTonne = new HashMap<Feve , Double>();
	protected Journal journallivraisonCC ;



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

		this.prixMinTonne.put(Feve.F_BQ, 2800.);
		this.prixMinTonne.put(Feve.F_BQ_E,3300.);
        this.prixMinTonne.put(Feve.F_MQ,3300.);
        this.prixMinTonne.put(Feve.F_MQ_E,4000.);
        this.prixMinTonne.put(Feve.F_HQ,4000.);
        this.prixMinTonne.put(Feve.F_HQ_E,4000.);

    }

	public void initialiser() {
		super.initialiser();
		this.supCC = (SuperviseurVentesContratCadre)(Filiere.LA_FILIERE.getActeur("Sup.CCadre"));
	}


    /**
	 * Methode appelee par le superviseur afin de savoir si l'acheteur
	 * est pret a faire un contrat cadre sur le produit indique.
	 * @param produit
	 * @return Retourne false si le vendeur ne souhaite pas etablir de contrat 
	 * a cette etape pour ce type de produit (retourne true si il est pret a
	 * negocier un contrat cadre pour ce type de produit).
	 */


	public boolean vend(IProduit produit){
		if(produit == Feve.F_BQ || produit == Feve.F_MQ){
			return this.pourcentageAVendre.get(produit) < 70;
		}
		return false;
	}


	/**
	 * Methode appelee par le SuperviseurVentesContratCadre lors de la phase de negociation
	 * sur l'echeancier afin de connaitre la contreproposition du vendeur. Le vendeur
	 * peut connaitre les precedentes propositions d'echeanciers via un appel a la methode
	 * getEcheanciers() sur le contrat. Un appel a getEcheancier() sur le contrat retourne 
	 * le dernier echeancier que l'acheteur a propose.
	 * @param contrat
	 * @return Retourne null si le vendeur souhaite mettre fin aux negociations en renoncant a
	 * ce contrat. Retourne l'echeancier courant du contrat (contrat.getEcheancier()) si il est
	 * d'accord avec cet echeancier. Sinon, retourne un autre echeancier qui est une contreproposition.
	 */
	public Echeancier contrePropositionDuVendeur(ExemplaireContratCadre contrat){
		Echeancier e = contrat.getEcheancier();
		Feve f = (Feve) contrat.getProduit();

		// on regarde si on peut vendre
		if(this.onPeutVendre(f, e)){
			return e;

		}

		// on regarde si c'est 3 fois la même proposition en face
		List<Echeancier> lastEcheanchiers = contrat.getEcheanciers();

		if(lastEcheanchiers.size() > 6
				&& lastEcheanchiers.get(lastEcheanchiers.size()-3) == e
				&& lastEcheanchiers.get(lastEcheanchiers.size()-5) == e){
			return null;
		}


		// On propose une alternative que l'on peut tenir
		Echeancier newEcheancier = this.correctionEcheancier(e, f);
        return newEcheancier;
    }


	/**
	 * Methode appele par le SuperviseurVentesContratCadre apres une negociation reussie
	 * sur l'echeancier afin de connaitre le prix a la tonne que le vendeur propose.
	 * @param contrat
	 * @return La proposition initale du prix a la tonne.
	 */
	public double propositionPrix(ExemplaireContratCadre contrat){
	
        return this.prixTonne.get((Feve) contrat.getProduit());
    }

	/**
	 * Methode appelee par le SuperviseurVentesContratCadre apres une contreproposition
	 * de prix different de la part de l'acheteur, afin de connaitre la contreproposition
	 * de prix du vendeur.
	 * @param contrat
	 * @return Retourne un nombre inferieur ou egal a 0.0 si le vendeur souhaite mettre fin
	 * aux negociation en renoncant a ce contrat. Retourne le prix actuel a la tonne du 
	 * contrat (contrat.getPrix()) si le vendeur est d'accord avec ce prix.
	 * Sinon, retourne une contreproposition de prix.
	 */
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


	/**
	 * Methode appelee par le SuperviseurVentesContratCadre afin de notifier le
	 * vendeur de la reussite des negociations sur le contrat precise en parametre
	 * qui a ete initie par l'acheteur.
	 * Le superviseur veillera a l'application de ce contrat (des appels a livrer(...) 
	 * seront effectues lorsque le vendeur devra livrer afin d'honorer le contrat, et
	 * des transferts d'argent auront lieur lorsque l'acheteur paiera les echeances prevues)..
	 * @param contrat
	 */
	public void notificationNouveauContratCadre(ExemplaireContratCadre contrat){

    }

	/**
	 * Methode appelee par le SuperviseurVentesContratCadre lorsque le vendeur doit livrer 
	 * quantite tonnes de produit afin d'honorer le contrat precise en parametre. 
	 * @param produit
	 * @param quantite
	 * @param contrat
	 * @return Retourne la quantite livree. Une penalite est prevue si cette quantite
	 *  est inferieure a celle precisee en parametre
	 */
	public double livrer(IProduit produit, double quantite, ExemplaireContratCadre contrat){
        
        double vrai_quantite= Math.min(quantite,getStock((Feve)produit));
        this.takeFeve((Feve)produit, vrai_quantite);
		this.journallivraisonCC.ajouter("livraison de" + vrai_quantite + "tonnes de" + produit + "pour le contrat avec" + contrat.getAcheteur().getNom());
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
			switch (f) {
				case F_BQ:
					quantiteTot = Math.min(255000, this.stock.get(f) * (100 - this.pourcentageAVendre.get(f) - 5) / 100);
					break;
				case F_MQ:
					quantiteTot = Math.min(45000, this.stock.get(f) * (100 - this.pourcentageAVendre.get(f) - 5) / 100);
					break;
				default:
					return;
			}

			if (quantiteTot <= 0) return;
		}

		int temps = 24 + this.periode - Filiere.LA_FILIERE.getEtape() % 24;

		ArrayList<Double> quantites = new ArrayList<>();
		for (int k = 1; k < temps + 1; k++) {
			quantites.add(quantiteTot / temps);
		}

		Echeancier echeancier = new Echeancier(Filiere.LA_FILIERE.getEtape() + 1, quantites);
		if (this.onPeutVendre(f, echeancier)) {
			ExemplaireContratCadre contrat = supCC.demandeVendeur(acheteur, this, f, echeancier, this.cryptogramme, false);
			if (contrat == null) {
				journalCC.ajouter(Color.RED, Color.white, "   echec des negociations");
			} else {
				this.contratsEnCours.add(contrat);
				double pourcent = (quantiteTot / this.getStock(f)) * 100;
				this.pourcentageAVendre.put(f, pourcent + this.pourcentageAVendre.get(f));
				this.logContratSigne(contrat, f, quantiteTot, acheteur);
			}
		}
	}


	



	public void renouvellementContratCadre(IAcheteurContratCadre acheteur, Feve f, ExemplaireContratCadre contrat){
		int temps = 24; // durée fixe d'un renouvellement, comme dans propositionContratCadre
		double quantiteTot = Math.min(contrat.getQuantiteTotale(), this.stock.get(f) * (100 - this.pourcentageAVendre.get(f) - 5) / 100);

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
			double pourcent = (quantiteTot / this.getStock(f)) * 100;
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
		double pourcent = (quantiteRestante/this.getStock(f))*100;

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
		int tempsRestant = 23 - etape%24;
		double quantiteRestante = echeancier.getQuantiteJusquA(tempsRestant);
		double pourcent = (quantiteRestante/this.getStock(f))*100;

		double CetteAnnee = Math.min(quantiteRestante,(this.getStock(f))*(95-this.pourcentageAVendre.get(f))/100);

		double quantiteApres = echeancier.getQuantiteAPartirDe(etape +tempsRestant + 1) ;
		if(echeancier.getStepFin()> etape + 48 - etape%24){
			quantiteApres +=  - echeancier.getQuantiteAPartirDe(etape + 48 - etape%24) ;
		}

		double quantiteSurAnnee = Math.min(quantiteApres,100000000); // b: this.pourcentage next year

		List<Double> quantites = new ArrayList<>();
		for (int i = 0; i < tempsRestant; i++) { // on ajoute pour cette année
			quantites.add(CetteAnnee/tempsRestant);
		}

		for (int i = Math.max(etape + tempsRestant,echeancier.getStepDebut()); i < echeancier.getStepFin(); i++) { // on ajoute pour cette année
			quantites.add(quantiteSurAnnee/24);
		}



		Echeancier newEcheancier = new Echeancier(echeancier.getStepDebut(),quantites);

		return newEcheancier;

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
    double stock  = this.getStock(f);
    double pourcent = stock > 0 ? (quantiteTot / stock) * 100 : 0;

    journalCC.ajouter(Color.GREEN, acheteur.getColor(), "   contrat signé avec " + acheteur.getNom());
    journalCC.ajouter(Color.GREEN, acheteur.getColor(),
        "     produit : " + f
        + " | qté totale : " + String.format("%.1f", quantiteTot) + " t"
        + " | période : étape " + stepDebut + " → " + stepFin + " (" + duree + " steps)"
        + " | % du stock engagé : " + String.format("%.1f", pourcent) + "%"
        + " | % engagé cumulé : " + String.format("%.1f", this.pourcentageAVendre.get(f)) + "%");
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

				double stock = this.getStock(f);
				double pourcent = stock > 0 ? (quantiteEngagee / stock) * 100 : 0;
				this.pourcentageAVendre.put(f, pourcent);

				journalCC.ajouter("  ["+f+"] quantité engagée cette année : "+quantiteEngagee+" t"
					+" | stock : "+stock+" t"
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


