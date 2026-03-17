package abstraction.eqXRomu.acteurs;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import abstraction.eqXRomu.appelDOffre.IAcheteurAO;
import abstraction.eqXRomu.appelDOffre.OffreVente;
import abstraction.eqXRomu.appelDOffre.SuperviseurVentesAO;
import abstraction.eqXRomu.bourseCacao.BourseCacao;
import abstraction.eqXRomu.encheres.Enchere;
import abstraction.eqXRomu.encheres.SuperviseurVentesAuxEncheres;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.Gamme;

public class DistributeurXAcheteurAppelDOffre extends DistributeurXAcheteurAuxEncheres implements IAcheteurAO {

	private HashMap<ChocolatDeMarque, List<Double>> prixRetenus;
	private SuperviseurVentesAO supAO;
	protected Journal journalAO;

	public DistributeurXAcheteurAppelDOffre() {
		super();
		this.journalAO = new Journal(this.getNom()+" journal A.O.", this);
	}

	public void initialiser() {
		super.initialiser();
		this.supAO = (SuperviseurVentesAO)(Filiere.LA_FILIERE.getActeur("Sup.AO"));
		this.prixRetenus = new HashMap<ChocolatDeMarque, List<Double>>();
		for (ChocolatDeMarque cm : this.stockChocoMarque.keySet()) {
			this.prixRetenus.put(cm, new LinkedList<Double>());
		}		
	}

	public void next() {
		super.next();
		this.journalAO.ajouter("=== STEP "+Filiere.LA_FILIERE.getEtape()+" ====================");
		for (ChocolatDeMarque cm : this.stockChocoMarque.keySet()) {
			if (this.stockChocoMarque.get(cm)<95000) { // pas top... il faudrait considerer les besoins des clients finaux et les marges qu'on fait sur ce chocolat
				int quantite = 5000 + Filiere.random.nextInt((int)(100001-this.stockChocoMarque.get(cm))); // il faudrait aussi tenir compte des contrats cadres en cours afin de ne pas vendre ce qu'on s'est engage a livrer
				OffreVente ov = supAO.acheterParAO(this,  cryptogramme, cm, quantite);
				journalAO.ajouter("   Je lance un appel d'offre de "+quantite+" T de "+cm);
				if (ov!=null) { // on a retenu l'une des offres de vente
					journalAO.ajouter("   AO finalise : on ajoute "+quantite+" T de "+cm+" au stock");
					stockChocoMarque.put(cm, stockChocoMarque.get(cm)+quantite);
					totalStocksChocoMarque.ajouter(this, quantite, cryptogramme);
					prixRetenus.get(cm).add(ov.getPrixT());
					if (prixRetenus.get(cm).size()>10) {
						prixRetenus.get(cm).remove(0); // on ne garde que les dix derniers prix
						journalAO.ajouter("   Les derniers prix pour "+cm+" sont "+prixRetenus.get(cm));
					}
				}
			}
		}

		// On archive les contrats termines
		this.journalAO.ajouter("=================================");
	}

	public double prixMoyen(ChocolatDeMarque cm) {
		List<Double> prix=prixRetenus.get(cm);
		if (prix.size()>0) {
			double somme =0.0;
			
			for (Double d : prix) {
				somme+=d;
			}
			return somme/prix.size();
		} else {
			return 0.0;
		}
	}


	public OffreVente choisirOV(List<OffreVente> propositions) {
		double solde = Filiere.LA_FILIERE.getBanque().getSolde(this, cryptogramme);
		int selected=0;
		boolean adequat=false;
		while (selected<propositions.size() && !adequat) {
			if (!(propositions.get(selected).getProduit() instanceof ChocolatDeMarque)) {
				journalAO.ajouter("   produit "+propositions.get(selected).getProduit()+" n'est pas du chocolat de marque");
				selected++;
			} else if (!((ChocolatDeMarque)(propositions.get(selected).getProduit())).greaterThan((ChocolatDeMarque)(propositions.get(selected).getOffre().getProduit()))) {
				journalAO.ajouter("   produit "+propositions.get(selected).getProduit()+" ne peut pas se substituer a la demande de "+(ChocolatDeMarque)(propositions.get(selected).getOffre().getProduit()));
				selected++;
			} else {
				adequat=true;
			}
		}
		if (selected>=propositions.size()) {
			journalAO.ajouter("   refus de l'AO : produit pas adequat");
			return null;
		}
		ChocolatDeMarque cm = (ChocolatDeMarque)(propositions.get(selected).getProduit());
		double prix = propositions.get(selected).getPrixT();
		if (solde<prix*propositions.get(selected).getQuantiteT()) {
			journalAO.ajouter("   refus de l'AO : pas assez d'argent sur le compte");
			return null;
		} else {
			double prixMoyen = prixMoyen(cm);
			if (prixMoyen==0.0) {
				BourseCacao bourse = (BourseCacao)(Filiere.LA_FILIERE.getActeur("BourseCacao"));
				double max = bourse.getCours(Feve.F_MQ).getMax()*1.75;
				if (cm.getChocolat().getGamme()==Gamme.HQ) {
					max = bourse.getCours(Feve.F_MQ).getMax()*2.5;
				} else if (cm.getChocolat().getGamme()==Gamme.BQ) {
					max = bourse.getCours(Feve.F_BQ).getMax()*1.75;
				}
				if (prix>max) {
					journalAO.ajouter("   refus de l'AO : prix "+prix+" superieur a mon max "+max);
					return null;
				} else {
					return propositions.get(0);
				}
			} else { // on a deja passe des AO sur ce produit
				if (prixMoyen*1.05>prix) {
					prixRetenus.get(cm).add(prixMoyen*1.05); // on fait comme si on avait accepte avec 5% d'augmentation afin que lors des prochains echanges on accepte des prix un peu plus eleves
					if (prixRetenus.get(cm).size()>10) {
						prixRetenus.get(cm).remove(0); // on ne garde que les dix derniers prix
						journalAO.ajouter("   La meilleure offre pour du "+cm+" est de "+prix+" alors que le prix moyen des derniers echanges est de "+prixMoyen);
						journalAO.ajouter("   Les prix depasse les 5% de plus que les derniers prix moyens --> on ajoute un prix a 5% de plus a la liste pour accepter des prix un peu plus eleves pour "+cm);
					}
					return null;
				} else {
					return propositions.get(0);
				}
			}
		}
	}
	
	public List<Journal> getJournaux() {
		List<Journal> jx=super.getJournaux();
		jx.add(journalAO);
		return jx;
	}

}
