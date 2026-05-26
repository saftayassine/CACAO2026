package abstraction.eq9Distributeur2.Achats;

import abstraction.eq9Distributeur2.Config.EQ9Config;
import abstraction.eq9Distributeur2.Stocks.EQ9_GestionStocks;
import abstraction.eqXRomu.contratsCadres.Echeancier;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.contratsCadres.IAcheteurContratCadre;
import abstraction.eqXRomu.contratsCadres.IVendeurContratCadre;
import abstraction.eqXRomu.contratsCadres.SuperviseurVentesContratCadre;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.IProduit;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Paul Juhel
 */

public class Distributeur2AcheteurCC extends Distributeur2AcheteurAO implements IAcheteurContratCadre {

    // Superviseur des contrats cadres
    private SuperviseurVentesContratCadre superviseurCC;

    // Liste des contrats en cours
    protected List<ExemplaireContratCadre> contratsEnCours;

    // Liste des contrats terminés
    protected List<ExemplaireContratCadre> contratsTermines;

    public Distributeur2AcheteurCC() {
        super();
        this.contratsEnCours = new LinkedList<>();
        this.contratsTermines = new LinkedList<>();
    }

    @Override
    public void initialiser() {
        super.initialiser();
        this.superviseurCC = (SuperviseurVentesContratCadre) Filiere.LA_FILIERE.getActeur("Sup.CCadre");
        this.journalCC.ajouter("Initialisation des CC");
    }

    @Override
    public void next() {
        int etape = Filiere.LA_FILIERE.getEtape();
        this.journalCC.ajouter("=== ETAPE " + etape + " ===");

        List<ExemplaireContratCadre> aRetirer = new java.util.ArrayList<>();
        for (ExemplaireContratCadre contrat : this.contratsEnCours) {
            if (contrat.getQuantiteRestantALivrer() == 0.0) {
                aRetirer.add(contrat);
                this.contratsTermines.add(contrat);
            }
        }
        this.contratsEnCours.removeAll(aRetirer);

        List<ChocolatDeMarque> produits = Filiere.LA_FILIERE.getChocolatsProduits();

        if (produits != null && !produits.isEmpty()) {
            // Frais de stockage : 120 €/T par étape (16x le coût producteur de 7.5€/T)
            payerFraisStockage();
            ajusterPrixDynamiques();
            fairePropositionCC();
        }

        this.indicateurStockTotal.setValeur(this, getStockTotal());
        journalStocks.ajouter("Stock total : " + (getStockTotal()) + " tonnes");
    }


    /**
     * frais de stockage pour cette étape
     */
    protected void payerFraisStockage() {
        double stockTotalT = getStockTotal();
        double fraisStockage = stockTotalT * EQ9Config.FRAIS_STOCKAGE_EUR_PAR_T;
        if (fraisStockage > 0) {
            Filiere.LA_FILIERE.getBanque().payerCout(this, this.cryptogramme, "Frais de stockage", fraisStockage);
        }
        this.journalFinancier.ajouter("Frais de stockage : " + fraisStockage + "€ pour " + stockTotalT + "t");
    }

    /**
     * Ajuste les prix de vente de manière dynamique
     */
    @Override
    protected void ajusterPrixDynamiques() {
        int etape = Filiere.LA_FILIERE.getEtape();
        if (etape < 1) return;

        for (ChocolatDeMarque choco : Filiere.LA_FILIERE.getChocolatsProduits()) {

            double coutAchat = obtenirCoutAchat(choco);
            double stockT = this.stock.getOrDefault(choco, 0.0);
            double dos = this.indicateurDOS.getValeur();
            double demande = estimerDemandeClients(choco);
            double prixConcurrent = estimerPrixConcurrent(choco);
            double partMarche = this.indicateurPartMarche.getValeur();
            double cash = getSolde();

            double prixFinal = this.pricingService.calculerPrix(
                choco, coutAchat, stockT, dos, demande, prixConcurrent, partMarche, cash
            );

            this.prix.put(choco, prixFinal);

            this.journal.ajouter("Prix EQ9 " + choco.getNom() + " = " + prixFinal
                + "€/T (coût=" + coutAchat + ", DOS=" + dos + ", marché=" + prixConcurrent + ")");
        }
    }
    

    //         IMPLEMENTATION DE L'INTERFACE IAcheteurContratCadre

    
    @Override
    public double getQuantiteEnStock(IProduit p, int etape) {
        return super.getQuantiteEnStock(p, this.cryptogramme);
    }

