package abstraction.eq2Producteur2;

import abstraction.eqXRomu.contratsCadres.Echeancier;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.contratsCadres.IAcheteurContratCadre;
import abstraction.eqXRomu.contratsCadres.IVendeurContratCadre;
import abstraction.eqXRomu.contratsCadres.SuperviseurVentesContratCadre;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.IProduit;
import java.util.LinkedList;
import java.util.List;

/** @author Thomas */
public class Producteur2VendeurCC extends Producteur2Bourse implements IVendeurContratCadre {

    private SuperviseurVentesContratCadre supCC;
    private List<ExemplaireContratCadre> contratsEnCours;
    private static final double MARGE_MIN = 1.2;
    private static final double MARGE_EQUITABLE = 2.0; // Marge plus importante pour les fèves équitables

    public Producteur2VendeurCC() {
        super();
        this.contratsEnCours = new LinkedList<>();
    }

    @Override
    public void initialiser() {
        super.initialiser();
        this.supCC = (SuperviseurVentesContratCadre) Filiere.LA_FILIERE.getActeur("Sup.CCadre");
        this.journalContratCadre.ajouter("Producteur2VendeurCC initialisé");
    }

    @Override
    public void next() {
        super.next();
        this.journalContratCadre.ajouter("Step " + Filiere.LA_FILIERE.getEtape() + " : vérification CC");
        for (Feve f : stocks.keySet()) {
            double disponible = stocks.get(f).getValeur(this.cryptogramme) - restantDu(f);
            this.journalContratCadre.ajouter("Stock disponible " + f + " = " + disponible);
            double seuilCC = (f == Feve.F_HQ) ? 10.0 : 100.0;
            if (disponible > seuilCC) {
                double parStep = Math.max(100.0, Math.min(disponible / 12.0, 1000.0));
                Echeancier e = new Echeancier(Filiere.LA_FILIERE.getEtape() + 1, 12, parStep);
                List<IAcheteurContratCadre> acheteurs = supCC.getAcheteurs(f);
                if (!acheteurs.isEmpty()) {
                    for (IAcheteurContratCadre acheteur : acheteurs) {
                        if (disponible <= seuilCC)
                            break; // On arrête si on a plus assez de stock

                        ExemplaireContratCadre contrat = supCC.demandeVendeur(acheteur, this, f, e, cryptogramme,
                                false);
                        if (contrat != null) {
                            if (!this.contratsEnCours.contains(contrat)) {
                                this.contratsEnCours.add(contrat);
                            }
                            this.journalContratCadre.ajouter("Contrat signé avec " + acheteur.getNom() + " pour " + f
                                    + " = " + contrat.getQuantiteTotale());

                            disponible -= contrat.getQuantiteTotale();
                            parStep = Math.max(100.0, Math.min(disponible / 12.0, 1000.0));
                            if (parStep >= 100.0)
                                e = new Echeancier(Filiere.LA_FILIERE.getEtape() + 1, 12, parStep);
                        } else {
                            this.journalContratCadre
                                    .ajouter("Négociation échouée avec " + acheteur.getNom() + " pour " + f);
                        }
                    }
                } else {
                    this.journalContratCadre.ajouter("Pas d'acheteur pour " + f);
                }
            }
        }
    }

    public double restantDu(Feve f) {
        double res = 0;
        for (ExemplaireContratCadre c : this.contratsEnCours) {
            if (c.getProduit().equals(f)) {
                res += c.getQuantiteRestantALivrer();
            }
        }
        return res;
    }

    @Override
    public boolean vend(IProduit produit) {
        if (!(produit instanceof Feve)) {
            return false;
        }
        Feve f = (Feve) produit;
        double disponible = stocks.get(f).getValeur(this.cryptogramme) - restantDu(f);
        double seuilVente = (f == Feve.F_HQ) ? 100.0 : 1200.0;
        boolean peutVendre = disponible > seuilVente;
        this.journalContratCadre
                .ajouter("vend? " + f + " (disponible=" + disponible + ", seuil=" + seuilVente + ") -> " + peutVendre);
        return peutVendre;
    }

