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
        super.next();

        List<ExemplaireContratCadre> aRetirer = new LinkedList<ExemplaireContratCadre>();
        for (ExemplaireContratCadre contrat : this.contratsEnCours) {
            if (contrat.getQuantiteRestantALivrer() <= 0.0) {
                aRetirer.add(contrat);
                this.contratsTermines.add(contrat);
        this.journal.ajouter("Contrat terminé : " + contrat.getProduit());
            }
        }
        this.contratsEnCours.removeAll(aRetirer);

        this.evaluerFideliteContrats();
        this.journal.ajouter("Contrats en cours : " + this.contratsEnCours.size());
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
    public double contrePropositionPrixAcheteur(ExemplaireContratCadre contrat) {
        double prixPropose = contrat.getPrix();

        // Accepter le prix tel quel
        // A MODIFIER EN V2

        this.journal.ajouter("Acceptation du prix " + prixPropose + "€/t pour " + contrat.getProduit());
        return prixPropose;
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
}