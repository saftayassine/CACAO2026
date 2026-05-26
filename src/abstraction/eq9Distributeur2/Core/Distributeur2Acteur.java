package abstraction.eq9Distributeur2.Core;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import abstraction.eqXRomu.filiere.IMarqueChocolat;
import abstraction.eq9Distributeur2.Config.EQ9Config;
import abstraction.eq9Distributeur2.Prix.EQ9_Pricing;
import abstraction.eq9Distributeur2.Stratégie.EQ9_StrategieConcurrentielle;
import abstraction.eq9Distributeur2.Stratégie.EQ9_StrategieFixationPrix;
import abstraction.eqXRomu.clients.ClientFinal;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IActeur;
import abstraction.eqXRomu.filiere.IDistributeurChocolatDeMarque;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.IProduit;

public class Distributeur2Acteur implements IActeur, IDistributeurChocolatDeMarque, IMarqueChocolat {
    protected int cryptogramme;
    protected Journal journal;
    protected Journal journalStocks;
    protected Journal journalCC;
    protected Journal journalAO;
    protected Journal journalFinancier;
    protected Map<IProduit, Double> stock;
    protected Variable indicateurStockTotal;
    protected Map<ChocolatDeMarque, Double> prix;
    protected double capaciteRayonTonnes = EQ9Config.CAPACITE_RAYON_T;

    protected EQ9_StrategieFixationPrix strategieFixationPrix;

    public static final String NOM_MARQUE = "EQ9";
    protected Variable indicateurMargeMoyenne;
    protected Variable indicateurMixMarquePrivee;
    protected Variable indicateurProfitBrutEtape;

    protected Variable indicateurPartMarche;
    protected Variable indicateurDOS;
    protected Variable indicateurTauxRupture;
    protected Variable indicateurCashBuffer;
    protected EQ9_StrategieConcurrentielle strategieConcurrentielle;
    protected EQ9_Pricing pricingService;




    /**
     * @author Paul Juhel
     */
    public Distributeur2Acteur() {
        this.journal = new Journal("Journal EQ9 - Général", this);
        this.journalStocks = new Journal("Journal EQ9 - Stocks", this);
        this.journalCC = new Journal("Journal EQ9 - Contrats Cadres", this);
        this.journalAO = new Journal("Journal EQ9 - Appels d'offres", this);
        this.journalFinancier = new Journal("Journal EQ9 - Finances", this);
        this.stock = new HashMap<>();
        this.indicateurStockTotal = new Variable("EQ9_stock_total", this, 0.0);
        this.indicateurMargeMoyenne = new Variable("EQ9_marge_moyenne", this, 18.0);
        this.indicateurMixMarquePrivee = new Variable("EQ9_pct_marque_privee", this, 40.0);
        this.indicateurProfitBrutEtape = new Variable("EQ9_profit_brut", this, 0.0);
        this.indicateurPartMarche = new Variable("EQ9_part_marche", this, 0.0);
        this.indicateurDOS = new Variable("EQ9_days_of_stock", this, 0.0);
        this.indicateurTauxRupture = new Variable("EQ9_taux_rupture", this, 0.0);
        this.indicateurCashBuffer = new Variable("EQ9_cash_buffer", this, 0.0);
        this.strategieConcurrentielle = new EQ9_StrategieConcurrentielle();
        this.pricingService = new EQ9_Pricing();
    }

    /**
     * @author Anass Ouisrani et Paul Juhel
     */
    public void initialiser() {
        this.stock.clear();
        List<ChocolatDeMarque> produits = Filiere.LA_FILIERE.getChocolatsProduits();

        // Initialiser le stock pour tous les produits disponibles
        if (produits != null && !produits.isEmpty()) {
            for (ChocolatDeMarque choco : produits) {

                this.stock.put(choco, 40000.0);
            }
        }

        this.indicateurStockTotal.setValeur(this, getStockTotal());

        // Initialisation des prix selon la qualité du chocolat
        this.prix = new HashMap<>();

        this.strategieFixationPrix = new EQ9_StrategieFixationPrix(journal);

        this.indicateurMargeMoyenne.setValeur(this, 18.0);
        this.indicateurMixMarquePrivee.setValeur(this, 40.0);
        this.indicateurProfitBrutEtape.setValeur(this, 0.0);

        journal.ajouter("Initialisation terminée : " + produits.size() + " produits en stock");
    }

