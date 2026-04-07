package abstraction.eqXRomu.acteurs;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.Feve;

public class TransformateurX extends TransformateurXAcheteurAppelDOffre {
	public void next() {
		super.next();
        int now = Filiere.LA_FILIERE.getEtape();
        if (now>0) {
            this.journal.ajouter("part de marche achat F_MQ EQX "+Filiere.LA_FILIERE.getPartMarcheAchatsFeves(Feve.F_MQ, this, now-1));
        }
    }
}
