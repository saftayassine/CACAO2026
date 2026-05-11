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
        
        if (!(p instanceof ChocolatDeMarque)) {
            return null;
        }
        
        ChocolatDeMarque cdm = (ChocolatDeMarque) p;
        
        if (!cdm.getMarque().equals("Ferrara Rocher")) {
            return null;
        }

        double stockDispo = this.getStock_chocolatDeMarque(cdm);
        if (stockDispo < offre.getQuantiteT()) {
            return null; 
        }

        double prixTonne;
        switch (cdm.getChocolat()) {
            case C_HQ: 
                prixTonne = 8000.0;
                break;
            case C_MQ: 
                prixTonne = 5000.0;
                break;
            case C_BQ: 
                prixTonne = 3000.0;
                break;
            default:   
                prixTonne = 2500.0;
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
        this.getJournaux().get(8).ajouter("Refus de l'offre: "+propositionRefusee.toString()+ "\n");
    }
}