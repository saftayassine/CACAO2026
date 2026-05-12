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
        String marque = cdm.getMarque().toLowerCase();

        if (!marque.contains("ferrara")) {
            return null;
        }

        double stockDispo = this.getStock_chocolatDeMarque(cdm);
        if (stockDispo < offre.getQuantiteT()) {
            return null; 
        }

        double prixTonne;
        switch (cdm.getChocolat()) {
            case C_HQ: 
                prixTonne = 15000.0;
                break;
            case C_MQ: 
                prixTonne = 10000.0;
                break;
            case C_BQ: 
                prixTonne = 7000.0;
                break;
            default:   
                prixTonne = 5000.0;
                break;
        }

        return new OffreVente(offre, this, cdm, prixTonne);
    }

	public void notifierVenteAO(OffreVente propositionRetenue){
        ChocolatDeMarque cdm = (ChocolatDeMarque) propositionRetenue.getProduit();
        this.remove_chocolatDeMarque(cdm, propositionRetenue.getQuantiteT());
        this.getJournaux().get(8).ajouter("Retenue de l'offre: "+propositionRetenue.toString()+ "\n");
    }

	public void notifierPropositionNonRetenueAO(OffreVente propositionRefusee){
        this.getJournaux().get(8).ajouter("Refus de l'offre: "+propositionRefusee.toString()+ "\n");
    }
}