    @Override
    public boolean achete(IProduit produit) {
        if (produit instanceof ChocolatDeMarque) {
            ChocolatDeMarque choco = (ChocolatDeMarque) produit;
            if (this.prix.containsKey(choco)) {
                this.journalCC.ajouter("Prêt à négocier un contrat cadre pour " + choco.getNom());
                return true;
            } else {
                this.journalCC.ajouter("Refus CC : pas de stratégie de prix pour " + choco.getNom());
                return false;
            }
        }
        return false;
    }

   /**
     * @author Paul Juhel
     */
    @Override
    public Echeancier contrePropositionDeLAcheteur(ExemplaireContratCadre contrat) {

        Echeancier prop = contrat.getEcheancier();
        ChocolatDeMarque choco = (ChocolatDeMarque) contrat.getProduit();

        int stepCourant = Filiere.LA_FILIERE.getEtape();
        int stepDebut = prop.getStepDebut();
        int nbSteps = prop.getNbEcheances();
        double quantiteTotale = prop.getQuantiteTotale();

        double stock = this.stock.getOrDefault(choco, 0.0);
        double enCours = restantDu(choco);
        double stockProjete = stock + enCours;

        // Stock faible : livraisons plus tôt 
        if (stockProjete < EQ9Config.SEUIL_MIN_T) {
            int debutSouhaite = stepCourant + 1;
            if (stepDebut > debutSouhaite) {
                Echeancier cp = new Echeancier(debutSouhaite, nbSteps, quantiteTotale / nbSteps);
                journalCC.ajouter("CC échéancier : stock faible → anticipation à l'étape " + debutSouhaite);
                return cp;
            }
        }

        //  Stock élevé : livraisons plus tard 
        if (stockProjete > EQ9Config.STOCK_CIBLE_T * 1.2) {
            int debutSouhaite = stepCourant + 3;
            if (stepDebut < debutSouhaite) {
                Echeancier cp = new Echeancier(debutSouhaite, nbSteps, quantiteTotale / nbSteps);
                journalCC.ajouter("CC échéancier : stock élevé → décalage à l'étape " + debutSouhaite);
                return cp;
            }
        }
        // Tout est OK :
        journalCC.ajouter("CC échéancier : accepté pour " + choco.getNom());
        return prop;
    }





    /**
     * @author Anass Ouisrani V1
     * @author Paul Juhel V2
     */
   @Override
    public double contrePropositionPrixAcheteur(ExemplaireContratCadre contrat) {

        ChocolatDeMarque choco = (ChocolatDeMarque) contrat.getProduit();
        double prixPropose = contrat.getPrix();
        double prixMax = getPrixMaxAcceptable(choco);
        double solde = getSolde();
        double quantiteTotale = contrat.getQuantiteTotale();

        List<Double> historique = contrat.getListePrix();
        int tours = historique.size();

        // ABANDON si prix trop élevé et on a déjà négocié 3 tours sans accord
        if (prixPropose > prixMax && tours >= 3) {
            journalCC.ajouter("CC : abandon après 3 tours (prix=" + prixPropose 
                + " > max=" + prixMax + ")");
            return -1.0;
        }

        // PRIX ACCEPTABLE
        if (prixPropose <= prixMax) {
            switch (tours) {
                case 0:
                    // Tour 1 : on tente -10%
                    double offre1 = prixPropose * 0.90;
                    // Sécurité : jamais plus cher que le vendeur
                    if (offre1 >= prixPropose) return prixPropose;
                    journalCC.ajouter("CC tour 1 : offre -10% → " + offre1);
                    return offre1;

                case 1:
                    // Tour 2 : on remonte un peu, -5%
                    double offre2 = prixPropose * 0.95;
                    if (offre2 >= prixPropose) return prixPropose;
                    journalCC.ajouter("CC tour 2 : offre -5% → " + offre2);
                    return offre2;

                case 2:
                    // Tour 3 : on remonte encore, -2%
                    double offre3 = prixPropose * 0.98;
                    if (offre3 >= prixPropose) return prixPropose;
                    journalCC.ajouter("CC tour 3 : offre -2% → " + offre3);
                    return offre3;

                default:
                    // Tour 4+ : on accepte
                    journalCC.ajouter("CC : acceptation finale (" + prixPropose + ")");
                    return prixPropose;
            }
        }

        // PRIX TROP HAUT : on négocie agressivement
        double ratio = 0.70 + 0.05 * tours;
        if (ratio > 0.90) ratio = 0.90;

        double contreProp = prixPropose * ratio;
        if (contreProp > prixMax) contreProp = prixMax;

        // FIX : vérification fonds en tenant compte des engagements déjà contractés
        double coutTotal = contreProp * quantiteTotale;
        double soldeDisponible = solde - getTotalEngagementsFinanciersFuturs();
        if (soldeDisponible < coutTotal) {
            journalCC.ajouter("CC : abandon → fonds insuffisants (disponible=" 
                + soldeDisponible + "€, besoin=" + coutTotal + "€)");
            return -1.0;
        }

        journalCC.ajouter("CC : contre-proposition tour " + tours 
            + " → " + contreProp
            + " (proposé=" + prixPropose + ", max=" + prixMax + ")");

        return contreProp;
    }



