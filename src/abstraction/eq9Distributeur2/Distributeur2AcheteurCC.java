package abstraction.eq9Distributeur2;

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

        List<ChocolatDeMarque> produits = Filiere.LA_FILIERE.getChocolatsProduits();

        if (produits != null && !produits.isEmpty()) {
            // Frais de stockage : 120 €/T par étape (16x le coût producteur de 7.5€/T)
            payerFraisStockage();
            // --- V2 : Ajustement dynamique des prix de vente ---
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
        double fraisStockage = stockTotalT * 120.0; // 120€/t
        if (fraisStockage > 0) {
            Filiere.LA_FILIERE.getBanque().payerCout(this, this.cryptogramme, "Frais de stockage", fraisStockage);
        }
        this.journalFinancier.ajouter("Frais de stockage : " + fraisStockage + "€ pour " + stockTotalT + "t");
    }

    /**
     * Ajuste les prix de vente de manière dynamique
     */
    protected void ajusterPrixDynamiques() {
        // mettre en place en V2
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


    @Override
    public Echeancier contrePropositionDeLAcheteur(ExemplaireContratCadre contrat) {
        Echeancier propositionVendeur = contrat.getEcheancier();

        // Analyser l'échéancier proposé
        int stepDebut = propositionVendeur.getStepDebut();
        int nbSteps = propositionVendeur.getNbEcheances();
        double quantiteTotale = propositionVendeur.getQuantiteTotale();

        // livraison échelonnée sur plusieurs étapes
        int stepCourant = Filiere.LA_FILIERE.getEtape();

        // Ajustement
        int debutOptimal = stepCourant + 2; // Décalage de 2 étapes
        if (stepDebut < debutOptimal) {
            int decalage = debutOptimal - stepDebut;
            Echeancier contreProposition = new Echeancier(debutOptimal, nbSteps, quantiteTotale / nbSteps);
            this.journalCC.ajouter("Contre-proposition échéancier : décalage à l'étape " + debutOptimal
                + " (était " + stepDebut + ")");
            return contreProposition;
        } else if (stepDebut > debutOptimal + 3) {
            int nouveauDebut = Math.max(stepCourant + 1, stepDebut - 2);
            Echeancier contreProposition = new Echeancier(nouveauDebut, nbSteps, quantiteTotale / nbSteps);
            this.journalCC.ajouter("Contre-proposition échéancier : anticipation à l'étape " + nouveauDebut
                + " (était " + stepDebut + ")");
            return contreProposition;
        }

        // Ajustement
        if (nbSteps < 3) {
            // Étaler sur plus d'étapes
            int nouvelleDuree = Math.min(6, 24 - stepDebut);
            Echeancier contreProposition = new Echeancier(stepDebut, nouvelleDuree, quantiteTotale / nouvelleDuree);
            this.journalCC.ajouter("Contre-proposition échéancier : étalement sur " + nouvelleDuree
                + " étapes (était " + nbSteps + ")");
            return contreProposition;
        } else if (nbSteps > 8) {
            // Raccourcir
            int nouvelleDuree = Math.max(3, nbSteps - 2);
            Echeancier contreProposition = new Echeancier(stepDebut, nouvelleDuree, quantiteTotale / nouvelleDuree);
            this.journalCC.ajouter("Contre-proposition échéancier : réduction à " + nouvelleDuree
                + " étapes (était " + nbSteps + ")");
            return contreProposition;
        }

        // Accepter l'échéancier
        this.journalCC.ajouter("Acceptation de l'échéancier pour " + ((ChocolatDeMarque)contrat.getProduit()).getNom());
        return propositionVendeur;
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

        double quantiteEnKg = quantiteEnTonnes;

        // Ajouter au stock
        double stockActuel = this.stock.getOrDefault(produit, 0.0);
        this.stock.put(produit, stockActuel + quantiteEnKg);

        // Mettre à jour l'indicateur de stock total
        this.indicateurStockTotal.setValeur(this, getStockTotal());

        this.journalStocks.ajouter("Livraison reçue : " + (quantiteEnKg) + "t de " +
                           produit + " (contrat cadre)");
    }


    //         MÉTHODES UTILITAIRES

    
    protected double restantDu(IProduit produit) {
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
        List<ChocolatDeMarque> produits = Filiere.LA_FILIERE.getChocolatsProduits();
        for (ChocolatDeMarque choco : produits) {
            double stockActuel = this.stock.getOrDefault(choco, 0.0);
            double enCours = restantDu(choco);
            double seuilDeSecurite = 10 ;
            double stockProjete = stockActuel + enCours;

            if (stockProjete < seuilDeSecurite) {
                double quantiteCible = 50.0; // 50 tonnes
                double quantiteAcheter = quantiteCible - stockProjete;
                if (quantiteAcheter < 100.0) { // Minimum 100 tonnes
                    continue;
                }

                // Vérifier les fonds disponibles
                double prixEstime = getPrixMaxAcceptable(choco);
                double coutEstime = (quantiteAcheter / 1.0) * prixEstime;
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

                // Essayer de négocier avec les vendeurs
                boolean propositionReussie = false;
                for (IVendeurContratCadre vendeur : vendeurs) {
                    int stepDebut = Filiere.LA_FILIERE.getEtape() + 1;
                    int nbSteps = Math.min(6, 24 - stepDebut);
                    if (nbSteps <= 0) continue;

                    double quantiteParStep = quantiteAcheter / nbSteps;
                    Echeancier echeancierPropose = new Echeancier(stepDebut, nbSteps, quantiteParStep);

                    // Initier la négociation
                    ExemplaireContratCadre contrat = this.superviseurCC.demandeAcheteur(
                        this, vendeur, choco, echeancierPropose, this.cryptogramme, false);

                    if (contrat != null) {
                        this.journalCC.ajouter("Proposition CC initiée pour " + (quantiteAcheter)
                            + "t de " + choco.getNom() + " chez " + vendeur.getNom());
                        propositionReussie = true;
                        break; // On s'arrête au premier vendeur qui accepte de négocier
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
    }

    /**
     * Méthode utilitaire qui définit le prix maximum qu'on accepte
     * selon la qualité du chocolat (en €/T)
     * Ces valeurs sont nos prix d'achat maximum pour garder une marge rentable
     */
    private double getPrixMaxAcceptable(ChocolatDeMarque choco) {
        return this.prix.getOrDefault(choco, 100.0) * 0.75; // 25% de marge systématique
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