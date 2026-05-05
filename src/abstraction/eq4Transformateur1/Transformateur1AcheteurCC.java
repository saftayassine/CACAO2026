package abstraction.eq4Transformateur1;
import java.util.List;

import abstraction.eqXRomu.contratsCadres.Echeancier;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.contratsCadres.IAcheteurContratCadre;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.produits.*;

public class Transformateur1AcheteurCC extends Transformateur1AcheteurEnchere implements IAcheteurContratCadre {
    
    protected Journal JournalAchatCC;

public Transformateur1AcheteurCC() {
		super();
		this.JournalAchatCC = new Journal(this.getNom()+"Journal Achat CC", this);

	}
    
    public boolean achete(IProduit produit){
        if (produit instanceof Feve && this.getStocksPrevuProduit(this.getChoco(produit))<50000 && produit!=Feve.F_MQ_E){
            return true;
        }
        else{
            return false;
        }
    }


    public Echeancier contrePropositionDeLAcheteur(ExemplaireContratCadre contrat){
        return contrat.getEcheancier();
    }


    public double contrePropositionPrixAcheteur(ExemplaireContratCadre contrat){
        double prix= contrat.getPrix();
        if (prix<10000){
        return contrat.getPrix();}
        else{
            return 10000.0;
        }
    }


    public void notificationNouveauContratCadre(ExemplaireContratCadre contrat){
        super.notificationNouveauContratCadre(contrat);
        if (contrat.getAcheteur().equals(this)){
        double quantite= contrat.getQuantiteTotale();
        this.JournalAchatCC.ajouter("Nouveau contrat cadre de "+quantite+"T de " +contrat.getProduit()+" pour "+contrat.getPrix()+"€");
        }
    }

	public void receptionner(IProduit p, double quantiteEnTonnes, ExemplaireContratCadre contrat){
        this.setStocksProduit(p, this.getStocksProduit(p)+quantiteEnTonnes);
    }

    public List<Journal> getJournaux() {
		List<Journal> jx=super.getJournaux();
		jx.add(this.JournalAchatCC);
		return jx;
    }
}