    @Override
    public void notificationNouveauContratCadre(ExemplaireContratCadre contrat) {
        this.contratsEnCours.add(contrat);

        this.journalCC.ajouter("Nouveau contrat cadre signé pour " + ((ChocolatDeMarque)contrat.getProduit()).getNom() +
                           " : " + contrat.getQuantiteTotale() + "t à " + contrat.getPrix() + "€/t");
    }

    @Override
    public void receptionner(IProduit produit, double quantiteEnTonnes, ExemplaireContratCadre contrat) {
        // Ajouter au stock
        double stockActuel = this.stock.getOrDefault(produit, 0.0);
        this.stock.put(produit, stockActuel + quantiteEnTonnes);

        // Mettre à jour l'indicateur de stock total
        this.indicateurStockTotal.setValeur(this, getStockTotal());

        this.journalStocks.ajouter("Livraison reçue : " + quantiteEnTonnes + "t de " +
                           produit + " (contrat cadre)");
    }


    //         MÉTHODES UTILITAIRES

    
    public double restantDu(IProduit produit) {
        double res = 0.0;
        for (ExemplaireContratCadre contrat : this.contratsEnCours) {
            if (contrat.getProduit().equals(produit)) {
                res += contrat.getQuantiteRestantALivrer();
            }
        }
        return res;
    }


    public List<ExemplaireContratCadre> getContratsEnCours() {
        return new LinkedList<>(this.contratsEnCours);
    }


    public List<ExemplaireContratCadre> getContratsTermines() {
        return new LinkedList<>(this.contratsTermines);
    }

    /**
     * Calcule le total des paiements futurs engagés sur tous les contrats en cours.
     * Utilisé pour évaluer la solvabilité réelle avant tout nouvel engagement.
     */
    private double getTotalEngagementsFinanciersFuturs() {
        double total = 0.0;
        for (ExemplaireContratCadre contrat : this.contratsEnCours) {
            total += contrat.getQuantiteRestantALivrer() * contrat.getPrix();
        }
        return total;
    }

