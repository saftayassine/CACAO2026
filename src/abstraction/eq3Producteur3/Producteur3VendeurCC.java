package abstraction.eq3Producteur3;

import java.util.LinkedList;
import java.util.List;
import abstraction.eqXRomu.acteurs.ProducteurXVendeurBourse;
import abstraction.eqXRomu.contratsCadres.Echeancier;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.contratsCadres.IAcheteurContratCadre;
import abstraction.eqXRomu.contratsCadres.IVendeurContratCadre;
import abstraction.eqXRomu.contratsCadres.SuperviseurVentesContratCadre;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.Gamme;
import abstraction.eqXRomu.produits.IProduit;


/** @author Victor Vannier-Moreau */
public class Producteur3VendeurCC extends Producteur3VendeurBourse implements IVendeurContratCadre {

    protected List<ExemplaireContratCadre> contratsEnCours;
    protected Journal journalCC;

    public Producteur3VendeurCC() {
        super();
        this.contratsEnCours = new LinkedList<ExemplaireContratCadre>();
        this.journalCC = new Journal("Journal Ventes CC EQ3", this);
    }

    public List<Journal> getJournaux() {
        List<Journal> jx = super.getJournaux();
        jx.add(this.journalCC);
        return jx;
    }

    public void next() {
        List<ExemplaireContratCadre> termines = new LinkedList<ExemplaireContratCadre>();
        for (ExemplaireContratCadre c : contratsEnCours) {
            if (c.getQuantiteRestantALivrer() == 0.0 && c.getMontantRestantARegler() == 0.0) {
                termines.add(c);
            }
        }
        contratsEnCours.removeAll(termines);

        super.next();

        SuperviseurVentesContratCadre supCC = (SuperviseurVentesContratCadre) Filiere.LA_FILIERE.getActeur("Sup.CCadre");
        List<Feve> mesFeves = new LinkedList<Feve>();
        mesFeves.add(Feve.F_MQ);
        mesFeves.add(Feve.F_HQ);
        mesFeves.add(Feve.F_MQ_E);
        mesFeves.add(Feve.F_HQ_E);

        for (Feve f : mesFeves) {
            List<IAcheteurContratCadre> acheteurs = supCC.getAcheteurs(f);
            double stockReel = this.stock.getStock(f);

            // Calculer tout ce qu'on a déjà promis de livrer dans le futur pour cette fève
            double resteALivrerTotal = 0;
            for (ExemplaireContratCadre c : contratsEnCours) {
                if (c.getProduit().equals(f)) {
                    resteALivrerTotal += c.getQuantiteRestantALivrer();
                }
            }

            double surplusReel = stockReel - resteALivrerTotal;

            if (surplusReel > 200.0) {
                double quantiteTotaleVoulue = surplusReel * 0.3;
                double quantiteParStep = quantiteTotaleVoulue / 12;

                if (quantiteParStep >= 100.0) {
                    for (IAcheteurContratCadre acheteur : acheteurs) {

                        //On ne re-propose pas si un contrat actif existe déjà
                        boolean dejaContrat = false;
                        for (ExemplaireContratCadre c : contratsEnCours) {
                            if (c.getProduit().equals(f) && c.getAcheteur().equals(acheteur)) {
                                dejaContrat = true;
                                break;
                            }
                        }
                        if (dejaContrat) continue;

                        //un Echeancier distinct par acheteur pour éviter les mutations croisées
                        Echeancier echPourCetAcheteur = new Echeancier(
                        Filiere.LA_FILIERE.getEtape() + 1, 12, quantiteParStep);

                        ExemplaireContratCadre contrat = supCC.demandeVendeur(acheteur, this, f, echPourCetAcheteur, cryptogramme, false);
                        if (contrat != null) {
                            this.contratsEnCours.add(contrat);
                            this.journalCC.ajouter("Contrat signé (initié par nous) avec "
                                    + acheteur.getNom() + " pour " + f
                                    + " | Total: " + contrat.getEcheancier().getQuantiteTotale() + "t"
                                    + " | Prix: " + contrat.getPrix() + "€/t");
                        } else {
                            this.journalCC.ajouter("Échec négociation avec "
                                    + acheteur.getNom() + " pour " + f);
                        }
                    }
                }
            }
        }

        this.journalCC.ajouter("--- Contrats en cours (step " + Filiere.LA_FILIERE.getEtape() + ") ---");
        for (ExemplaireContratCadre cc : this.contratsEnCours) {
            this.journalCC.ajouter(
            "Contrat #" + cc.getNumero()
            + " | " + cc.getProduit()
            + " | " + cc.getAcheteur().getNom()
            + " | signé step " + cc.getEcheancier().getStepDebut()
            + " | " + cc.getQuantiteRestantALivrer() + "t restantes / " + cc.getEcheancier().getQuantiteTotale() + "t total"
            + " | prix " + cc.getPrix() + "€/t"
        );
        }
        this.journalCC.ajouter("--------------------------------------");
    }

