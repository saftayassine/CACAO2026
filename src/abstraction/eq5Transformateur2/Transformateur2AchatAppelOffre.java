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

        // 1. Frein d'urgence global
        if (this.getStock_feve_total() > 650000.0) {
            return null;
        }

        OffreVente meilleureOffre = null;
        double meilleurPrix = Double.MAX_VALUE;

        for (OffreVente ov : propositions) {
            if (!(ov.getProduit() instanceof Feve)) continue;
            Feve f = (Feve) ov.getProduit();

            // 2. Est-ce qu'on a besoin de ces fèves ? (Stock actuel + offre < Cible)
            double cible = (f == Feve.F_HQ) ? 98000.0 : (f == Feve.F_MQ) ? 154000.0 : (f == Feve.F_BQ) ? 238000.0 : 0.0;
            if (this.getStock_feve(f) + ov.getQuantiteT() > cible) {
                continue; // On n'achète pas, on a déjà ce qu'il faut !
            }

            // 3. Est-ce une bonne affaire ? (On compare avec le cours actuel de la Bourse)
            double coursBourse = ((BourseCacao) Filiere.LA_FILIERE.getActeur("BourseCacao")).getCours(f).getValeur();

            // Si l'offre propose au moins 5% de réduction par rapport au marché spot
            if (ov.getPrixT() <= coursBourse * 0.95) { 
                if (ov.getPrixT() < meilleurPrix) {
                    meilleurPrix = ov.getPrixT();
                    meilleureOffre = ov;
                }
            }
        }
        
        return meilleureOffre;
    }
}
    