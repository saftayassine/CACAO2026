package abstraction.eq5Transformateur2;

import java.util.HashMap;
import java.util.List;
import abstraction.eqXRomu.produits.Gamme;
import abstraction.eqXRomu.bourseCacao.IAcheteurBourse;
import abstraction.eqXRomu.encheres.Enchere;
import abstraction.eqXRomu.encheres.ExempleAbsAcheteurAuxEncheres;
import abstraction.eqXRomu.encheres.IAcheteurAuxEncheres;
import abstraction.eqXRomu.encheres.IVendeurAuxEncheres;
import abstraction.eqXRomu.encheres.MiseAuxEncheres;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.Feve;

/**
 * Auteur Raphaël
 */


/**
public class Transformateur2AchatEncheres extends ExempleAbsAcheteurAuxEncheres implements IAcheteurAuxEncheres {

    public Transformateur2AchatEncheres(double prixInit) {
        super(prixInit);
        //TODO Auto-generated constructor stub
    }

    @Override
    public double proposerPrix(MiseAuxEncheres miseAuxEncheres) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'proposerPrix'");
    }

    @Override
    public void notifierAchatAuxEncheres(Enchere enchereRetenue) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'notifierAchatAuxEncheres'");
    }

    @Override
    public void notifierEnchereNonRetenue(Enchere enchereNonRetenue) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'notifierEnchereNonRetenue'");
    }
    

    protected HashMap<IVendeurAuxEncheres, Double> prix;
	
	public ExempleAcheteurAuxEncheres(double prixInit) {
		super(prixInit);
		this.prix=new HashMap<IVendeurAuxEncheres, Double>();
	}

	public double proposerPrix(MiseAuxEncheres offre) {
		journal.ajouter("ProposerPrix("+offre+"):");
			double px = this.prixInit;
			if (this.prix.keySet().contains(offre.getVendeur())) {
				px = this.prix.get(offre.getVendeur());
			}
			journal.ajouter("   je propose "+px);
			return px;
	}

	public void notifierAchatAuxEncheres(Enchere propositionRetenue) {
		double stock = (this.stock.keySet().contains(propositionRetenue.getMiseAuxEncheres().getProduit())) ?this.stock.get(propositionRetenue.getMiseAuxEncheres().getProduit()) : 0.0;
		this.stock.put(propositionRetenue.getMiseAuxEncheres().getProduit(), stock+ propositionRetenue.getMiseAuxEncheres().getQuantiteT());
		this.prix.put(propositionRetenue.getMiseAuxEncheres().getVendeur(), propositionRetenue.getPrixTonne()-1000.0);
		journal.ajouter("   mon prix a ete accepte. Mon prix pour "+propositionRetenue.getMiseAuxEncheres().getVendeur()+" passe a "+(propositionRetenue.getPrixTonne()-1000.0));
	}

	public void notifierEnchereNonRetenue(Enchere propositionNonRetenue) {
		this.prix.put(propositionNonRetenue.getMiseAuxEncheres().getVendeur(), propositionNonRetenue.getPrixTonne()+100.);
		journal.ajouter("   mon prix a ete refuse. Mon prix pour "+propositionNonRetenue.getMiseAuxEncheres().getVendeur()+" passe a "+(propositionNonRetenue.getPrixTonne()+100.));
	}
}
*/