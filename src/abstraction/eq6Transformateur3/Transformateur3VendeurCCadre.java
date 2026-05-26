package abstraction.eq6Transformateur3;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

import abstraction.eqXRomu.contratsCadres.Echeancier;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.contratsCadres.IAcheteurContratCadre;
import abstraction.eqXRomu.contratsCadres.IVendeurContratCadre;
import abstraction.eqXRomu.contratsCadres.SuperviseurVentesContratCadre;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IDistributeurChocolatDeMarque;

import abstraction.eqXRomu.general.Journal;

import abstraction.eqXRomu.produits.IProduit;

public class Transformateur3VendeurCCadre extends Transformateur3AcheteurCCadre implements IVendeurContratCadre {

    protected Journal journalCCVente;

    protected List<ExemplaireContratCadre> contratsVendus;

    public Transformateur3VendeurCCadre() {

        super();

        this.journalCCVente =
                new Journal("Journal Vendeur Contrat Cadre EQ6", this);

        this.contratsVendus =
                new LinkedList<ExemplaireContratCadre>();
    }

    /* =============================================================== */
    /*                         OUTILS STOCK                            */
    /* =============================================================== */

    public double stockEngage(IProduit produit) {

        double total = 0.0;

        for (ExemplaireContratCadre c : this.contratsVendus) {

            if (c.getProduit().equals(produit)) {

                total += c.getQuantiteRestantALivrer();
            }
        }

        return total;
    }

    public double stockDisponible(IProduit produit) {

        return this.getStockProduit(produit)
                - stockEngage(produit);
    }

    /* =============================================================== */
    /*                        PEUT-ON VENDRE ?                         */
    /* =============================================================== */

    public boolean vend(IProduit produit){

        if (produit.getType().equals("Feve")) {
            return false;
        }

        return stockDisponible(produit) > 500;
    }

    /* =============================================================== */
    /*                    NEGOCIATION ECHEANCIER                       */
    /* =============================================================== */

    public Echeancier contrePropositionDuVendeur(
            ExemplaireContratCadre contrat){

        double disponible =
                stockDisponible(contrat.getProduit());

        if (disponible < contrat.getQuantiteTotale()) {

            this.journalCCVente.ajouter(
                    "REFUS contrat "
                    + contrat.getNumero()
                    + " : stock insuffisant"
            );

            return null;
        }

        this.journalCCVente.ajouter(
                "ACCEPTATION echeancier contrat "
                + contrat.getNumero()
                + " pour "
                + contrat.getProduit()
        );

        return contrat.getEcheancier();
    }

    /* =============================================================== */
    /*                      NEGOCIATION PRIX                           */
    /* =============================================================== */

    public double propositionPrix(
            ExemplaireContratCadre contrat){

        IProduit produit = contrat.getProduit();

        double prix = 10000.0;

        if (produit.equals(LamborghiniduCacao)) {

            prix = 18000.0;

        } else if (produit.equals(Chocoenbien)) {

            prix = 12000.0;
        }

        this.journalCCVente.ajouter(
                "Proposition prix vendeur contrat "
                + contrat.getNumero()
                + " = "
                + prix
        );

        return prix;
    }

    public double contrePropositionPrixVendeur(
            ExemplaireContratCadre contrat){

        double prixAcheteur = contrat.getPrix();

        double prixMinimum = propositionPrix(contrat);

        if (prixAcheteur >= prixMinimum * 0.95) {

            this.journalCCVente.ajouter(
                    "ACCORD prix contrat "
                    + contrat.getNumero()
                    + " = "
                    + prixAcheteur
            );

            return prixAcheteur;

        } else {

            double contreProposition =
                    (prixAcheteur + prixMinimum) / 2.0;

            this.journalCCVente.ajouter(
                    "CONTRE-PROPOSITION prix contrat "
                    + contrat.getNumero()
                    + " = "
                    + contreProposition
            );

            return contreProposition;
        }
    }

    /* =============================================================== */
    /*                    SIGNATURE DU CONTRAT                         */
    /* =============================================================== */

    public void notificationNouveauContratCadre(
            ExemplaireContratCadre contrat){

        if (contratAvecDistributeur(contrat)) {

            this.journalCCVente.ajouter(
                    "CONTRAT SIGNE : "
                    + contrat.getProduit()
                    + " | quantite = "
                    + contrat.getQuantiteTotale()
                    + " | prix = "
                    + contrat.getPrix()
            );

            this.contratsVendus.add(contrat);
        }
    }

    /* =============================================================== */
    /*                           LIVRAISON                             */
    /* =============================================================== */