    public String getNom() {// NE PAS MODIFIER
        return "EQ9";
    }

    public String toString() {// NE PAS MODIFIER
        return this.getNom();
    }

    ///////////////////////////////////////////////////////
    // En lien avec l'interface graphique //
    ///////////////////////////////////////////////////////

    /**
     * @author Paul Juhel
     * @author Anass Ouisrani
     */
    public void next() {
        int etape = Filiere.LA_FILIERE.getEtape();
        this.journal.ajouter("=== ETAPE " + etape + " ===");
        this.mettreAJourKPIs();
        // Cette méthode est surchargée par Distributeur2AcheteurCC
        // Elle ne doit pas être appelée directement
    }

    /** @author Anass Ouisrani */
    protected double getStockTotal() {
        double total = 0.0;
        for (double q : stock.values()) {
            total += q;
        }
        return total;
    }

    public Color getColor() {// NE PAS MODIFIER
        return new Color(245, 155, 185);
    }

    public String getDescription() {
        return "Distributeur moderne avec stratégie de prix différenciés et gestion optimisée des stocks";
    }

    /**
     * @author Anass Ouisrani
     */
    // Renvoie les indicateurs
    public List<Variable> getIndicateurs() {
        List<Variable> res = new ArrayList<Variable>();
        res.add(indicateurStockTotal);
        res.add(indicateurMargeMoyenne);
        res.add(indicateurMixMarquePrivee);
        res.add(indicateurProfitBrutEtape);
        res.add(indicateurPartMarche);
        res.add(indicateurDOS);
        res.add(indicateurTauxRupture);
        res.add(indicateurCashBuffer);
        return res;
    }

    // Renvoie les parametres
    public List<Variable> getParametres() {
        List<Variable> res = new ArrayList<Variable>();
        return res;
    }

    // Renvoie les journaux
    /**
     * @author Paul Juhel
     */
    public List<Journal> getJournaux() {
        List<Journal> res = new ArrayList<Journal>();
        res.add(journal);
        res.add(journalStocks);
        res.add(journalCC);
        res.add(journalAO);
        res.add(journalFinancier);
        return res;
    }

    ///////////////////////////////////////////////////////
    // En lien avec la Banque //
    ///////////////////////////////////////////////////////

    /**
     * @author Paul Juhel et Paul Rossignol
     */
    // Appelee en debut de simulation pour communiquer cryptogramme personnel
    public void setCryptogramme(Integer crypto) {
        this.cryptogramme = crypto;
    }

    // Appelee lorsqu'un acteur fait faillite

    public void notificationFaillite(IActeur acteur) {
        if (acteur != null) {
            journal.ajouter("Information : " + acteur.getNom() + " a fait faillite");
        }
    }

    // Nous informer apres chaque operation sur votre compte bancaire,
    public void notificationOperationBancaire(double montant) {
        if (montant > 0) {
            journal.ajouter("Crédit bancaire : +" + montant + "€");
        } else {
            journal.ajouter("Débit bancaire : " + montant + "€");
        }
    }

    // Renvoie le solde actuel
    protected double getSolde() {
        return Filiere.LA_FILIERE.getBanque().getSolde(Filiere.LA_FILIERE.getActeur(getNom()), this.cryptogramme);
    }

    ///////////////////////////////////////////////////////
    // Pour la creation de filieres de test //
    ///////////////////////////////////////////////////////

    // Renvoie la liste des filieres proposees
    public List<String> getNomsFilieresProposees() {
        ArrayList<String> filieres = new ArrayList<String>();
        return (filieres);
    }

