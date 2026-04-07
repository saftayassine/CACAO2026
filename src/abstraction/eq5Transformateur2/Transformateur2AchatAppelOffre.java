package abstraction.eq5Transformateur2;

import java.applet.Applet;
import java.awt.Color;
import java.util.List;

import abstraction.eqXRomu.appelDOffre.AppelDOffre;
import abstraction.eqXRomu.appelDOffre.IAcheteurAO;
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
public class Transformateur2AchatAppelOffre extends Transformateur2AcheteurBourse implements IAcheteurAO{

    public Transformateur2AchatAppelOffre() {
        super();
    }

	public OffreVente choisirOV(List<OffreVente> propositions){
		OffreVente mp = null;
		for (int i = 0; i < propositions.size(); i++){
			if (propositions.get(i).getProduit() == propositions.get(i).getOffre().getProduit()){
				if (propositions.get(i).getPrixT() <= propositions.get(i).getQuantiteT()*((BourseCacao) (Filiere.LA_FILIERE.getActeur("BourseCacao"))).getCours(Feve.F_MQ).getValeur()*0.97){
					if (propositions.get(i).compareTo(mp) == 1){
						mp = propositions.get(i);
					}
				}

			}
		}
		this.getJournaux().get(7).ajouter("Achat fève en AO" + (mp).toString() + "\n");
		return mp;
	}
}
    