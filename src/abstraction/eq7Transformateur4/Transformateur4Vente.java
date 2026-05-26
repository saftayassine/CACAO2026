package abstraction.eq7Transformateur4;

import abstraction.eqXRomu.contratsCadres.Echeancier;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.contratsCadres.IAcheteurContratCadre;
import abstraction.eqXRomu.contratsCadres.IVendeurContratCadre;
import abstraction.eqXRomu.contratsCadres.SuperviseurVentesContratCadre;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IActeur;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.IProduit;

//Auteur -> Paul
public class Transformateur4Vente extends Transformateur4Production implements IVendeurContratCadre {

    @Override
    public boolean vend(IProduit produit) {
        if (produit instanceof ChocolatDeMarque){
            ChocolatDeMarque chocolat = (ChocolatDeMarque)produit;
            if (chocolat.getChocolat()==Chocolat.C_BQ && chocolat.getMarque()=="CACAO+"){
                return true;

            }
            else{
                return false;
            }
        }
        else{
            return false;
        }
    }

    @Override
    public Echeancier contrePropositionDuVendeur(ExemplaireContratCadre contrat) {
        if (contrat.getProduit() instanceof ChocolatDeMarque){
        this.journal_negociation_CC.ajouter("[Échancier] Proposition de Contrat avec "+ contrat.getAcheteur() + " , négociation de l'échéancier " + contrat.getEcheancier());
        return contrat.getEcheancier(); }
        else {
            return null;
        }

    }

    @Override
    public double propositionPrix(ExemplaireContratCadre contrat) {
        if (contrat.getProduit() instanceof ChocolatDeMarque){
        this.journal_negociation_CC.ajouter("[Prix acheteur] Proposition de Contrat avec négociation du prix " + contrat.getPrix());
        if (Double.isNaN(contrat.getPrix())){
            this.journal_negociation_CC.ajouter("[Prix vendeur] Proposition de Contrat avec "+ contrat.getAcheteur() + ", négociation du prix " + this.cout_prod.getValeur()*3);
            return this.cout_prod.getValeur()*3;
        }
        else if (contrat.getPrix()<this.cout_prod.getValeur()){
            this.journal_negociation_CC.ajouter("[Prix vendeur] Proposition de Contrat avec "+ contrat.getAcheteur() + ", négociation du prix " + contrat.getPrix()*3);
            return contrat.getPrix()*2;}
        else {
            return 0.;}}
        else{
            return 0.;
        }
        
        }

    @Override
    public double contrePropositionPrixVendeur(ExemplaireContratCadre contrat) {
        if (contrat.getProduit() instanceof ChocolatDeMarque){
        this.journal_negociation_CC.ajouter("[Prix acheteur] Proposition de Contrat avec "+ contrat.getAcheteur() + ", négociation du prix " + contrat.getPrix());
        if (contrat.getPrix()<this.cout_prod.getValeur()){
            this.journal_negociation_CC.ajouter("[Prix vendeur] Proposition de Contrat avec "+ contrat.getAcheteur() + ", négociation du prix " + contrat.getPrix()*2);
            return contrat.getPrix()*2;}
        else {
            return 0.;}
        }
        else {
            return 0.;
        }
        
        
    }



    @Override
    public double livrer(IProduit produit, double quantite, ExemplaireContratCadre contrat) {
        double alivrer = Math.min(quantite, this.get_StockChoco_BQ().getValeur());
        this.get_StockChoco_BQ().ajouter(this,-alivrer);
        this.journal_vente_CC.ajouter("Vente de " + alivrer + " tonnes de Chocolat à " + contrat.getAcheteur());
        return alivrer;
    }
    
    public void next(){
        super.next();
        SuperviseurVentesContratCadre suppcc ;
        suppcc = (SuperviseurVentesContratCadre)Filiere.LA_FILIERE.getActeur("Sup.CCadre");
        double quantiteParTour = 10000;
        ChocolatDeMarque chocolat = new ChocolatDeMarque(Chocolat.C_BQ, "CACAO+", 45);
        Echeancier echeancier = new Echeancier(Filiere.LA_FILIERE.getEtape() + 1, 5, quantiteParTour);
        for (IActeur acteur : suppcc.getAcheteurs(chocolat)) {
			if (acteur!=this && (acteur instanceof IAcheteurContratCadre)) {
                this.journal_vente_CC.ajouter("[DEMANDE] Demande de CC à " + acteur + " pour une quantité par tour de " + quantiteParTour);
        
				suppcc.demandeVendeur((IAcheteurContratCadre)acteur,this,chocolat, echeancier, cryptogramme, false);

			}
        }
    }

}