    /**
     * Vérifie si un CC est déjà en cours pour ce produit.
     * Évite de signer plusieurs contrats cumulatifs pour le même chocolat.
     */
    private boolean aDejaContratEnCours(ChocolatDeMarque choco) {
        for (ExemplaireContratCadre contrat : this.contratsEnCours) {
            if (contrat.getProduit().equals(choco)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Méthode pour initier des propositions de contrats cadres
     * @author Paul JUHEL
     * CORRECTIF : quantité bornée au manque réel, vérif financière sur engagements totaux,
     *             un seul CC actif par produit à la fois.
     */
    public void fairePropositionCC() {
        EQ9_GestionStocks gs = new EQ9_GestionStocks(this.stock, this::restantDu);

        List<ChocolatDeMarque> produitsFiliere = Filiere.LA_FILIERE.getChocolatsProduits();
        if (produitsFiliere == null || produitsFiliere.isEmpty()) {
            return;
        }

        for (ChocolatDeMarque choco : produitsFiliere) {
            if (!gs.doitAcheter(choco)) continue;
            if (!gs.prefererCC(choco)) continue;

            double stockActuel = this.stock.getOrDefault(choco, 0.0);
            double enCours = restantDu(choco);
            double stockProjete = stockActuel + enCours;

            // FIX 1 : ne pas commander si le stock projeté (actuel + en cours de livraison)
            // couvre déjà l'objectif. Évite l'accumulation explosive de contrats.
            if (stockProjete >= EQ9Config.SEUIL_MIN_T) {
                journalCC.ajouter("CC ignoré pour " + choco.getNom()
                    + " : stock projeté " + stockProjete + "t >= seuil " + EQ9Config.SEUIL_MIN_T + "t");
                continue;
            }

            // FIX 2 : un seul contrat actif par produit à la fois.
            // Sans cette garde, chaque appel à next() empile un nouveau CC sur les précédents.
            if (aDejaContratEnCours(choco)) {
                journalCC.ajouter("CC ignoré pour " + choco.getNom()
                    + " : un contrat est déjà en cours de livraison");
                continue;
            }

            // FIX 3 : la quantité à commander = UNIQUEMENT ce qui manque pour atteindre STOCK_CIBLE,
            // en tenant compte du stock projeté. Jamais plus que STOCK_CIBLE.
            double manque = EQ9Config.STOCK_CIBLE_T - stockProjete;
            if (manque <= 0) continue;
            double quantiteAcheter = Math.min(
                Math.max(manque, EQ9Config.CC_QUANTITE_MIN_T),
                EQ9Config.STOCK_CIBLE_T   // plafond absolu : jamais plus d'un STOCK_CIBLE par contrat
            );

            double prixEstime = getPrixMaxAcceptable(choco);
            double coutEstime = quantiteAcheter * prixEstime;

            // FIX 4 : vérification financière réaliste = solde - engagements déjà contractés.
            // L'ancien check ignorait les paiements futurs des CC déjà signés.
            double soldeDisponible = getSolde() - getTotalEngagementsFinanciersFuturs();
            if (soldeDisponible < coutEstime * 1.2) {
                this.journalCC.ajouter("Fonds insuffisants pour CC " + choco.getNom()
                    + " : besoin " + (coutEstime * 1.2) + "€, solde disponible (engagements déduits) "
                    + soldeDisponible + "€");
                continue;
            }

            List<IVendeurContratCadre> vendeurs = this.superviseurCC.getVendeurs(choco);
            if (vendeurs.isEmpty()) {
                this.journalCC.ajouter("Aucun vendeur disponible pour " + choco.getNom());
                continue;
            }

            boolean propositionReussie = false;
            for (IVendeurContratCadre vendeur : vendeurs) {

                int stepDebut = Filiere.LA_FILIERE.getEtape() + 1;
                int nbSteps = 6;
                double quantiteParStep = quantiteAcheter / nbSteps;

                Echeancier echeancierPropose = new Echeancier(stepDebut, nbSteps, quantiteParStep);

                ExemplaireContratCadre contrat = this.superviseurCC.demandeAcheteur(
                    this, vendeur, choco, echeancierPropose, this.cryptogramme, false);

                if (contrat != null) {
                    this.journalCC.ajouter("Proposition CC initiée pour " + quantiteAcheter
                        + "t de " + choco.getNom() + " chez " + vendeur.getNom());
                    propositionReussie = true;
                    break;
                } else {
                    this.journalCC.ajouter("Proposition CC rejetée par " + vendeur.getNom()
                        + " pour " + choco.getNom());
                }
            }

            if (!propositionReussie) {
                this.journalCC.ajouter("Échec de toutes les propositions CC pour " + choco.getNom());
            }
        }
    }



    /**
     * Méthode utilitaire qui définit le prix maximum qu'on accepte
     * Ces valeurs sont nos prix d'achat maximum pour garder une marge rentable
     * @author Paul JUHEL
     */
    private double getPrixMaxAcceptable(ChocolatDeMarque choco) {
        double prixVentePrevu = prix(choco);
        double margeMin = EQ9Config.MARGE_BRUTE_MIN;

        double prixMax = prixVentePrevu / (1.0 + margeMin);

        // Ajustements agressifs
        if (this.indicateurPartMarche.getValeur() < EQ9Config.PART_MARCHE_CIBLE * 100) {
            prixMax *= 1.05; // on accepte 5% plus cher pour gagner du volume
        }

        if (getSolde() > EQ9Config.CASH_BUFFER_MIN * 2) {
            prixMax *= 1.03; // on utilise notre puissance financière
        }

        double stock = this.stock.getOrDefault(choco, 0.0);
        if (stock < EQ9Config.SEUIL_MIN_T) {
            prixMax *= 1.10; // on paye plus cher pour éviter la rupture
        }
        double limiteSurvie = prixVentePrevu / 1.05;
        if (prixMax > limiteSurvie) {
            prixMax = limiteSurvie;
        }

        return prixMax;
    }



    @Override
    public Filiere getFiliere(String nom) {
        return Filiere.LA_FILIERE;
    }

    @Override
    public java.util.List<String> getNomsFilieresProposees() {
        return new java.util.ArrayList<>();
    }
}