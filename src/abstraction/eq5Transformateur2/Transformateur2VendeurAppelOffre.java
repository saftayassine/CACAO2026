package abstraction.eq5Transformateur2;

import java.awt.Color;
import java.util.List;

import abstraction.eqXRomu.appelDOffre.AppelDOffre;
import abstraction.eqXRomu.appelDOffre.IVendeurAO;
import abstraction.eqXRomu.appelDOffre.OffreVente;
import abstraction.eqXRomu.appelDOffre.SuperviseurVentesAO;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IActeur;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.produits.IProduit;
import abstraction.eqXRomu.produits.Chocolat; 
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.bourseCacao.BourseCacao;

/**
 * @author Pierre GUTTIEREZ
 */
public class Transformateur2VendeurAppelOffre extends Transformateur2AcheteurBourse implements IVendeurAO{

    public Transformateur2VendeurAppelOffre() {
        super();
    }

	public OffreVente proposerVente(AppelDOffre offre){
        if (offre.getQuantiteT() <= getStock_chocolat(offre.getProduit())){
            OffreVente OV = new OffreVente(offre, this, offre.getProduit(),((BourseCacao) (Filiere.LA_FILIERE.getActeur("BourseCacao"))).getCours(Feve.F_MQ).getValeur()*1.2*offre.getQuantiteT());
            return OV;
        } else {
            return null;
        }
    }

	
	/**
	 * Methode appelee lorsque la proposition de prix du vendeur a ete retenue 
	 * par l'acheteur. La transaction d'argent a deja ete effectuee mais il reste 
	 * au vendeur a mettre a jour ses stock pour tenir compte de l'achat qu'il
	 * vient de faire ( propositionRetenue.getOffre() ).
	 * @param propositionRetenue la proposition qu'a fait l'acheteur this et qui
	 *  vient d'etre retenue par le vendeur.
	 */
	public void notifierVenteAO(OffreVente propositionRetenue){
        this.getJournaux().get(6).ajouter(propositionRetenue.toString()+ "\n");
    }

	/**
	 * Methode appelee pour avertir le vendeur que sa proposition de vente n'a pas ete retenue 
	 * @param propositionRefusee, la proposition qui avait ete faite mais qui n'a pas ete retenue
	 */
	public void notifierPropositionNonRetenueAO(OffreVente propositionRefusee){
        this.getJournaux().get(6).ajouter(propositionRefusee.toString()+ "\n");
    }
}
    