    // On ne vend que les gammes MQ et HQ par contrat cadre
    public boolean vend(IProduit produit) {
        if (produit instanceof Feve) {
            Feve f = (Feve) produit;
            if (f.getGamme() == Gamme.MQ) {
                return true;
            }
            if (f.getGamme() == Gamme.HQ) {
                return true;
            }
        }
        return false;
    }

    public Echeancier contrePropositionDuVendeur(ExemplaireContratCadre contrat) {
        Feve f = (Feve) contrat.getProduit();
        double stockTotalReel = this.stock.getStock(f);

        double totalDejaPromis = 0;
        for (ExemplaireContratCadre c : contratsEnCours) {
            if (c.getProduit().equals(f)) {
                totalDejaPromis += c.getQuantiteRestantALivrer();
            }
        }

        double disponible = stockTotalReel - totalDejaPromis;
        double demandeAcheteur = contrat.getEcheancier().getQuantiteTotale();

        if (disponible >= demandeAcheteur) {
            return contrat.getEcheancier();
        } else {
            if (disponible > 0) {
                Echeancier e = contrat.getEcheancier();
                e.set(e.getStepDebut(), disponible);
                return e;
            } else {
                return null;
            }
        }
    }

    public double livrer(IProduit produit, double quantite, ExemplaireContratCadre contrat) {
        Feve f = (Feve) produit;
        String nomAcheteur = contrat.getAcheteur().getNom();

        boolean contratConnu = contratsEnCours.contains(contrat);
        if (!contratConnu) {
            // si on reçoit une livraison pour un contrat qu'on ne connaît pas
            // (initié par l'acheteur, notificationNouveauContratCadre non appelée),
            // on l'enregistre maintenant
            this.contratsEnCours.add(contrat);
            this.journalCC.ajouter("Contrat #" + contrat.getNumero()
                    + " découvert à la livraison (initié par " + nomAcheteur + ") — enregistré");
        }

        double stockTotal = this.stock.getStock(f);
        double aLivre = Math.min(quantite, stockTotal);

        this.stock.retireStock(f, aLivre);
        this.mettreAJourIndicateurStock();
        this.journalCC.ajouter("période " + Filiere.LA_FILIERE.getEtape()
                + " : Livraison de " + aLivre + " tonnes de " + f
                + " à " + nomAcheteur
                + " (contrat #" + contrat.getNumero() + ")");
        return aLivre;
    }

    public double calculerPrixVente(Feve f) {
        double coutFeve = this.gestionCouts.getCoutFeve(f, this);
        double productionFeve = this.plantationeq3.getProductionFeve(f);

        if (productionFeve <= 0) {
            return 2000.0;
        }

        double coutParTonne = coutFeve / productionFeve;
        return coutParTonne * 2;
    }

    public double propositionPrix(ExemplaireContratCadre contrat) {
        return calculerPrixVente((Feve) contrat.getProduit());
    }

    public double contrePropositionPrixVendeur(ExemplaireContratCadre contrat) {
        Feve f = (Feve) contrat.getProduit();

        double coutFeve = this.gestionCouts.getCoutFeve(f, this);
        double productionFeve = this.plantationeq3.getProductionFeve(f);

        if (productionFeve <= 0) {
            return contrat.getPrix();
        }

        double prixPlancher = (coutFeve / productionFeve) * 1.10;
        double prixAcheteur = contrat.getPrix();

        if (prixAcheteur >= prixPlancher) {
            return prixAcheteur;
        } else {
            double prixInitial = this.propositionPrix(contrat);
            double contreProposition = (prixInitial + prixPlancher) / 2.0;
            return Math.max(contreProposition, prixPlancher);
        }
    }

    public void notificationNouveauContratCadre(ExemplaireContratCadre contrat) {
        if (!contratsEnCours.contains(contrat)) {
            this.contratsEnCours.add(contrat);
        }

        String client = contrat.getAcheteur().getNom();
        Echeancier ech = contrat.getEcheancier();

        String info = "Nouveau contrat (initié par acheteur) " + contrat.getProduit() + " avec " + client;
        info += " | Durée: " + ech.getNbEcheances() + " steps";
        info += " | Fin: " + ech.getStepFin();
        info += " | Total: " + ech.getQuantiteTotale() + "t";
        info += " | Prix: " + contrat.getPrix() + "€/t";

        this.journalCC.ajouter(info);
    }
}