    @Override
    public Echeancier contrePropositionDuVendeur(ExemplaireContratCadre contrat) {
        if (!(contrat.getProduit() instanceof Feve)) {
            return null;
        }
        Feve f = (Feve) contrat.getProduit();
        double disponible = stocks.get(f).getValeur(this.cryptogramme) - restantDu(f);
        if (disponible < SuperviseurVentesContratCadre.QUANTITE_MIN_ECHEANCIER) {
            return null;
        }
        double quantiteDemandee = contrat.getEcheancier().getQuantiteTotale();
        if (quantiteDemandee <= disponible) {
            return contrat.getEcheancier();
        }
        double quantiteParStep = disponible / 12.0;
        if (quantiteParStep * 12.0 < SuperviseurVentesContratCadre.QUANTITE_MIN_ECHEANCIER) {
            return null;
        }
        return new Echeancier(Filiere.LA_FILIERE.getEtape() + 1, 12, quantiteParStep);
    }

    @Override
    public double propositionPrix(ExemplaireContratCadre contrat) {
        if (!(contrat.getProduit() instanceof Feve)) {
            return 0;
        }
        Feve feve = (Feve) contrat.getProduit();
        double prix = this.prixMinimumAcceptable(feve);
        this.journalContratCadre.ajouter("Proposition prix " + prix + " pour " + contrat.getProduit());
        return prix;
    }

    @Override
    public double contrePropositionPrixVendeur(ExemplaireContratCadre contrat) {
        if (!(contrat.getProduit() instanceof Feve)) {
            return 0;
        }
        Feve feve = (Feve) contrat.getProduit();
        double prixActuel = contrat.getPrix();
        double prixMinimum = this.prixMinimumAcceptable(feve);
        if (prixActuel < prixMinimum) {
            this.journalContratCadre
                    .ajouter("Refus prix CC " + prixActuel + " < seuil " + prixMinimum + " pour " + feve);
            return 0.0;
        }

        if (feve == Feve.F_HQ_E) {
            double prixPropose = Math.max(prixActuel, prixMinimum);
            this.journalContratCadre.ajouter("Prix équitable proposé: " + prixPropose + " € pour " + feve);
            return prixPropose;
        }

        return prixActuel;
    }

    @Override
    public void notificationNouveauContratCadre(ExemplaireContratCadre contrat) {
        if (!this.contratsEnCours.contains(contrat)) {
            this.contratsEnCours.add(contrat);
        }
    }

    private double prixMinimumAcceptable(Feve feve) {
        double cours = feve.isEquitable() ? 0.0
                : ((abstraction.eqXRomu.bourseCacao.BourseCacao) Filiere.LA_FILIERE.getActeur("BourseCacao"))
                        .getCours(feve).getValeur();
        double coutProd = this.cout_unit_t.getOrDefault(feve, 0.0);

        // Pour les fèves équitables (F_HQ_E), appliquer une meilleure marge
        if (feve == Feve.F_HQ_E) {
            double prixEquitable = coutProd * MARGE_EQUITABLE;
            return Math.max(prixEquitable, coutProd * 1.5);
        }

        return Math.max(cours, coutProd * 1.1);
    }

    @Override
    public double livrer(IProduit produit, double quantite, ExemplaireContratCadre contrat) {
        if (!(produit instanceof Feve)) {
            return 0.0;
        }
        Feve f = (Feve) produit;
        double livre = this.retirerDuStock(f, quantite);
        Variable stockFeve = stocks.get(f);
        Variable stockVarFeve = this.stockvar.get(f);
        if (stockFeve != null && stockVarFeve != null) {
            stockFeve.setValeur(this, stockVarFeve.getValeur());
        }
        this.journalContratCadre
                .ajouter("Livraison de " + livre + " t de " + f + " pour contrat " + contrat.getNumero());
        return livre;
    }
}