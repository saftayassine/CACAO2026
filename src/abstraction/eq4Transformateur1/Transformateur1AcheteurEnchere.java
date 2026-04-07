package abstraction.eq4Transformateur1;

import java.util.HashMap;
import java.util.List;

import abstraction.eqXRomu.encheres.Enchere;
import abstraction.eqXRomu.encheres.IAcheteurAuxEncheres;
import abstraction.eqXRomu.encheres.MiseAuxEncheres;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.produits.ChocolatDeMarque;

public class Transformateur1AcheteurEnchere extends Transformateur1Stock implements IAcheteurAuxEncheres {
    private HashMap<Feve, Double> prix;	
	protected Journal journalEncheres;

	public Transformateur1AcheteurEnchere() {
		super();
		this.journalEncheres = new Journal(this.getNom()+" journal Encheres", this);

	}
	public void initialiser() {
		super.initialiser();
		this.prix=new HashMap<Feve, Double>();
		List<Feve> Fs = Filiere.LA_FILIERE.getChocolatsProduits();
		for (Feve f : Fs) {
			this.prix.put(f,  this.prix(f)*0.75); // initialement on va proposer 75%
		}
	}

	
	public double proposerPrix(MiseAuxEncheres miseAuxEncheres) {
		if (!(miseAuxEncheres.getProduit() instanceof Feve)) {
			journalEncheres.ajouter(" pour "+miseAuxEncheres.getProduit()+" prix propose = 0.0");
			return 0.0; // on ne fait des propositions que pour les feves
		}
		journalEncheres.ajouter(" pour "+miseAuxEncheres.getProduit()+" prix propose = "+this.prix.get((Feve)(miseAuxEncheres.getProduit())));
		return this.prix.get((Feve)(miseAuxEncheres.getProduit()));
	}

	public void notifierAchatAuxEncheres(Enchere enchereRetenue) {
		ChocolatDeMarque cm = (ChocolatDeMarque)(enchereRetenue.getMiseAuxEncheres().getProduit());
		journalEncheres.ajouter(" Enchere remportee : j'ajoute "+enchereRetenue.getQuantiteT()+" T de "+cm+" au stock");
		stockChocoMarque.put(cm, stockChocoMarque.get(cm)+enchereRetenue.getQuantiteT());
		totalStocksChocoMarque.ajouter(this, enchereRetenue.getQuantiteT(), cryptogramme);
		prix.put(cm, prix.get(cm)*0.95); // on essayera un peu moins cher
		journalEncheres.ajouter(" Enchere remportee : le prix de "+cm+" passe a "+prix.get(cm));
	}

	public void notifierEnchereNonRetenue(Enchere enchereNonRetenue) {
		ChocolatDeMarque cm = (ChocolatDeMarque)(enchereNonRetenue.getMiseAuxEncheres().getProduit());
		prix.put(cm, prix.get(cm)*1.05); // on essayera un peu plus cher
		journalEncheres.ajouter(" Enchere remportee : le prix de "+cm+" passe a "+prix.get(cm));
	}
	
	public List<Journal> getJournaux() {
		List<Journal> jx=super.getJournaux();
		jx.add(journalEncheres);
		return jx;
}
}