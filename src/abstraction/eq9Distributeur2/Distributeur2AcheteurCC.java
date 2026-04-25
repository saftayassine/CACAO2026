package abstraction.eq9Distributeur2;

import abstraction.eqXRomu.contratsCadres.Echeancier;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.contratsCadres.IAcheteurContratCadre;
import abstraction.eqXRomu.contratsCadres.IVendeurContratCadre;
import abstraction.eqXRomu.contratsCadres.SuperviseurVentesContratCadre;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.IProduit;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;

/**
 * @author Paul Juhel et Paul ROSSIGNOL
 */

public class Distributeur2AcheteurCC extends Distributeur2AcheteurAO implements IAcheteurContratCadre {

    // Superviseur des contrats cadres
    private SuperviseurVentesContratCadre superviseurCC;

    // Liste des contrats en cours
    protected List<ExemplaireContratCadre> contratsEnCours;

    // Liste des contrats terminés
    protected List<ExemplaireContratCadre> contratsTermines;

    // score de fidélité par vendeur logique opportuniste
    protected java.util.Map<IVendeurContratCadre, Double> scoreFidelite;

    public Distributeur2AcheteurCC() {
        super();
        this.contratsEnCours = new LinkedList<ExemplaireContratCadre>();
        this.contratsTermines = new LinkedList<ExemplaireContratCadre>();
        this.scoreFidelite = new java.util.HashMap<IVendeurContratCadre, Double>();
    }

    @Override
    public void initialiser() {
        super.initialiser();
        this.superviseurCC = (SuperviseurVentesContratCadre) Filiere.LA_FILIERE.getActeur("Sup.CCadre");
        this.journal.ajouter("Initialisation des CC");
    }

    @Override
    public void next() {
        int etape = Filiere.LA_FILIERE.getEtape();
        this.journal.ajouter("=== ETAPE " + etape + " ===");

        List<ChocolatDeMarque> produits = Filiere.LA_FILIERE.getChocolatsProduits();

        if (produits != null && !produits.isEmpty()) {
            for (ChocolatDeMarque choco : produits) {
                double quantiteActuelle = this.stock.getOrDefault(choco, 0.0);
                double seuilMin = 10000.0;  // 10 tonnes : seuil déclenchant réappro
                double stockCible = 50000.0; // 50 tonnes : stock visé

                // On ne réapprovisionne que si on est en dessous du seuil
                if (quantiteActuelle < seuilMin) {
                    double ajout = stockCible - quantiteActuelle;
                    this.stock.put(choco, quantiteActuelle + ajout);
                    journal.ajouter("Réapprovisionnement " + choco.getNom() 
                        + " : +" + (ajout/1000) + "t (stock était " 
                        + (quantiteActuelle/1000) + "t)");
                }
                
            }
            // Frais de stockage : 120 €/T par étape (16x le coût producteur de 7.5€/T)
            payerFraisStockage();
            // Ajustement dynamique des prix de vente
            ajusterPrix();
        }

        this.indicateurStockTotal.setValeur(this, getStockTotal());
        journal.ajouter("Stock total : " + (getStockTotal()/1000) + " tonnes");
    }


    protected void evaluerFideliteContrats() {
        for (ExemplaireContratCadre contrat : this.contratsEnCours) {
            IVendeurContratCadre vendeur = contrat.getVendeur();
            double prixCD = contrat.getPrix();
            double prixConcurrent = prixCD * 0.98; // hypothèse prix concurrent meilleur
            boolean garder = garderFournisseur(vendeur, prixCD, prixConcurrent);
            if (!garder) {
                this.journal.ajouter("Remplacement opportuniste du fournisseur " + vendeur.getNom() + " (prix=" + prixCD + ")");
            }
        }
    }

    //         IMPLEMENTATION DE L'INTERFACE IAcheteurContratCadre

    /**
     * Indique si l'acheteur est prêt à faire un contrat cadre pour ce produit
     * @param produit le produit concerné
     * @return true si prêt à négocier, false sinon
     */

    @Override
    public boolean achete(IProduit produit) {
        // On accepte tous les contrats cadres pour les chocolats de marque
        if (produit instanceof ChocolatDeMarque) {
            this.journal.ajouter("Prêt à négocier un contrat cadre pour " + produit);
            return true;
        }
        return false;
    }

