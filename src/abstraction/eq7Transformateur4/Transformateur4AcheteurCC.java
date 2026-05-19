package abstraction.eq7Transformateur4;

import abstraction.eqXRomu.bourseCacao.BourseCacao;
import abstraction.eqXRomu.contratsCadres.Echeancier;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.contratsCadres.IAcheteurContratCadre;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.Gamme;
import abstraction.eqXRomu.produits.IProduit;

public class Transformateur4AcheteurCC extends Transformateur4AcheteurBourse implements IAcheteurContratCadre{

    public Transformateur4AcheteurCC(){
        super();

    }

    public boolean achete(IProduit produit) {
        if (produit== Feve.F_BQ || produit == Feve.F_BQ_E){
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public Echeancier contrePropositionDeLAcheteur(ExemplaireContratCadre contrat) {
        if (!contrat.getProduit().getType().equals("Feve")) {
            this.journal_CC_achat.ajouter("[REJET] Demande de contrat cadre pour du "+contrat.getProduit() + ". Rejeté");
			return null;
		}
        if (contrat.getQuantiteTotale()/(contrat.getEcheancier().getNbEcheances())>10000){
            if (contrat.getPrix()/contrat.getQuantiteTotale()>this.cout_prod.getValeur()*0.8){
                this.journal_CC_achat.ajouter("[REJET] Demande de contrat cadre de " + contrat.getVendeur() + " pour un prix de " + contrat.getPrix()+ " et une quantitée de " + contrat.getQuantiteTotale()+". Rejeté car prix unitaire trop élevé par rapport au coût de production");
                return null;
            }
            else{
                this.journal_CC_achat.ajouter("[NEGOCIATION DEBUT] Demande de contrat cadre de "+ contrat.getVendeur() + " pour un prix de " + contrat.getPrix()+ " et une quantitée de " + contrat.getQuantiteTotale()+ ". Accepté");
                return contrat.getEcheancier();
            }
        }
        else {
            return null;
        }
    }

    @Override
    public double contrePropositionPrixAcheteur(ExemplaireContratCadre contrat) {
        BourseCacao bourse = (BourseCacao)(Filiere.LA_FILIERE.getActeur("BourseCacao"));
        this.journal_CC_achat.ajouter("[NEGOCIATIONS]");
        this.journal_CC_achat.ajouter("[PROPOSITION VENDEUR] Proposition de " + contrat.getVendeur() + " pour un prix de "+ contrat.getPrix() + " et une quantitée totale de " + contrat.getQuantiteTotale());
        if (contrat.getPrix()>2*bourse.getCours(Feve.F_BQ).getValeur()){
            this.journal_CC_achat.ajouter("[PROPOSITION ACHETEUR] Proposition d'un prix de "+ contrat.getPrix()/(2*contrat.getQuantiteTotale()) + " la tonne" );
            return contrat.getPrix()/2;
        }
        if (contrat.getPrix()>bourse.getCours(Feve.F_BQ).getValeur()){
            this.journal_CC_achat.ajouter("[PROPOSITION ACHETEUR] Proposition d'un prix de "+ contrat.getPrix()*0.8/contrat.getQuantiteTotale() + " la tonne" );
            return contrat.getPrix()*0.8;
        }
        else{
             this.journal_CC_achat.ajouter("[PROPOSITION ACHETEUR] Proposition d'un prix de "+ contrat.getPrix() + " la tonne" );
            return contrat.getPrix();
        }
    }

    @Override
    public void notificationNouveauContratCadre(ExemplaireContratCadre contrat) {
        this.journal_CC_achat.ajouter("[CONTRAT ACCEPTE] Contrat avec "+ contrat.getVendeur() + " pour un prix de " + contrat.getPrix() + " et une quantitée de " + contrat.getQuantiteTotale());
    }

    @Override
    public void receptionner(IProduit p, double quantiteEnTonnes, ExemplaireContratCadre contrat) {
        this.get_Stock().add(quantiteEnTonnes,Gamme.BQ);
    }

    public void next(){
        super.next();
        
    }

}
