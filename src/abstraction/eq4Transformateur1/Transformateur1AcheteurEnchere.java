/** @author Yassine Safta */
package abstraction.eq4Transformateur1;

import java.util.HashMap;
import java.util.List;

import abstraction.eqXRomu.bourseCacao.BourseCacao;
import abstraction.eqXRomu.encheres.Enchere;
import abstraction.eqXRomu.encheres.IAcheteurAuxEncheres;
import abstraction.eqXRomu.encheres.MiseAuxEncheres;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Feve;

public class Transformateur1AcheteurEnchere extends Transformateur1VendeurEnchere implements IAcheteurAuxEncheres {
    private HashMap<Feve, Double> prix;	
	protected Journal journalAchatEncheres;
	private BourseCacao bourse;

	public Transformateur1AcheteurEnchere() {
		super();
		this.journalAchatEncheres = new Journal(this.getNom()+" journal Achat Encheres", this);

	}

	/** @author Ewan Lefort */
	public void initialiser() {
		super.initialiser();
		bourse=(BourseCacao)(Filiere.LA_FILIERE.getActeur("BourseCacao"));

		this.prix=new HashMap<Feve, Double>();
		List<Feve> cms =List.of(Feve.F_BQ,Feve.F_BQ_E,Feve.F_HQ,Feve.F_HQ_E,Feve.F_MQ,Feve.F_MQ_E);
		for (Feve cm : cms) {
			if (!cm.isEquitable()){
			double cours= bourse.getCours(cm).getValeur();
			prix.put(cm,  0.75*cours);
			}
			
			else if (cm==Feve.F_BQ_E){
					prix.put(cm, prix.get(Feve.F_BQ));
			}
			else if (cm==Feve.F_MQ_E){
					prix.put(cm, prix.get(Feve.F_MQ));
			}
			else if (cm==Feve.F_HQ_E){
					prix.put(cm, prix.get(Feve.F_HQ));
			}
			else{
					prix.put(cm, 0.0);				
				}
				
			}
		}
	

	public double proposerPrix(MiseAuxEncheres miseAuxEncheres) {
		if (!(miseAuxEncheres.getProduit() instanceof Feve)) {
			journalAchatEncheres.ajouter(" pour "+miseAuxEncheres.getProduit()+" prix propose = 0.0");
			return 0.0; // on ne fait des propositions que pour les feves
		}
		journalAchatEncheres.ajouter(" pour "+miseAuxEncheres.getProduit()+" prix propose = "+this.prix.get((Feve)(miseAuxEncheres.getProduit())));
		return this.prix.get((Feve)(miseAuxEncheres.getProduit()));
	}

	public void notifierAchatAuxEncheres(Enchere enchereRetenue) {
		Feve cm = (Feve)(enchereRetenue.getMiseAuxEncheres().getProduit());
		journalAchatEncheres.ajouter(" Enchere remportee : j'ajoute "+enchereRetenue.getQuantiteT()+" T de "+cm+" au stock");
		this.getStock().put(cm, this.getStock().get(cm)+enchereRetenue.getQuantiteT());
		prix.put(cm, prix.get(cm)*0.95); // on essayera un peu moins cher
		journalAchatEncheres.ajouter(" Enchere remportee : le prix de "+cm+" passe a "+prix.get(cm));
	}

	public void notifierEnchereNonRetenue(Enchere enchereNonRetenue) {
		Feve cm = (Feve)(enchereNonRetenue.getMiseAuxEncheres().getProduit());
		prix.put(cm, prix.get(cm)*1.05); // on essayera un peu plus cher
		journalAchatEncheres.ajouter(" Enchere non remportee : le prix de "+cm+" passe a "+prix.get(cm));
	}
	
	public List<Journal> getJournaux() {
		List<Journal> jx=super.getJournaux();
		jx.add(journalAchatEncheres);
		return jx;
}
}
