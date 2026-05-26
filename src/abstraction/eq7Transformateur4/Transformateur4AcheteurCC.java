package abstraction.eq7Transformateur4;

import abstraction.eqXRomu.bourseCacao.BourseCacao;
import abstraction.eqXRomu.contratsCadres.Echeancier;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.contratsCadres.IAcheteurContratCadre;
import abstraction.eqXRomu.contratsCadres.IVendeurContratCadre;
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
        double quantiteTotale = contrat.getQuantiteTotale();
        int nbEcheances = contrat.getEcheancier().getNbEcheances();
        if (quantiteTotale <= 0 || nbEcheances <= 0) {
            this.journal_CC_achat.ajouter("[REJET] Contrat cadre invalide : quantite ou échéancier invalide");
            return null;
        }
        double stockChoco = this.get_StockChoco_BQ().getValeur() + this.get_StockChoco_MQ().getValeur() + this.get_StockChoco_HQ().getValeur();
        double seuilStockChoco = 500000.0;
        if (stockChoco >= seuilStockChoco) {
            this.journal_CC_achat.ajouter("[REJET] Stock de chocolat trop élevé (" + String.format("%.0f", stockChoco) + "t), pas besoin d'acheter des fèves");
            return null;
        }
        double quantiteParTour = quantiteTotale / nbEcheances;
        double prixUnitaire = contrat.getPrix();
        double solde = this.getSolde();
        double budgetMaxTotal = Math.max(0.0, solde * 0.25);
        double budgetMaxUnitaire = quantiteTotale > 0 ? budgetMaxTotal / quantiteTotale : 0.0;
        BourseCacao bourse = (BourseCacao)(Filiere.LA_FILIERE.getActeur("BourseCacao"));
        double prixMarche = bourse.getCours(Feve.F_BQ).getValeur();

        if (prixUnitaire > budgetMaxUnitaire) {
            this.journal_CC_achat.ajouter("[REJET] Prix unitaire trop élevé pour notre trésorerie : " + String.format("%.2f", prixUnitaire) + " > " + String.format("%.2f", budgetMaxUnitaire));
            return null;
        }

        if (prixUnitaire > prixMarche * 1.2 || prixUnitaire > this.cout_prod.getValeur() * 1.2) {
            this.journal_CC_achat.ajouter("[REJET] Prix unitaire trop élevé : " + String.format("%.2f", prixUnitaire) + " €/t (marche=" + String.format("%.2f", prixMarche) + ")");
            return null;
        }

        if (quantiteParTour > 10000) {
            int nbStepsProposes = (int) Math.ceil(quantiteTotale / 10000.0);
            nbStepsProposes = Math.max(nbStepsProposes, nbEcheances);
            double quantiteParStep = quantiteTotale / nbStepsProposes;
            Echeancier proposition = new Echeancier(contrat.getEcheancier().getStepDebut(), nbStepsProposes, quantiteParStep);
            this.journal_CC_achat.ajouter("[NEGOCIATION DEBUT] Contrat de " + quantiteTotale + "t : échéancier trop serré (" + String.format("%.0f", quantiteParTour) + "t/step). Proposition : " + proposition);
            return proposition;
        }

        this.journal_CC_achat.ajouter("[ACCEPTATION ECHEANCIER] Contrat de " + quantiteTotale + "t accepté avec échéancier " + contrat.getEcheancier());
        return contrat.getEcheancier();
    }

    @Override
    public double contrePropositionPrixAcheteur(ExemplaireContratCadre contrat) {
        BourseCacao bourse = (BourseCacao)(Filiere.LA_FILIERE.getActeur("BourseCacao"));
        double quantiteTotale = contrat.getQuantiteTotale();
        if (quantiteTotale <= 0) {
            this.journal_CC_achat.ajouter("[REJET] Contrat cadre invalide : quantité totale nulle ou négative");
            return 0.;
        }
        double prixUnitaire = contrat.getPrix();
        double solde = getSolde();
        double budgetMaxTotal = Math.max(0.0, solde * 0.25);
        double prixBourse = bourse.getCours(Feve.F_BQ).getValeur();
        double budgetMaxUnitaire = budgetMaxTotal / quantiteTotale;

        this.journal_CC_achat.ajouter("[NEGOCIATIONS]");
        this.journal_CC_achat.ajouter("[PROPOSITION VENDEUR] Proposition de " + contrat.getVendeur() + " pour un prix unitaire de " + String.format("%.2f", prixUnitaire) + " €/t et une quantitée totale de " + quantiteTotale);
        this.journal_CC_achat.ajouter("[COUR BOURSE] " + String.format("%.2f", prixBourse));

        if (prixUnitaire > budgetMaxUnitaire) {
            this.journal_CC_achat.ajouter("[REJET] Prix unitaire non-finançable : " + prixUnitaire + " > budget unitaire " + String.format("%.2f", budgetMaxUnitaire));
            return 0.;
        }

        if (prixUnitaire > prixBourse * 1.3) {
            this.journal_CC_achat.ajouter("[REJET] Prix unitaire trop élevé pour négociation : "+ prixUnitaire + " €/t");
            return 0.;
        }

        if (prixUnitaire > prixBourse * 1.1) {
            double contrePrix = Math.min(prixUnitaire * 0.8, prixBourse * 1.05);
            this.journal_CC_achat.ajouter("[PROPOSITION ACHETEUR] Prix élevé (" + prixUnitaire + " €/t). Proposition : " + String.format("%.2f", contrePrix) + " €/t");
            return contrePrix;
        }

        this.journal_CC_achat.ajouter("[PROPOSITION ACHETEUR] Prix accepté : " + prixUnitaire+ " €/t");
        return prixUnitaire;
    }

    public void notificationNouveauContratCadre(ExemplaireContratCadre contrat) {
        if (contrat.getVendeur().equals((IVendeurContratCadre)this)){
            this.journal_vente_CC.ajouter("[CONTRAT ACCEPTE] Contrat accepté avec " + contrat.getAcheteur() +" de numero de contrat " + contrat.getNumero() + " pour " + contrat.getQuantiteTotale() + " tonnes de " + contrat.getProduit() + " à " + contrat.getPrix() + " € la tonne");
            this.journal_negociation_CC.ajouter("[CONTRAT ACCEPTE] Contrat accepté avec " + contrat.getAcheteur() + " pour " + contrat.getQuantiteTotale() + " tonnes de " + contrat.getProduit() + " à " + contrat.getPrix() + " € la tonne"); }
        else {
            this.journal_CC_achat.ajouter("[CONTRAT ACCEPTE] Contrat avec "+ contrat.getVendeur() + " pour un prix de " + contrat.getPrix() + " et une quantitée de " + contrat.getQuantiteTotale());
        }
    }

    @Override
    public void receptionner(IProduit p, double quantiteEnTonnes, ExemplaireContratCadre contrat) {
        this.get_Stock().add(quantiteEnTonnes,Gamme.BQ);
    }

    public void next(){
        super.next();
        
    }

}