    // Renvoie une instance d'une filiere d'apres son nom
    public Filiere getFiliere(String nom) {
        return Filiere.LA_FILIERE;
    }

    public double getQuantiteEnStock(IProduit p, int cryptogramme) {
        if (this.cryptogramme == cryptogramme) {
            return this.stock.getOrDefault(p, 0.0);
        } else {
            return 0;
        }
    }
    ///////////////////////////////////////////////////////
    // IDistributeurChocolatDeMarque //
    ///////////////////////////////////////////////////////

    /**
     * @author Anass Ouisrani et Paul Juhel
     */

    @Override
    public double prix(ChocolatDeMarque choco) {
        if (!prix.containsKey(choco)) {
            switch (choco.getChocolat()) {
                case C_HQ_E:
                    return 26000.0;
                case C_HQ:
                    return 22000.0;
                case C_MQ_E:
                    return 18000.0;
                case C_MQ:
                    return 16000.0;
                case C_BQ_E:
                    return 14000.0;
                case C_BQ:
                    return 12000.0;
                default:
                    return 0.0;
            }
        }
        return prix.getOrDefault(choco, 0.0);
    }

    @Override
    public double quantiteEnVente(ChocolatDeMarque choco, int crypto) {
        if (crypto != this.cryptogramme) {
            this.journal.ajouter("Tentative accès non autorisé quantiteEnVente");
            return 0.0;
        }
        double qStock = this.stock.getOrDefault(choco, 0.0);
        return Math.min(qStock, this.capaciteRayonTonnes);
    }

    @Override
    public double quantiteEnVenteTG(ChocolatDeMarque choco, int crypto) {
        if (crypto != this.cryptogramme) {
            this.journal.ajouter("Tentative accès non autorisé quantiteEnVenteTG");
            return 0.0;
        }
        return quantiteEnVente(choco, crypto) * 0.10;
    }

    @Override
    public void vendre(ClientFinal client, ChocolatDeMarque choco, double quantite, double montant, int crypto) {
        if (crypto != this.cryptogramme) {
            this.journal.ajouter("Tentative accès non autorisé vendre");
            return;
        }
        double stockActuel = this.stock.getOrDefault(choco, 0.0);
        if (quantite <= 0 || quantite > stockActuel) {
            this.journal.ajouter("Stock insuffisant pour " + choco.getNom() + ": demandé " + quantite + "t, dispo "
                    + stockActuel + "t");
            return;
        }
        this.stock.put(choco, stockActuel - quantite);
        this.indicateurStockTotal.setValeur(this, getStockTotal());
        this.journal.ajouter("Vente de " + quantite + "t de " + choco.getNom() + " pour " + montant + " €");
    }

    @Override
    public void notificationRayonVide(ChocolatDeMarque choco, int crypto) {
        if (crypto != this.cryptogramme)
            return;
        this.journal.ajouter("RUPTURE STOCK : " + choco.getNom() + " - Rayon vide ! Augmenter les achats.");
    }

    /**
     * Calcule le prix de maniere dynamique selon la qualité du chocolat
     * 
     * @author Paul JUHEL
     */

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

            double prixFinal = pricingService.calculerPrix(
                choco, coutAchat, stockT, dos, demande, prixConcurrent, partMarche, cash
            );

            this.prix.put(choco, prixFinal);

