package abstraction.eq9Distributeur2.Achats;

import abstraction.eq9Distributeur2.Config.EQ9Config;
import abstraction.eq9Distributeur2.Stocks.EQ9_GestionStocks;
import abstraction.eq9Distributeur2.Stratégie.EQ9_StrategieFixationPrix;
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

        // Stock faible = livraisons rapides
        if (stockProjete < EQ9Config.SEUIL_MIN_T) {
            int debutSouhaite = stepCourant + 1;
            if (stepDebut > debutSouhaite) {
                Echeancier cp = new Echeancier(debutSouhaite, nbSteps, quantiteTotale / nbSteps);
                journalCC.ajouter("CC échéancier : stock faible → anticipation livraison à l'étape " + debutSouhaite);
                return cp;
            }
        }

        // Stock élevé = début plus tardif 
        if (stockProjete > EQ9Config.STOCK_CIBLE_T * 1.2) {
            int debutSouhaite = stepCourant + 3;
            if (stepDebut < debutSouhaite) {
                Echeancier cp = new Echeancier(debutSouhaite, nbSteps, quantiteTotale / nbSteps);
                journalCC.ajouter("CC échéancier : stock élevé → décalage livraison à l'étape " + debutSouhaite);
                return cp;
            }
        }

        // Ajustement de la durée
        // Trop court = étaler
        if (nbSteps < 3) {
            int nouvelleDuree = Math.min(6, 24 - stepDebut);
            Echeancier cp = new Echeancier(stepDebut, nouvelleDuree, quantiteTotale / nouvelleDuree);
            journalCC.ajouter("CC échéancier : durée trop courte → étalement sur " + nouvelleDuree + " étapes");
            return cp;
        }

        // Trop long = raccourcir
        if (nbSteps > 8) {
            int nouvelleDuree = Math.max(4, nbSteps - 2);
            Echeancier cp = new Echeancier(stepDebut, nouvelleDuree, quantiteTotale / nouvelleDuree);
            journalCC.ajouter("CC échéancier : durée trop longue → réduction à " + nouvelleDuree + " étapes");
            return cp;
        }

        // Tout est ok = acceptation 
        journalCC.ajouter("CC échéancier : accepté pour " + choco.getNom());
        return prop;
    }



    /**
     * @author Anass Ouisrani
     * @author Paul Juhel (pour correctifs)
     */
    @Override
    public double contrePropositionPrixAcheteur(ExemplaireContratCadre contrat) {
        double prixPropose = contrat.getPrix();

        ChocolatDeMarque choco = (ChocolatDeMarque) contrat.getProduit();

        int etape = Filiere.LA_FILIERE.getEtape();

        EQ9_StrategieFixationPrix strat = new EQ9_StrategieFixationPrix(this.journal);

        // Simulation d’un prix de vente réaliste
        double prixVenteEstime = strat.calculerPrixVente(
            prixPropose, // on suppose que c’est le coût d’achat
            choco.getNom(),
            this.stock.getOrDefault(choco, 0.0),
            20.0, // demande estimée simple
            prixPropose 
        );

        // marge minimale 
        double margeMin = 1.2; // 20%

        double prixMax = prixVenteEstime / margeMin;

        // Si le vendeur demande plus que notre maximum : on abandonne la négociation
        // -1.0 = stop dans le protocole
        if (prixPropose > prixMax) {
            this.journalCC.ajouter("Abandon négociation CC : prix " + prixPropose
                + "€/T trop élevé (max=" + prixMax + "€/T)");
            return -1.0;
        }

        // Situation financière
        double solde = getSolde();
        double quantiteTotale = contrat.getQuantiteTotale(); // en tonnes
        double coutTotalEstime = quantiteTotale * prixPropose;

        // Si on n'a pas les fonds, être plus ferme dans la négociation
        double margeSecurite = (solde < coutTotalEstime * 2) ? 1.1 : 1.05;

        // On consulte le prix moyen du marché à l'étape précédente
        // Si etape=0 on n'a pas de référence donc on met NaN (Not a Number = pas de valeur)
        double prixMoyen = (etape >= 1) ? Filiere.LA_FILIERE.prixMoyen(choco, etape - 1) : Double.NaN;

        // Si on a une référence marché valide
        if (!Double.isNaN(prixMoyen) && prixMoyen > 0) {

            double prixSeuilBas = Math.max(prixMax * 0.70, prixMoyen * 0.70);
            prixSeuilBas = Math.min(prixSeuilBas, prixMax);

            double prixCompromis = Math.max(prixPropose * 0.80, prixSeuilBas);
            prixCompromis = Math.min(prixCompromis, prixMax);

            if (prixPropose <= prixCompromis) {
                this.journalCC.ajouter("Acceptation CC : prix attractif " + prixPropose + "€/T (marché=" + prixMoyen + ")");
                return prixPropose;
            }

            List<Double> listePrix = contrat.getListePrix();
            if (listePrix.isEmpty()) {
                this.journalCC.ajouter("Abandon CC : liste des prix vide");
                return -1.0;
            }
            double prixInitial = listePrix.get(0);
            int tourNegociation = listePrix.size() / 2;
            double ratio = 0.70;
            for (int i = 0; i < tourNegociation && ratio < 0.80; i++) {
                ratio += 0.05;
            }
            if (ratio > 0.80) {
                ratio = 0.80;
            }

            double contreProposition = prixInitial * ratio;
            if (contreProposition < prixSeuilBas) {
                contreProposition = prixSeuilBas;
            }
            if (contreProposition > prixMax) {
                contreProposition = prixMax;
            }

            if (contreProposition >= prixPropose * 0.98) {
                this.journalCC.ajouter("Acceptation CC : " + prixPropose + "€/T pour " + choco.getNom());
                return prixPropose;
            }

            double coutContreProposition = quantiteTotale * contreProposition;
            if (solde < coutContreProposition * margeSecurite) {
                this.journalCC.ajouter("Abandon CC : fonds insuffisants pour " + coutContreProposition
                    + "€ (solde=" + solde + "€)");
                return -1.0;
            }

            this.journalCC.ajouter("Contre-proposition CC : " + contreProposition
                + "€/T (proposé=" + prixPropose + ", marché=" + prixMoyen + ", seuil bas=" + prixSeuilBas + ", compromis=" + prixCompromis + ")");
            return contreProposition;
        }

        // Pas de référence marché disponible
        double prixSeuilBas = Math.max(prixMax * 0.70, prixPropose * 0.70);
        double prixCompromis = Math.max(prixPropose * 0.80, prixSeuilBas);
        prixCompromis = Math.min(prixCompromis, prixMax);

        if (prixPropose <= prixCompromis) {
            this.journalCC.ajouter("Acceptation CC (pas de ref marché) : " + prixPropose + "€/T");
            return prixPropose;
        }

        List<Double> listePrix = contrat.getListePrix();
        if (listePrix.isEmpty()) {
            this.journalCC.ajouter("Abandon CC : liste des prix vide (pas de ref marché)");
            return -1.0;
        }
        double prixInitial = listePrix.get(0);
        int tourNegociation = listePrix.size() / 2;
        double ratio = 0.70;
        for (int i = 0; i < tourNegociation && ratio < 0.80; i++) {
            ratio += 0.05;
        }
        if (ratio > 0.80) {
            ratio = 0.80;
        }

        double contreProposition = prixInitial * ratio;
        if (contreProposition < prixSeuilBas) {
            contreProposition = prixSeuilBas;
        }
        if (contreProposition > prixMax) {
            contreProposition = prixMax;
        }

        double coutContreProposition = quantiteTotale * contreProposition;
        if (solde >= coutContreProposition * margeSecurite) {
            this.journalCC.ajouter("Contre-proposition CC (pas de ref) : " + contreProposition + "€/T");
            return contreProposition;
        } else {
            this.journalCC.ajouter("Abandon CC : fonds insuffisants (pas de ref marché)");
            return -1.0;
        }
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
     * Méthode pour initier des propositions de contrats cadres
     * @author Paul JUHEL
     */
    public void fairePropositionCC() {
        EQ9_GestionStocks gs = new EQ9_GestionStocks(this.stock, this::restantDu);

        List<ChocolatDeMarque> produitsFiliere = Filiere.LA_FILIERE.getChocolatsProduits();
        if (produitsFiliere == null || produitsFiliere.isEmpty()) {
            return;
        }

        for (ChocolatDeMarque choco : produitsFiliere) {
            if (!gs.doitAcheter(choco)) continue;
            if (!gs.prefererCC(choco)) continue; // AO gère le reste

            double quantiteCC = gs.quantiteAacheter(choco);
            if (quantiteCC < EQ9Config.CC_QUANTITE_MIN_T) continue;

            double stockActuel = this.stock.getOrDefault(choco, 0.0);
            double enCours = restantDu(choco);
            double seuilDeSecurite = EQ9Config.SEUIL_MIN_T;
            double stockProjete = stockActuel + enCours;

            if (stockProjete >= seuilDeSecurite) {
                continue;
            }

            double quantiteAcheter = Math.max(quantiteCC, EQ9Config.CC_QUANTITE_MIN_T);

            // Vérifier les fonds disponibles
            double prixEstime = getPrixMaxAcceptable(choco);
            double coutEstime = quantiteAcheter * prixEstime;
            if (getSolde() < coutEstime * 1.2) { // Marge de sécurité
                this.journalCC.ajouter("Fonds insuffisants pour CC " + choco.getNom()
                    + " : besoin " + coutEstime + "€, solde " + getSolde() + "€");
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
                int nbSteps = Math.min(6, 24 - stepDebut);
                if (nbSteps <= 0) continue;

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