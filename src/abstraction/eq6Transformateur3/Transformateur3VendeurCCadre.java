package abstraction.eq6Transformateur3;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import abstraction.eqXRomu.contratsCadres.Echeancier;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.contratsCadres.IAcheteurContratCadre;
import abstraction.eqXRomu.contratsCadres.IVendeurContratCadre;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IDistributeurChocolatDeMarque;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.IProduit;
import abstraction.eqXRomu.contratsCadres.SuperviseurVentesContratCadre;

/** @author : Brevael Le Clezio */
public class Transformateur3VendeurCCadre extends Transformateur3AcheteurCCadre implements IVendeurContratCadre {

    protected Journal journalCCVente;
    protected List<ExemplaireContratCadre> contratsVendus;

    public Transformateur3VendeurCCadre() {
        super();
        this.journalCCVente = new Journal(" Journal Vendeur Contrat Cadre EQ6", this);
        this.contratsVendus = new LinkedList<ExemplaireContratCadre>();
    }
    
    
    public boolean vend(IProduit produit){
        boolean vend = this.getStockProduit(produit)>200 && !produit.getType().equals("Feve");
        return vend;
    }

    public Echeancier contrePropositionDuVendeur(ExemplaireContratCadre contrat){
        Echeancier e = contrat.getEcheancier();
        if (contratAvecDistributeur(contrat)) {
            this.journalCCVente.ajouter("Contre-proposition d'\u00e9ch\u00e9ancier pour " + contrat.getProduit() + " : " + e);
        }
        return e;
    }

    public double propositionPrix(ExemplaireContratCadre contrat){
        double prix = 9000;
        if (contratAvecDistributeur(contrat)) {
            this.journalCCVente.ajouter("Proposition prix vendeur pour contrat " + contrat.getNumero() + " = " + prix);
        }
        return prix;
    }

    public double contrePropositionPrixVendeur(ExemplaireContratCadre contrat){
        double prix = contrat.getPrix();
        if (contratAvecDistributeur(contrat)) {
            this.journalCCVente.ajouter("Contre-proposition prix vendeur pour contrat " + contrat.getNumero() + " = " + prix);
        }
        return prix;
    }

    public void notificationNouveauContratCadre(ExemplaireContratCadre contrat){
        if (contratAvecDistributeur(contrat)) {
            this.journalCCVente.ajouter("Notification nouveau contrat cadre : " + contrat);
            this.contratsVendus.add(contrat);
        }
    }


    public double livrer(IProduit produit, double quantite, ExemplaireContratCadre contrat){
        double disponible = this.getStockProduit(produit);
        if (!contratAvecDistributeur(contrat)) {
            if (disponible <= 0) {
                return 0;
            } else if (disponible >= quantite) {
                this.setStockProduit(produit, disponible - quantite);
                return quantite;
            } else {
                double aLivrer = disponible;
                this.setStockProduit(produit, 0);
                return aLivrer;
            }
        }
        if (disponible <= 0) {
            this.journalCCVente.ajouter("Livraison impossible pour " + produit + " (contrat " + contrat.getNumero() + ") : stock epuis\u00e9");
            return 0;
        } else if (disponible >= quantite) {
            this.setStockProduit(produit, disponible - quantite);
            this.journalCCVente.ajouter("Livraison de " + quantite + " T de " + produit + " pour contrat " + contrat.getNumero() + " (stock restant=" + (disponible - quantite) + ")");
            return quantite;
        } else {
            double aLivrer = disponible;
            this.setStockProduit(produit, 0);
            this.journalCCVente.ajouter("Livraison partielle de " + aLivrer + " T de " + produit + " pour contrat " + contrat.getNumero() + " (stock insuffisant)");
            return aLivrer;
        }
    }

    /** @author : Pol Bailleul */
    public void next(){
        super.next();
        this.journalCCVente.ajouter("Etape"+Filiere.LA_FILIERE.getEtape());
        SuperviseurVentesContratCadre sup = null;
        sup = (SuperviseurVentesContratCadre)(Filiere.LA_FILIERE.getActeur("Sup.CCadre"));
        List<IAcheteurContratCadre> acheteurs = sup.getAcheteurs(LamborghiniduCacao);
        if (this.getStockProduit(LamborghiniduCacao) > 200 && !acheteurs.isEmpty()) {
            IAcheteurContratCadre acheteur = acheteurs.get(0);
            if (acheteur instanceof IDistributeurChocolatDeMarque) {
                Echeancier e = new Echeancier(Filiere.LA_FILIERE.getEtape()+1, 2, this.getStockProduit(LamborghiniduCacao) / 2);
                this.journalCCVente.ajouter("Envoi d'une demande vendeur pour " + LamborghiniduCacao + " \u00e0 " + acheteur.getNom());
                sup.demandeVendeur(acheteur, this, LamborghiniduCacao, e, cryptogramme, false);
            }
        }

        if (this.getStockProduit(Chocoenbien) > 200 && !acheteurs.isEmpty()) {
            IAcheteurContratCadre acheteur = acheteurs.get(0);
            if (acheteur instanceof IDistributeurChocolatDeMarque) {
                Echeancier e = new Echeancier(Filiere.LA_FILIERE.getEtape()+1, 2, this.getStockProduit(Chocoenbien) / 2);
                this.journalCCVente.ajouter("Envoi d'une demande vendeur pour " + Chocoenbien + " \u00e0 " + acheteur.getNom());
                sup.demandeVendeur(acheteur, this, Chocoenbien, e, cryptogramme, false);
            }
        }
        
        List<ExemplaireContratCadre> termines = new LinkedList<ExemplaireContratCadre>();
        for (ExemplaireContratCadre c : this.contratsVendus) {
            if (c.getQuantiteRestantALivrer()==0.0 && c.getMontantRestantARegler()<=0.0) {
                termines.add(c);
            }
        }
        for (ExemplaireContratCadre c : termines) {
            this.journalCCVente.ajouter("Archivage du contrat " + c);
            this.contratsVendus.remove(c);
        }

    }

    private boolean contratAvecDistributeur(ExemplaireContratCadre contrat) {
        return contrat != null && contrat.getAcheteur() instanceof IDistributeurChocolatDeMarque;
    }

    public List<Journal> getJournaux() {
        List<Journal> res = new ArrayList<Journal>(super.getJournaux());
        res.add(this.journalCCVente);
        return res;
    }

}