package abstraction.eq7Transformateur4;

import abstraction.eqXRomu.contratsCadres.Echeancier;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.contratsCadres.IVendeurContratCadre;
import abstraction.eqXRomu.produits.IProduit;

//Auteur -> Paul
public class Transformateur4Vente extends Transformateur4Production implements IVendeurContratCadre {

    @Override
    public boolean vend(IProduit produit) {
        return this.getChocolatsProduits().contains(produit);
    }

    @Override
    public Echeancier contrePropositionDuVendeur(ExemplaireContratCadre contrat) {
        this.journal_negociation_CC.ajouter("[Échancier] Proposition de Contrat avec "+ contrat.getAcheteur() + " , négociation de l'échéancier " + contrat.getEcheancier());
        return contrat.getEcheancier();//echeance;
    }

    @Override
    public double propositionPrix(ExemplaireContratCadre contrat) {
        this.journal_negociation_CC.ajouter("[Prix]Proposition de Contrat avec "+ contrat.getAcheteur() + ", négociation du prix " + contrat.getPrix());
        return 6500.;}

    @Override
    public double contrePropositionPrixVendeur(ExemplaireContratCadre contrat) {
        this.journal_negociation_CC.ajouter("[Prix]Proposition de Contrat avec "+ contrat.getAcheteur() + ", négociation du prix " + contrat.getPrix());
        return 6000.;
    }

    @Override
    public void notificationNouveauContratCadre(ExemplaireContratCadre contrat) {
        this.journal_vente_CC.ajouter("Contrat accepté avec " + contrat.getAcheteur() + " pour " + contrat.getQuantiteTotale() + " tonnes de " + contrat.getProduit() + " à " + contrat.getPrix() + " € la tonne");
        this.journal_negociation_CC.ajouter("Contrat accepté avec " + contrat.getAcheteur() + " pour " + contrat.getQuantiteTotale() + " tonnes de " + contrat.getProduit() + " à " + contrat.getPrix() + " € la tonne");
    
    }

    @Override
    public double livrer(IProduit produit, double quantite, ExemplaireContratCadre contrat) {
        double alivrer = Math.min(quantite, this.get_StockChoco_BQ().getValeur());
        this.get_StockChoco_BQ().ajouter(this,-alivrer);
        this.journal_vente_CC.ajouter("Vente de " + alivrer + " tonnes de Chocolat à " + contrat.getAcheteur());
        return alivrer;
    }
    
}