package abstraction.eq7Transformateur4;

import abstraction.eqXRomu.appelDOffre.AppelDOffre;
import abstraction.eqXRomu.appelDOffre.IVendeurAO;
import abstraction.eqXRomu.appelDOffre.OffreVente;
import abstraction.eqXRomu.produits.IProduit;
import abstraction.eqXRomu.produits.ChocolatDeMarque;


//Auteur : Paul

public class Transformateur4VendeurAppelDOffre extends Transformateur4VendeurAuxEncheres implements IVendeurAO{

    public Transformateur4VendeurAppelDOffre() {
        super();
    }

	public OffreVente proposerVente(AppelDOffre offre) {
        IProduit p = offre.getProduit();
        
        if (!(p instanceof ChocolatDeMarque)) {
            return null;
        }
        
        ChocolatDeMarque cdm = (ChocolatDeMarque) p;
        String marque = cdm.getMarque().toLowerCase();

        if (!marque.contains("cacao+")) {
            return null;
        }

        double stockDispo = this.get_StockChoco_BQ().getValeur()+this.get_StockChoco_MQ().getValeur()+this.get_StockChoco_HQ().getValeur();
        if (stockDispo < offre.getQuantiteT()) {
            return null; 
        }

        double prixTonne;
        switch (cdm.getChocolat()) {
            case C_HQ: 
                prixTonne = 9000.0;
                this.journal_vente_AO.ajouter("Proposition de vente de "+offre.getQuantiteT()+" T de "+cdm+" à "+prixTonne+" €/T par "+offre.getAcheteur().getNom());
                break;
            case C_MQ: 
                prixTonne = 8000.0;
                this.journal_vente_AO.ajouter("Proposition de vente de "+offre.getQuantiteT()+" T de "+cdm+" à "+prixTonne+" €/T par "+offre.getAcheteur().getNom());
                break;
            case C_BQ: 
                prixTonne = 6000.0;
                this.journal_vente_AO.ajouter("Proposition de vente de "+offre.getQuantiteT()+" T de "+cdm+" à "+prixTonne+" €/T par "+offre.getAcheteur().getNom());
                break;
            default:   
                prixTonne = 5000.0;
                this.journal_vente_AO.ajouter("Proposition de vente de "+offre.getQuantiteT()+" T de "+cdm+" à "+prixTonne+" €/T par "+offre.getAcheteur().getNom());}
    
			return new OffreVente(offre, this, cdm, prixTonne);
	
			}

	public void notifierVenteAO(OffreVente propositionRetenue){
        this.get_StockChoco_BQ().retirer(this, propositionRetenue.getQuantiteT());
        this.journal_vente_AO.ajouter("[OFFRE RETENUE] Retenue de l'offre: "+propositionRetenue.toString()+ "\n");
    }

	public void notifierPropositionNonRetenueAO(OffreVente propositionRefusee){
        this.journal_vente_AO.ajouter("[OFFRE REFUSEE] Refus de l'offre: "+propositionRefusee.toString()+ "\n");
    }
}