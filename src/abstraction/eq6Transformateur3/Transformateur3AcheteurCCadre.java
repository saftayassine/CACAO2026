package abstraction.eq6Transformateur3;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import abstraction.eqXRomu.acteurs.TransformateurXAcheteurBourse;
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


/**@author: Pol Bailleul */
public class Transformateur3AcheteurCCadre extends Transformateur3AcheteurBourse implements IAcheteurContratCadre {
    private SuperviseurVentesContratCadre supCC;
	protected List<ExemplaireContratCadre> contratsEnCours;
	private List<ExemplaireContratCadre> contratsTermines;
	protected Journal journalCC;

    public Transformateur3AcheteurCCadre() {
		super();
		this.contratsEnCours=new LinkedList<ExemplaireContratCadre>();
		this.contratsTermines=new LinkedList<ExemplaireContratCadre>();
		this.journalCC = new Journal(" journal Contrat Cadre EQ6", this);
	}

	public void initialiser() {
		super.initialiser();
		this.supCC = (SuperviseurVentesContratCadre)(Filiere.LA_FILIERE.getActeur("Sup.CCadre"));
	}

	public void next() {
		super.next();
		this.journalCC.ajouter("Etape"+Filiere.LA_FILIERE.getEtape());
				for (Feve f : stockFeve.getFeves()) { // pas forcement equitable : on avise si on lance un contrat cadre pour tout type de feve
					if (true) { 
						this.journalCC.ajouter("   "+f+" suffisamment peu en stock/contrat pour passer un CC");
						double parStep = Math.max(100, (21200-stockFeve.getQuantite(f)-restantDu(f))/12); // au moins 100
						Echeancier e = new Echeancier(Filiere.LA_FILIERE.getEtape()+1, 12, parStep);
						List<IVendeurContratCadre> vendeurs = supCC.getVendeurs(f);
						if (vendeurs.size()>0) {
							IVendeurContratCadre vendeur = vendeurs.get(Filiere.random.nextInt(vendeurs.size()));
							journalCC.ajouter("   "+vendeur.getNom()+" retenu comme vendeur parmi "+vendeurs.size()+" vendeurs potentiels");
							ExemplaireContratCadre contrat = supCC.demandeAcheteur(this, vendeur, f, e, cryptogramme, false);
							if (contrat==null) {
								journalCC.ajouter(Color.RED, Color.white,"   echec des negociations");
							} else {
								this.contratsEnCours.add(contrat);
								journalCC.ajouter(Color.GREEN, vendeur.getColor(), "   contrat signe");
							}
						} else {
							journalCC.ajouter("   pas de vendeur");
						}
					}
				}
		// On archive les contrats termines
		for (ExemplaireContratCadre c : this.contratsEnCours) {
			if (c.getQuantiteRestantALivrer()==0.0 && c.getMontantRestantARegler()<=0.0) {
				this.contratsTermines.add(c);
			}
		}
		for (ExemplaireContratCadre c : this.contratsTermines) {
			journalCC.ajouter("Archivage du contrat "+c);
			this.contratsEnCours.remove(c);
		}
        int etape = Filiere.LA_FILIERE.getEtape();
        journalCC.ajouter("Etape"+ etape);
	}

	public double restantDu(Feve f) {
		double res=0;
		for (ExemplaireContratCadre c : this.contratsEnCours) {
			if (c.getProduit().equals(f)) {
				res+=c.getQuantiteRestantALivrer();
			}
		}
		return res;
	}

	public double restantAPayer() {
		double res=0;
		for (ExemplaireContratCadre c : this.contratsEnCours) {
			res+=c.getMontantRestantARegler();
		}
		return res;
	}

	public List<Journal> getJournaux() {
    List<Journal> res = new ArrayList<Journal>(super.getJournaux());
    res.add(this.journalCC);
    return res;
	}

