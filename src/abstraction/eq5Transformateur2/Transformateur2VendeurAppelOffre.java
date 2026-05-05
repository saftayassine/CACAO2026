package abstraction.eq5Transformateur2;

import abstraction.eqXRomu.appelDOffre.AppelDOffre;
import abstraction.eqXRomu.appelDOffre.IVendeurAO;
import abstraction.eqXRomu.appelDOffre.OffreVente;
import abstraction.eqXRomu.produits.IProduit;
import abstraction.eqXRomu.produits.ChocolatDeMarque;

/**
 * @author Pierre GUTTIEREZ
 */
public class Transformateur2VendeurAppelOffre extends Transformateur2AchatAppelOffre implements IVendeurAO{

    public Transformateur2VendeurAppelOffre() {
        super();
    }

	public OffreVente proposerVente(AppelDOffre offre) {
        IProduit p = offre.getProduit();
        
        // 1. On refuse si ce n'est pas un chocolat de marque
        if (!(p instanceof ChocolatDeMarque)) {
            return null;
        }
        
        ChocolatDeMarque cdm = (ChocolatDeMarque) p;
        
        // 2. Choix stratégique : On ne vend que NOTRE marque Ferrara Rocher
        if (!cdm.getMarque().equals("Ferrara Rocher")) {
            return null;
        }

        // 3. On vérifie nos stocks !
        double stockDispo = this.getStock_chocolatDeMarque(cdm);
        if (stockDispo < offre.getQuantiteT()) {
            return null; // Pas assez de stock
        }

        // 4. On calcule un prix unitaire COHÉRENT avec la gamme demandée
        // Vous pouvez ajuster ces prix pour être plus ou moins agressif face aux autres équipes
        double prixTonne;
        switch (cdm.getChocolat()) {
            case C_HQ: 
                prixTonne = 10000.0; // Le HQ se vend cher
                break;
            case C_MQ: 
                prixTonne = 7500.0; // Prix standard pour du MQ
                break;
            case C_BQ: 
                prixTonne = 5000.0; // Prix bas pour du BQ pour s'assurer de remporter l'offre
                break;
            default:   
                prixTonne = 3000.0;
                break;
        }


        return new OffreVente(offre, this, cdm, prixTonne);
    }

	public void notifierVenteAO(OffreVente propositionRetenue){
        ChocolatDeMarque cdm = (ChocolatDeMarque) propositionRetenue.getProduit();
        this.remove_chocolatDeMarque(cdm, propositionRetenue.getQuantiteT());
        this.getJournaux().get(8).ajouter(propositionRetenue.toString()+ "\n");
    }

	public void notifierPropositionNonRetenueAO(OffreVente propositionRefusee){
        this.getJournaux().get(8).ajouter("Refus" + propositionRefusee.toString()+ "\n");
    }
}