package abstraction.eq5Transformateur2;
import java.util.List;

import abstraction.eqXRomu.appelDOffre.IAcheteurAO;
import abstraction.eqXRomu.appelDOffre.OffreVente;
import abstraction.eqXRomu.filiere.Filiere;
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
	if (propositions == null || propositions.isEmpty()) {
            return null;
        }

        OffreVente meilleureOffre = null;
        
        double coursMQ = ((BourseCacao) (Filiere.LA_FILIERE.getActeur("BourseCacao"))).getCours(Feve.F_MQ).getValeur();
        double seuilPaiement = (this.getStock_feve(Feve.F_MQ) < 1000) ? 1.05 : 0.98;

        for (OffreVente ov : propositions) {
            
            if (ov.getPrixT() <= ov.getQuantiteT() * coursMQ * seuilPaiement) {

                
                if (meilleureOffre == null || ov.getPrixT() < meilleureOffre.getPrixT()) {
                    meilleureOffre = ov; // On la retient
                }
            }
        }
        
        if (meilleureOffre != null) {
            this.getJournaux().get(7).ajouter("Achat fève en AO : " + meilleureOffre.toString() + "\n");
        }
        
        return meilleureOffre;
    }
}
    