	public boolean achete(IProduit produit) {
		if (!produit.getType().equals("Feve")) {
			return false;
		}
		Feve f = (Feve) produit;
		return stockFeve.getQuantite(f)+restantDu(f) < 150000;
	}

	public Echeancier contrePropositionDeLAcheteur(ExemplaireContratCadre contrat) {
//		return null;
		if (!contrat.getProduit().getType().equals("Feve")) {
			return null;
		}

		if (stockFeve.getQuantite((Feve)(contrat.getProduit()))+restantDu((Feve)(contrat.getProduit()))+contrat.getEcheancier().getQuantiteTotale()<150000) {
			if (contrat.getEcheancier().getStepFin()-contrat.getEcheancier().getStepDebut()<11
					|| contrat.getEcheancier().getStepDebut()-Filiere.LA_FILIERE.getEtape()>8) {
				return new Echeancier(Filiere.LA_FILIERE.getEtape()+1, 12, contrat.getEcheancier().getQuantiteTotale()/12 );
			} else { // les volumes sont corrects, la duree et le debut aussi
				return contrat.getEcheancier();
			}
		} else {
			double marge = 150000 - stockFeve.getQuantite((Feve)(contrat.getProduit())) - restantDu((Feve)(contrat.getProduit()));
			if (marge<1200) {
				return null;
			} else {
				double quantite = 1200 + Filiere.random.nextDouble()*(marge-1200); // un nombre aleatoire entre 1200 et la marge
				return new Echeancier(Filiere.LA_FILIERE.getEtape()+1, 12, quantite/12 );
			}
		}
	}

	public double contrePropositionPrixAcheteur(ExemplaireContratCadre contrat) {
		BourseCacao bourse = (BourseCacao)(Filiere.LA_FILIERE.getActeur("BourseCacao"));
		double solde = Filiere.LA_FILIERE.getBanque().getSolde(this, cryptogramme)-restantAPayer();
		double prixSansDecouvert = solde / contrat.getQuantiteTotale();
		if (prixSansDecouvert<bourse.getCours(Feve.F_BQ).getValeur()) {
			return 0.0; // nous ne sommes pas en mesure de fournir un prix raisonnable
		}
		if (((Feve)contrat.getProduit()).isEquitable()) { // pas de cours en bourse
			double max = bourse.getCours(Feve.F_MQ).getMax()*1.25;
			double alea = Filiere.random.nextInt((int)max);
			if (contrat.getPrix()<Math.min(alea, prixSansDecouvert)) {
				return contrat.getPrix();
			} else {
				return Math.min(prixSansDecouvert, bourse.getCours(Feve.F_MQ).getValeur()*(1+(Filiere.random.nextInt(25)/100.0))); // entre 1 et 1.25 le prix de F_MQ
			}
		} else {
			double cours = bourse.getCours((Feve)contrat.getProduit()).getValeur();
			double coursMax = bourse.getCours((Feve)contrat.getProduit()).getMax();
			int alea = coursMax-cours>1 ? Filiere.random.nextInt((int)(coursMax-cours)) : 0;
			if (contrat.getPrix()<cours+alea) {
				return Math.min(prixSansDecouvert, contrat.getPrix());
			} else {
				return Math.min(prixSansDecouvert, cours*(1.1-(Filiere.random.nextDouble()/3.0)));
			}
		}
	}

	public void notificationNouveauContratCadre(ExemplaireContratCadre contrat) {
		journalCC.ajouter("Nouveau contrat :"+contrat);
		this.contratsEnCours.add(contrat);
	}

	public void receptionner(IProduit p, double quantiteEnTonnes, ExemplaireContratCadre contrat) {
		journalCC.ajouter("Reception de "+quantiteEnTonnes+" T de "+p+" du contrat "+contrat.getNumero());
		stockFeve.ajouterQuantite((Feve)p, quantiteEnTonnes);
		Eq6TotalStock.ajouter(this, quantiteEnTonnes, cryptogramme);
	}

}
