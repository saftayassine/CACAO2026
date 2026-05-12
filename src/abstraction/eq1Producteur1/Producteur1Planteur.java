package abstraction.eq1Producteur1;
import java.util.ArrayList;
import java.util.List;
 
import abstraction.eqXRomu.filiere.Banque;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.produits.Feve;
 
 
 
public class Producteur1Planteur extends Producteur1Stock {
 
    private List<Plantation> plantations = new ArrayList<Plantation>();
    private double taille_totale = 1000000;
    private double tailleEq     = 850000;
    private double tailleNonEq  = 150000;
    private double capaciteProchaine;
    protected Journal journalPlantation;
    /**
     * @author Théophile Trillat
     */
    public Producteur1Planteur() {
        super();
        Plantation BQ = new Plantation(Feve.F_BQ, 850000, -600);
        Plantation MQ = new Plantation(Feve.F_MQ, 150000, -600);
        this.plantations.add(BQ);
        this.plantations.add(MQ);
        this.journalPlantation = new Journal("Journal "+this.getNom()+ " plantation", this);
    }
 
    /**
     * @author Elise Dossal
     */
    public double getTaillePlantation() {
        return this.taille_totale;
    }
 
    public double getTaillePlantation(Boolean eq) {
        if (eq) {
            return this.tailleEq;
        } else {
            return this.tailleNonEq;
        }
    }
 
    /**
     * @author Théophile Trillat
     */
    public void planter(Feve f, double taille) {
        Plantation newP = new Plantation(f, taille, Filiere.LA_FILIERE.getEtape());
        this.plantations.add(newP);
        if (f.isEquitable()) {
            this.tailleEq += taille;
        } else {
            this.tailleNonEq += taille;
        }
        this.taille_totale += taille;
        // BQ 1800/ha    MQ 3500/ha    HQ 7000/ha
    }
 
    /**
     * @author Elise Dossal
     */
    public void couper(int i) {
        double taille = this.plantations.get(i).getTaille();
        Feve f = this.plantations.get(i).getGamme();
        this.plantations.remove(i);
        if (f.isEquitable()) {
            this.tailleEq -= taille;
        } else {
            this.tailleNonEq -= taille;
        }
        this.taille_totale -= taille;
    }
 
    /**
     * @author Elise Dossal
     */
    public void collecter() { // On crée un lot de chaque qualité qui regroupe plusieurs plantations
        double lot_HQ   = 0;
        double lot_HQ_E = 0;
        double lot_MQ   = 0;
        double lot_MQ_E = 0;
        double lot_BQ   = 0;
        double lot_BQ_E = 0;
 
        for (int i = 0; i < this.plantations.size(); i++) {
            Plantation plantation = this.plantations.get(i);
            if (plantation.getEtat() == 10) { // vérifie si les arbres ne sont pas morts, sinon les coupe
                this.couper(i);
                i--; // compense le décalage d'indice après la suppression
                continue;
            }
            double cacao = plantation.collecte();
            Feve gamme = plantation.getGamme();
 
            if (gamme == Feve.F_HQ)   { lot_HQ   += cacao; }
            if (gamme == Feve.F_HQ_E) { lot_HQ_E += cacao; }
            if (gamme == Feve.F_MQ)   { lot_MQ   += cacao; }
            if (gamme == Feve.F_MQ_E) { lot_MQ_E += cacao; }
            if (gamme == Feve.F_BQ)   { lot_BQ   += cacao; }
            if (gamme == Feve.F_BQ_E) { lot_BQ_E += cacao; }
        }
 
        this.add_lot(Feve.F_HQ,   lot_HQ);
        this.add_lot(Feve.F_HQ_E, lot_HQ_E);
        this.add_lot(Feve.F_MQ,   lot_MQ);
        this.add_lot(Feve.F_MQ_E, lot_MQ_E);
        this.add_lot(Feve.F_BQ,   lot_BQ);
        this.add_lot(Feve.F_BQ_E, lot_BQ_E);
    }
 
    /**
     * @author Tristan Proust
     */
    public void gererRotation() { // Gère la rotation des plantations
        int etape = Filiere.LA_FILIERE.getEtape();
        
        if (etape % 24 != 0) return;
        int annee = etape / 24;
 
        for (int i = this.plantations.size() - 1; i >= 0; i--) {
            if (this.plantations.get(i).getEtat() == 10) {
                this.couper(i);
            }
        }

        double tailleAPlanter;
        if (annee <= 15) {
            tailleAPlanter = 67000;
        } else {
            tailleAPlanter = 25000;
        }
        double tailleBQAPlanter = tailleAPlanter * 0.85; // 85% BQ
        double tailleMQAPlanter = tailleAPlanter * 0.15; // 15% MQ
 
        this.planter(Feve.F_BQ, tailleBQAPlanter);
        this.planter(Feve.F_MQ, tailleMQAPlanter);
 
        double capaciteTotale = 0;
        for (Plantation p : this.plantations) {
            Feve gamme = p.getGamme();
            double rendement;
            if (gamme == Feve.F_MQ || gamme == Feve.F_MQ_E) { 
                rendement = 3500; 
            }
            else { 
                rendement = 1800;     // BQ / BQ_E
            } 
            capaciteTotale += p.getTaille() * rendement;
        }
 
        double tailleAPlanter_prochaine;
        if ((annee + 1) <= 15) {
            tailleAPlanter_prochaine = 67000;
        } else {
            tailleAPlanter_prochaine = 25000;
        }
        double tailleBQProchaine = tailleAPlanter_prochaine * 0.85;
        double tailleMQProchaine = tailleAPlanter_prochaine * 0.15;
 
        this.capaciteProchaine = capaciteTotale
                + (tailleBQProchaine * 1800)  // BQ planté l'an prochain
                + (tailleMQProchaine * 3500); // MQ planté l'an prochain
 
        this.journalPlantation.ajouter("Année " + annee + " : Rotation des plantations");
        this.journalPlantation.ajouter("Nouveaux arbres plantés : " + tailleAPlanter + " ha"
                + " (BQ : " + tailleBQAPlanter + " ha, MQ : " + tailleMQAPlanter + " ha)");
        this.journalPlantation.ajouter("Surface totale : " + this.taille_totale
                + " ha (équitable : " + this.tailleEq + " ha, non-équitable : " + this.tailleNonEq + " ha)");
        this.journalPlantation.ajouter("Production annuelle estimée : " + capaciteTotale + " kg");
        this.journalPlantation.ajouter("Production estimée l'année prochaine : " + capaciteProchaine + " kg");
    }
 
    public double getCapaciteProchaine() {
        return this.capaciteProchaine;
    }

    public void impots(){
        double montant = 250*this.taille_totale;
        Banque banque = Filiere.LA_FILIERE.getBanque();
        banque.payerCout(this, this.cryptogramme, "Impot plantation" ,montant);
        this.journalBanque.ajouter("Impot plantation : " + montant);
    }
 
    public void charge() {
        Banque banque = Filiere.LA_FILIERE.getBanque();
        banque.payerCout(this, this.cryptogramme, "Masse salariale", 617.65);
        this.journalBanque.ajouter("Charges payées : " + 617.65);
    }
 
    /**
     * @author Elise Dossal
     */
    @Override
    public void next() {
        super.next();
        int etape = Filiere.LA_FILIERE.getEtape();
        this.impots();
        this.gererRotation(); // coupe les morts + replante + journalise la capacité
        if (etape % 24 == 0) { // Une collecte tous les ans
            this.collecter();
            this.charge();
        }
    }
}
