package abstraction.eq8Distributeur1; // Vérifie bien ton nom de package

import java.util.List;
import abstraction.eqXRomu.appelDOffre.IAcheteurAO;
import abstraction.eqXRomu.appelDOffre.OffreVente;

/**
 * Classe gérant les achats par Appel d'Offre.
 * Hérite de ContratCadre2 qui elle-même hérite d'Approvisionnement2.
 */
public class AppelOffre extends ContratCadre2 implements IAcheteurAO {
    
    public AppelOffre() {
        super();
    }

    @Override
    public OffreVente choisirOV(List<OffreVente> propositions) {
        // Strict minimum pour compiler : on ne choisit rien pour l'instant.
        // La logique de sélection (prix le plus bas, etc.) sera ajoutée ensuite.
        if (propositions == null || propositions.isEmpty()) {
            return null;
        }
        return null;
    }
}