    /**
     * Contre-proposition de l'échéancier
     * @param contrat le contrat en négociation
     * @return l'échéancier proposé, null pour abandonner, ou le même pour accepter
     */
    @Override
    public Echeancier contrePropositionDeLAcheteur(ExemplaireContratCadre contrat) {
        Echeancier propositionVendeur = contrat.getEcheancier();

        // Accepter l'échéancier tel quel
        // A MODIFIER EN V2
        this.journal.ajouter("Acceptation de l'échéancier pour " + contrat.getProduit());
        return propositionVendeur;
    }

    /**
     * Contre-proposition sur le prix
     * @param contrat le contrat en négociation
     * @return le prix proposé, négatif pour abandonner, ou le même pour accepter
     */
    @Override
    /** @author Anass Ouisrani*/
public double contrePropositionPrixAcheteur(ExemplaireContratCadre contrat) {
    // On récupère le prix que le vendeur propose
    double prixPropose = contrat.getPrix();
    
    // On récupère le chocolat concerné par le contrat
    ChocolatDeMarque choco = (ChocolatDeMarque) contrat.getProduit();
    
    // On récupère l'étape actuelle pour consulter le prix moyen du marché
    int etape = Filiere.LA_FILIERE.getEtape();

    // On définit notre prix maximum acceptable selon la qualité du chocolat
    double prixMax = getPrixMaxAcceptable(choco);

    // Si le vendeur demande plus que notre maximum → on abandonne la négociation
    // -1.0 signifie "je me retire" dans le protocole
    if (prixPropose > prixMax) {
        this.journal.ajouter("Abandon négociation CC : prix " + prixPropose 
            + "€/T trop élevé (max=" + prixMax + "€/T)");
        return -1.0;
    }

    // On consulte le prix moyen du marché à l'étape précédente
    // Si etape=0 on n'a pas de référence donc on met NaN (Not a Number = pas de valeur)
    double prixMoyen = (etape >= 1) ? Filiere.LA_FILIERE.prixMoyen(choco, etape - 1) : Double.NaN;

    // Si on a une référence marché valide
    if (!Double.isNaN(prixMoyen) && prixMoyen > 0) {
        
        // On propose 5% moins cher que ce que demande le vendeur
        double contreProposition = prixPropose * 0.95;
        
        // Mais jamais en dessous de 98% du prix moyen marché
        // sinon le vendeur refusera forcément
        contreProposition = Math.max(contreProposition, prixMoyen * 0.98);
        
        // Si après calcul notre contre-proposition est >= au prix du vendeur
        // ça ne sert à rien de négocier, on accepte directement
        if (contreProposition >= prixPropose) {
            this.journal.ajouter("Acceptation CC : " + prixPropose + "€/T pour " + choco.getNom());
            return prixPropose;
        }
        
        // On envoie notre contre-proposition
        this.journal.ajouter("Contre-proposition CC : " + contreProposition 
            + "€/T (proposé=" + prixPropose + ", marché=" + prixMoyen + ")");
        return contreProposition;
    }

    // Pas de référence marché disponible → on accepte le prix
    // tant qu'il est dans nos limites (déjà vérifié au dessus)
    this.journal.ajouter("Acceptation CC (pas de ref marché) : " + prixPropose + "€/T");
    return prixPropose;
}

// Méthode utilitaire qui définit le prix maximum qu'on accepte
// selon la qualité du chocolat (en €/T)
// Ces valeurs sont nos prix d'achat maximum pour garder une marge rentable
/** @author Anass Ouisrani*/
private double getPrixMaxAcceptable(ChocolatDeMarque choco) {
    return prix(choco) * 0.75; // 25% de marge systématique
}

    /**
     * Notification de la réussite des négociations
     * @param contrat le contrat signé
     */
    @Override
    public void notificationNouveauContratCadre(ExemplaireContratCadre contrat) {
        this.contratsEnCours.add(contrat);

        // Mise à jour fidélité
        IVendeurContratCadre v = contrat.getVendeur();
        double oldScore = this.scoreFidelite.getOrDefault(v, 0.3);
        double nouvelScore = Math.min(1.0, oldScore + 0.10);
        this.scoreFidelite.put(v, nouvelScore);

        this.journal.ajouter("Nouveau contrat cadre signé pour " + contrat.getProduit() +
                           " : " + contrat.getQuantiteTotale() + "t à " + contrat.getPrix() + "€/t" +
                           " (score fidélité " + String.format("%.2f", nouvelScore) + ")");
    }

