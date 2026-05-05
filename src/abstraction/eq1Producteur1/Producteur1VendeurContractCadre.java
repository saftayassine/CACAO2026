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

public class Producteur1VendeurContractCadre extends Producteur1VendeurAuxEncheres implements IVendeurContratCadre{
	private HashMap<Feve , Double > pourcentageAVendre = new HashMap<Feve , Double>();
	protected int periode = 8 ;
	private SuperviseurVentesContratCadre supCC;
	protected List<ExemplaireContratCadre> contratsEnCours;
	private List<ExemplaireContratCadre> contratsTermines;
	protected Journal journalCC;

    public Producteur1VendeurContractCadre(){
        super();
		this.contratsEnCours=new LinkedList<ExemplaireContratCadre>();
		this.contratsTermines=new LinkedList<ExemplaireContratCadre>();
		this.journalCC = new Journal(this.getNom()+" journal CC", this);
		this.pourcentageAVendre.put(Feve.F_BQ,0.);
        this.pourcentageAVendre.put(Feve.F_BQ_E,0.);
        this.pourcentageAVendre.put(Feve.F_MQ,0.);
        this.pourcentageAVendre.put(Feve.F_MQ_E,0.);
        this.pourcentageAVendre.put(Feve.F_HQ,0.);
        this.pourcentageAVendre.put(Feve.F_HQ_E,0.);

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
        if(produit instanceof Feve){
            if(this.getStock((Feve)produit) != 0){
                return true;
            }
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
        return contrat.getEcheancier();
    }
	
	/**
	 * Methode appele par le SuperviseurVentesContratCadre apres une negociation reussie
	 * sur l'echeancier afin de connaitre le prix a la tonne que le vendeur propose.
	 * @param contrat
	 * @return La proposition initale du prix a la tonne.
	 */
	public double propositionPrix(ExemplaireContratCadre contrat){
        return 10000;
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
        return vrai_quantite;
    }


	public void propositionContractCadre(IAcheteurContratCadre acheteur, Feve f){
		// vérifier si c'est un nouvel acheteur. Si c'est un ancien (avec qui on a déjà fait des contracts) on adapte la demande 

		double quantiteTot = 0;

		if (null == f) {
			return;
		} else {
			switch (f) {
				case F_BQ:
					quantiteTot = 255000;
					break;
				case F_MQ:
					quantiteTot = 45000;
					break;
				default:
					return;
			}
		}

		double pourcent = (quantiteTot/this.getStock(f))*100;
		this.pourcentageAVendre.put(f,pourcent + this.pourcentageAVendre.get(f));

		int temps = 24 + this.periode - Filiere.LA_FILIERE.getEtape()%24 ;


		ArrayList<Double> quantites = new ArrayList<>();
		for (int k = 1; k < temps + 1; k++) {
			quantites.add(quantiteTot/temps);
		}

		Echeancier echeancier = new Echeancier(Filiere.LA_FILIERE.getEtape()+1,quantites);
		ExemplaireContratCadre contrat = supCC.demandeVendeur(acheteur,this,f,echeancier,this.cryptogramme,false);
					if (contrat==null) {
						journalCC.ajouter(Color.RED, Color.white,"   echec des negociations");
					} else {
						this.contratsEnCours.add(contrat);
						journalCC.ajouter(Color.GREEN, acheteur.getColor(), "   contrat signe");
					}


	}


	//initier des contracts
	public void initialisationContractCadre(Feve f){

		List<IAcheteurContratCadre> acheteurs = this.supCC.getAcheteurs(f);
		if (acheteurs.size()>0 && this.pourcentageAVendre.get(f)<=70) {
			IAcheteurContratCadre acheteur = acheteurs.get(Filiere.random.nextInt(acheteurs.size()));
			journalCC.ajouter("   "+acheteur.getNom()+" retenu comme acheteur parmi "+acheteurs.size()+" acheteurs potentiels");
			if(this.isContractdEnCours(acheteur, f)){
				//si on a déjà un contract on le récupère
				ExemplaireContratCadre contract = null;

				for (int i = 0; i < this.contratsEnCours.size(); i++) {
					ExemplaireContratCadre contractAct = this.contratsEnCours.get(i);
					if(contract.getAcheteur()==acheteur && contractAct.getProduit()==f){
						contract = contractAct;
					}
				}
				// on regarde si le contract se fini dans l'année
				int tempsRestant = Filiere.LA_FILIERE.getEtape()-contract.getEcheancier().getStepFin();
				if(Filiere.LA_FILIERE.getEtape()%24 >= tempsRestant){
					this.renouvellementContractCadre(acheteur, f,contract);
				}
			}

			else{
				this.propositionContractCadre(acheteur, f);
			}
		} else {
			journalCC.ajouter("   pas d'acheteur " + f);
		}
	}




	public boolean isContractdEnCours(IAcheteurContratCadre acheteur, Feve f){ //vérifie si l'acheteur a un conctract avec nous en cours sur un certain produit
		for (int i = 0; i < this.contratsEnCours.size(); i++) {
			ExemplaireContratCadre contract = this.contratsEnCours.get(i);
			if(contract.getAcheteur()==acheteur && contract.getProduit()==f){
				return true;
			}
		}
		return false;
	}



	public void renouvellementContractCadre(IAcheteurContratCadre acheteur, Feve f, ExemplaireContratCadre contract){

		int temps = 24 -this.periode + contract.getEcheancier().getStepFin() + (int) Math.floor((contract.getEcheancier().getStepFin() - contract.getEcheancier().getStepDebut())/24) * 24  ;
		double quantiteTot = contract.getQuantiteTotale();

		ArrayList<Double> quantites = new ArrayList<>();
		for (int k = 1; k < temps + 1; k++) {
			quantites.add(quantiteTot/temps);
		}

		Echeancier echeancier = new Echeancier(contract.getEcheancier().getStepFin()+1,quantites);
		ExemplaireContratCadre contrat = supCC.demandeVendeur(acheteur,this,f,echeancier,this.cryptogramme,false);
					if (contrat==null) {
						journalCC.ajouter(Color.RED, Color.white,"   echec des negociations");
					} else {
						this.contratsEnCours.add(contrat);
						journalCC.ajouter(Color.GREEN, acheteur.getColor(), "   contrat signe");
					}
	}

	public void next(){
		super.next();
		int etape = Filiere.LA_FILIERE.getEtape();
		if(etape%24 <= this.periode){
			this.initialisationContractCadre(Feve.F_BQ);
			this.initialisationContractCadre(Feve.F_MQ);
		}


	}
}