            this.journal.ajouter("Prix EQ9 " + choco.getNom() + " = " + prixFinal
                + "€/T (coût=" + coutAchat + ", DOS=" + dos + ", marché=" + prixConcurrent + ")");
        }
    }



    /**
     * Obtient le coût d'achat pour un produit (estimation)
     * @author Anass Ouisrani
     */
    protected double obtenirCoutAchat(ChocolatDeMarque choco) {
        int etape = Filiere.LA_FILIERE.getEtape();
        if (etape < 1)
            return prix(choco) * 0.75; // estimation par défaut

        // Le prix moyen du marché est la meilleure approximation
        // du coût d'achat réel
        double prixMoyen = Filiere.LA_FILIERE.prixMoyen(choco, etape - 1);
        if (prixMoyen > 0)
            return prixMoyen;

        // Fallback : 75% de notre prix de vente
        return prix(choco) * 0.75;
    }

    /**
     * Estime la demande clients pour un produit (placeholder)
     */
    protected double estimerDemandeClients(ChocolatDeMarque choco) {
        int etape = Filiere.LA_FILIERE.getEtape();
        if (etape < 1)
            return 50000.0; // Pas de données : estimation par défaut
        return Filiere.LA_FILIERE.getVentes(choco, etape - 1);
    }

    /**
     * Estime le prix concurrent direct (placeholder)
     */
    public double estimerPrixConcurrent(ChocolatDeMarque choco) {
        int etape = Filiere.LA_FILIERE.getEtape();
        if (etape < 1)
            return 0;

        // Le prix moyen du marché représente le prix concurrent
        double prixMoyen = Filiere.LA_FILIERE.prixMoyen(choco, etape - 1);
        if (Double.isNaN(prixMoyen) || prixMoyen <= 0) {
            return this.prix(choco); // fallback
        }
        return prixMoyen;
    }

    ///////////////////////////////////////////////////////
    // IMarqueChocolat //
    ///////////////////////////////////////////////////////

    /**
     * @author Anass Ouisrani
     */
    @Override
    public List<String> getMarquesChocolat() {
        List<String> marques = new ArrayList<>();
        marques.add(NOM_MARQUE);
        return marques;
    }

    /**
     * @author Paul JUHEL
     */

    protected double getCash() {
    return Filiere.LA_FILIERE.getBanque().getSolde(this, this.cryptogramme);
    }

    protected double estimerVentesTotalesDerniereEtape() {
        double ventes = 0.0;
        int etape = Filiere.LA_FILIERE.getEtape();
        if (etape < 1) return 0.0;
        for (ChocolatDeMarque ch : Filiere.LA_FILIERE.getChocolatsProduits()) {
            ventes += Filiere.LA_FILIERE.getVentes(ch, etape - 1);
        }
        return ventes;
    }

    protected void mettreAJourKPIs() {
        int etape = Filiere.LA_FILIERE.getEtape();
        if (etape < 1) return;

        // Part de marché (ventes EQ9 / ventes totales)
        double ventesTotales = 0.0;
        double ventesEQ9 = 0.0;
        for (ChocolatDeMarque ch : Filiere.LA_FILIERE.getChocolatsProduits()) {
            double v = Filiere.LA_FILIERE.getVentes(ch, etape - 1);
            ventesTotales += v;
            if (ch.getMarque().equals(NOM_MARQUE)) {
                ventesEQ9 += v;
            }
        }
        double partMarche = (ventesTotales > 0) ? (ventesEQ9 / ventesTotales * 100.0) : 0.0;
        this.indicateurPartMarche.setValeur(this, partMarche);

        // Days of Stock (DOS): stock total / ventes moyennes
        double stockKg = getStockTotal() * 1000.0;
        double ventesJourKg = ventesTotales / 365.0;
        double dos = (ventesJourKg > 0) ? stockKg / ventesJourKg : 999.0;
        this.indicateurDOS.setValeur(this, dos);

        // Cash buffer
        this.indicateurCashBuffer.setValeur(this, getCash());

        // Taux de rupture
        this.indicateurTauxRupture.setValeur(this, 0.0);
    }
    public boolean estMarqueConcurrente(ChocolatDeMarque choco) {
        String marque = choco.getMarque();
        
        
        for (IActeur acteur : Filiere.LA_FILIERE.getActeurs()) {
            
            
            if (acteur instanceof IMarqueChocolat && acteur instanceof IDistributeurChocolatDeMarque) {
                
                
                if (acteur != this) {
                    List<String> sesMarques = ((IMarqueChocolat) acteur).getMarquesChocolat();
                    
                    
                    if (sesMarques.contains(marque)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
}