    /**
     * Détermination de conservation d'un fournisseur
     */
    public boolean garderFournisseur(IVendeurContratCadre vendeur, double prixActuel, double prixConcurrentMax) {
        double remiseHistorique = this.scoreFidelite.getOrDefault(vendeur, 0.3);
        double diffRel = (prixActuel - prixConcurrentMax) / Math.max(1.0, prixConcurrentMax);

        if (diffRel > 0.05 && remiseHistorique < 0.6) {
            return false;
        }
        if (remiseHistorique >= 0.7) {
            return true;
        }
        return diffRel <= 0.03;
    }

    /**
     * Réception d'une livraison dans le cadre d'un contrat
     * @param produit le produit livré
     * @param quantiteEnTonnes la quantité livrée (en tonnes)
     * @param contrat le contrat concerné
     */

    @Override
    public void receptionner(IProduit produit, double quantiteEnTonnes, ExemplaireContratCadre contrat) {

        double quantiteEnKg = quantiteEnTonnes * 1000.0;

        // Ajouter au stock
        double stockActuel = this.stock.getOrDefault(produit, 0.0);
        this.stock.put(produit, stockActuel + quantiteEnKg);

        // Mettre à jour l'indicateur de stock total
        this.indicateurStockTotal.setValeur(this, getStockTotal());

        this.journal.ajouter("Livraison reçue : " + (quantiteEnKg/1000) + "t de " +
                           produit + " (contrat cadre)");
    }


    //         MÉTHODES UTILITAIRES

    
    /**
     * Calcule la quantité restante à livrer pour un produit donné
     * @param produit le produit
     * @return la quantité totale restant à livrer (en kg)
     */
    protected double restantDu(IProduit produit) {
        double res = 0.0;
        for (ExemplaireContratCadre contrat : this.contratsEnCours) {
            if (contrat.getProduit().equals(produit)) {
                res += contrat.getQuantiteRestantALivrer() * 1000.0;
            }
        }
        return res;
    }

    /**
     * Renvoie la liste des contrats en cours
     * @return liste des contrats actifs
     */
    public List<ExemplaireContratCadre> getContratsEnCours() {
        return new LinkedList<ExemplaireContratCadre>(this.contratsEnCours);
    }

    /**
     * Renvoie la liste des contrats terminés
     * @return liste des contrats terminés
     */
    public List<ExemplaireContratCadre> getContratsTermines() {
        return new LinkedList<ExemplaireContratCadre>(this.contratsTermines);
    }

    /**
     * Méthode pour initier des propositions de contrats cadres (V1)
     */
    /**
    * @author Paul JUHEL
    */
    public void fairePropositionCC() {
        List<ChocolatDeMarque> produits = Filiere.LA_FILIERE.getChocolatsProduits();
        for (ChocolatDeMarque choco : produits) {
            double stockActuel = this.stock.getOrDefault(choco, 0.0);
            double seuilDeSecurite = 10000.0; // 10 tonnes
            if (stockActuel < seuilDeSecurite) {
                double quantiteCible = 50000.0; // 50 tonnes
                double quantiteAcheter = quantiteCible - stockActuel;
                if (quantiteAcheter < 1000.0) { // Minimum 1 tonne
                    continue;
                }
                List<IVendeurContratCadre> vendeurs = this.superviseurCC.getVendeurs(choco);
                for (IVendeurContratCadre vendeur : vendeurs) {
                    int stepDebut = Filiere.LA_FILIERE.getEtape() + 1;
                    int nbSteps = 6;
                    double quantiteParStep = quantiteAcheter / nbSteps;
                    Echeancier echeancierPropose = new Echeancier(stepDebut, nbSteps, quantiteParStep);
                    ExemplaireContratCadre contrat = this.superviseurCC.demandeAcheteur(this, vendeur, choco, echeancierPropose, this.cryptogramme, false);
                    if (contrat != null) {
                        this.journal.ajouter("Proposition CC réussie pour " + (quantiteAcheter/1000) + "t de " + choco.getNom() + " chez " + vendeur.getNom());
                        break;
                    }
                }
            }
        }
    }
}