    public double livrer(
            IProduit produit,
            double quantite,
            ExemplaireContratCadre contrat){

        double disponible =
                this.getStockProduit(produit);

        if (disponible <= 0) {

            this.journalCCVente.ajouter(
                    "Livraison impossible contrat "
                    + contrat.getNumero()
                    + " : stock vide"
            );

            return 0.0;
        }

        double aLivrer =
                Math.min(disponible, quantite);

        this.setStockProduit(
                produit,
                disponible - aLivrer
        );

        this.journalCCVente.ajouter(
                "Livraison de "
                + aLivrer
                + " T de "
                + produit
                + " | contrat "
                + contrat.getNumero()
                + " | stock restant = "
                + this.getStockProduit(produit)
        );

        return aLivrer;
    }

    /* =============================================================== */
    /*                             NEXT                                */
    /* =============================================================== */

    public void next(){

        super.next();

        this.journalCCVente.ajouter(
                "===== ETAPE "
                + Filiere.LA_FILIERE.getEtape()
                + " ====="
        );

        SuperviseurVentesContratCadre sup =
                (SuperviseurVentesContratCadre)
                        (Filiere.LA_FILIERE.getActeur("Sup.CCadre"));

        List<IAcheteurContratCadre> acheteurs =
                sup.getAcheteurs(LamborghiniduCacao);

        /* ========================= */
        /*      LAMBORGHINI          */
        /* ========================= */

        double disponibleLambo =
                stockDisponible(LamborghiniduCacao);

        if (disponibleLambo > 1000
                && !acheteurs.isEmpty()) {

            IAcheteurContratCadre acheteur =
                    acheteurs.get(0);

            if (acheteur instanceof IDistributeurChocolatDeMarque) {

                double quantite =
                        disponibleLambo * 0.25;

                Echeancier e =
                        new Echeancier(
                                Filiere.LA_FILIERE.getEtape()+1,
                                4,
                                quantite/4
                        );

                this.journalCCVente.ajouter(
                        "Demande vendeur envoyee : "
                        + quantite
                        + " T de "
                        + LamborghiniduCacao
                        + " a "
                        + acheteur.getNom()
                );

                sup.demandeVendeur(
                        acheteur,
                        this,
                        LamborghiniduCacao,
                        e,
                        cryptogramme,
                        false
                );
            }
        }

        /* ========================= */
        /*        CHOCOENBIEN        */
        /* ========================= */

        double disponibleChoco =
                stockDisponible(Chocoenbien);

        if (disponibleChoco > 1000
                && !acheteurs.isEmpty()) {

            IAcheteurContratCadre acheteur =
                    acheteurs.get(0);

            if (acheteur instanceof IDistributeurChocolatDeMarque) {

                double quantite =
                        disponibleChoco * 0.25;

                Echeancier e =
                        new Echeancier(
                                Filiere.LA_FILIERE.getEtape()+1,
                                4,
                                quantite/4
                        );

                this.journalCCVente.ajouter(
                        "Demande vendeur envoyee : "
                        + quantite
                        + " T de "
                        + Chocoenbien
                        + " a "
                        + acheteur.getNom()
                );

                sup.demandeVendeur(
                        acheteur,
                        this,
                        Chocoenbien,
                        e,
                        cryptogramme,
                        false
                );
            }
        }

        /* ========================= */
        /*     ARCHIVAGE CONTRATS    */
        /* ========================= */

        List<ExemplaireContratCadre> termines =
                new LinkedList<ExemplaireContratCadre>();

        for (ExemplaireContratCadre c : this.contratsVendus) {

            if (c.getQuantiteRestantALivrer()==0.0
                    && c.getMontantRestantARegler()<=0.0) {

                termines.add(c);
            }
        }

        for (ExemplaireContratCadre c : termines) {

            this.journalCCVente.ajouter(
                    "Archivage contrat "
                    + c.getNumero()
            );

            this.contratsVendus.remove(c);
        }
    }

    /* =============================================================== */
    /*                           OUTILS                                */
    /* =============================================================== */

    private boolean contratAvecDistributeur(
            ExemplaireContratCadre contrat) {

        return contrat != null
                && contrat.getAcheteur()
                instanceof IDistributeurChocolatDeMarque;
    }

    public List<Journal> getJournaux() {

        List<Journal> res =
                new ArrayList<Journal>(super.getJournaux());

        res.add(this.journalCCVente);

        return res;
    }

    public double restantALivrer(IProduit p) {
        double res = 0;
        for (ExemplaireContratCadre c : this.contratsVendus) {
            if (c.getProduit().equals(p)) {
                res += c.getQuantiteRestantALivrer();
            }
        }
        return res;
    }
}