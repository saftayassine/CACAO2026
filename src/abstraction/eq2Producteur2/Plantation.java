package abstraction.eq2Producteur2;
import abstraction.eqXRomu.produits.Feve;
/** @author Paul */
public class Plantation {
    private static final double FEVES_PAR_TONNE = 1_000_000.0;
    private Feve typeFeve;          // Type de fèves cultivées
    private int parcelles;          // Nombre de parcelles de 1 ha
    private int age;                // Âge de la plantation en steps
    private final int dureeDeVie;   // Durée de vie maximale avant remplacement
    private final int tempsAvantProduction; // Temps nécessaire avant production
    private final int productionParParcelle; // Nombre de fèves par parcelle a chaque step
    private double prix_achat; // Prix d'achat de la plantation
    private double prix_vente; // Prix de vente de la plantation
    private double prix_replantation; // Prix de replantation de la plantation
    private double salaire_employe; // Prix que coûtent les employés par step par parcelle
    private double cout_cooperative = 0 ; // Prix que coûtent les infrastructures communes à la coopérative par step
    private double stock_max;       // Stock maximum de fèves avant de ne pas replanter
    private boolean replante = false;
    
    // Champs pour la certification équitable
    private boolean estEquitable = false;           // Est certifiée équitable
    private int nombreOuvriers = 0;                 // Nombre d'ouvriers sur cette parcelle
    private boolean travailEnfant = false;          // Y a-t-il du travail enfant ?
    private double salaireMiniJournalier = 0.0;    // Salaire minimum journalier (euros)
    private double coutLabelMensuel = 0.0;         // Coût du label équitable par mois (tous les 2 next)
    private int etapeLastCoutLabel = -1;           // Dernier paiement du label

    public Plantation(Feve typeFeve, int parcelles, int age) {
        super();
        this.typeFeve = typeFeve;
        this.parcelles = parcelles;
        this.age = age; // Plantation récente
        
        // Initialisation des paramètres selon la qualité des fèves
        switch(typeFeve) {
            case F_BQ:
                this.dureeDeVie = 960;  // 40 ans
                this.tempsAvantProduction = 72;  // 3 ans
                this.productionParParcelle = 105000 ; //  fèves par parcelle a chaque next
                this.prix_achat = 2000 ;
                this.prix_vente = 1200 ;
                this.prix_replantation = 1000 ; // 1 euro par plant
                this.salaire_employe = 150 ;
                this.stock_max = 0 ; // Stock maximum en tonnes pour F_BQ
                break;
 
            case F_MQ:
                this.dureeDeVie = 960;
                this.tempsAvantProduction = 72; // 3 ans
                this.productionParParcelle = 85000;
                this.prix_achat = 2500;
                this.prix_vente = 1500 ;
                this.prix_replantation = 1500 ; // 1.5 euro par plant
                this.salaire_employe = 150 ;
                this.stock_max = 0 ; // Stock maximum en tonnes pour F_MQ
                break;

            case F_HQ:
                this.dureeDeVie = 960;
                this.tempsAvantProduction = 72; // 5 ans
                this.productionParParcelle = 63000;
                this.prix_achat = 3000 ;
                this.prix_vente = 1800 ;
                this.prix_replantation = 1750 ; // 1.75 euro par plant
                this.salaire_employe = 150 ;
                this.stock_max = 0 ; // Stock maximum en tonnes pour F_HQ
                break;

            case F_HQ_E:
                this.dureeDeVie = 960;
                this.tempsAvantProduction = 72; // 5 ans
                this.productionParParcelle = 63000 ;
                this.prix_achat = 3000 ;
                this.prix_vente = 1800 ;
                this.prix_replantation = 2000 ; // 2 euro par plant
                this.salaire_employe = 300 ;
                this.stock_max = 0 ; // Stock maximum en tonnes pour F_HQ_E
                break;
            
            default:
                throw new IllegalArgumentException("Type de fève non reconnu !");
        }
        
        // Initialisation par défaut : pas de certification
        this.etapeLastCoutLabel = -1;
    }


    /**
     * Avance l'âge de la plantation d'un step et renvoie la quantité produite en tonnes.
     */
    public double prodPlantation() {
        if (age == 0) {
            return 0; // Plantation récente, pas encore en production
        }
        else if (age < tempsAvantProduction) {
            return 0; // La plantation n'est pas encore en production
        }
        if (age >= dureeDeVie) {
            return 0; // Plantation morte, nécessite un remplacement
        }

        // Calcul de la production convertie en tonnes
        // Evite le debordement entier : le calcul doit se faire en double
        double fevesTotales = ((double) this.parcelles) * ((double) this.productionParParcelle);
        return fevesTotales / FEVES_PAR_TONNE;
    }

    public void add_age() {
        age++;
    }

    public Feve getTypeFeve() {
        return typeFeve;
    }

    public int getParcelles() {
        return parcelles;
    }

    public int getAge() {
        return age;
    }

    public int getDureeDeVie() {
        return dureeDeVie;
    }

    public int getTempsAvantProduction() {
        return tempsAvantProduction;
    }

    public int getProductionParParcelle() {
        return productionParParcelle;
    }

    public boolean estMorte() {
        return age >= dureeDeVie;
    }


    public boolean getReplante() {
        return replante;
    }

    public boolean estProductive() {
        return age >= tempsAvantProduction && age < dureeDeVie;
    }

    /**
     * Replante l'arbre si les conditions sont remplies:
     * 1. Le stock de fève de la gamme ne dépasse pas le seuil stock_max
     * 2. L'arbre vient de dépasser l'âge limite 
     */
    public boolean Replante(double stockFeve) {
        // Vérifier que l'arbre est effectivement mort
        if (age < dureeDeVie) {
            return false;
        }
        
        // Vérifier que le stock ne dépasse pas le seuil maximum
        if (stockFeve > stock_max) {
            return false;
        }
        
        // Les conditions sont remplies, replanter l'arbre
        age = 0;
        replante = true;
        return true;
    }
    
    public double getprix_achat() {
        return prix_achat;
    }

    public double getprix_replantation() {
        return prix_replantation;
    }

    public double getcout() {
        if ((age == 0) && (replante == false)) {
            return parcelles*prix_achat + cout_cooperative;
        }
        else if ((age == 0) && (replante == true)) {
            return parcelles*prix_replantation + cout_cooperative;
        }
        else if (age <= dureeDeVie){
            return parcelles*salaire_employe + cout_cooperative;
        }
        else {
            return 0;
        }
    }

    public double getcout_amorti() {
        if ((age == 0) && (replante == false)) {
            return cout_cooperative + parcelles*prix_achat / 960;
        }
        else if ((age == 0) && (replante == true)) {
            return cout_cooperative + parcelles*prix_replantation / 960;
        }
        else if ((age <= dureeDeVie) && (replante == false)){
            return parcelles*salaire_employe + cout_cooperative + (parcelles*prix_achat / 960);
        }
        else if ((age <= dureeDeVie) && (replante == true)){
            return parcelles*salaire_employe + cout_cooperative + (parcelles*prix_replantation / 960);
        }
        else {
            return 0;
        }
    }

    public double get_prix_vente() {
        return prix_vente;
    }

    // ========== Méthodes pour la certification équitable ==========
    
    /**
     * Active le mode équitable avec les paramètres nécessaires
     */
    public void activerCertificationEquitable(int nombreOuvriers, double salaireMiniJournalier, double coutLabelMensuel, double pourcentageCertifie) {
        if (this.typeFeve != Feve.F_HQ && this.typeFeve != Feve.F_HQ_E) {
            return; // Seules les plantations F_HQ/F_HQ_E peuvent devenir équitables
        }
        
        if (pourcentageCertifie < 0 || pourcentageCertifie > 100) {
            return;
        }
        
        // Réduire le nombre de parcelles à la portion certifiée
        this.parcelles = (int) Math.ceil(this.parcelles * (pourcentageCertifie / 100.0));
        
        this.nombreOuvriers = nombreOuvriers;
        this.salaireMiniJournalier = salaireMiniJournalier;
        this.coutLabelMensuel = coutLabelMensuel;
        this.travailEnfant = false; // Certification équitable = pas de travail enfant
        this.estEquitable = true;
        this.typeFeve = Feve.F_HQ_E; // Conversion en F_HQ_E
    }
    
    /**
     * Crée une copie de cette plantation avec un pourcentage non-certifié
     * Utile pour diviser une plantation en partie certifiée + non-certifiée
     */
    public Plantation diviserPlantation(double pourcentageACertifier) {
        if (this.typeFeve != Feve.F_HQ) {
            return null; // Seules les plantations F_HQ peuvent être divisées
        }
        
        if (pourcentageACertifier <= 0 || pourcentageACertifier >= 100) {
            return null;
        }
        
        // Calculer les parcelles de chaque partie
        int parcellesCertifiees = (int) Math.ceil(this.parcelles * (pourcentageACertifier / 100.0));
        int parcellesNonCertifiees = this.parcelles - parcellesCertifiees;
        
        // Créer une nouvelle plantation avec les parcelles non-certifiées
        Plantation plantationNonCertifiee = new Plantation(Feve.F_HQ, parcellesNonCertifiees, this.age);
        
        // Réduire cette plantation à la portion certifiée
        this.parcelles = parcellesCertifiees;
        
        return plantationNonCertifiee;
    }
    
    /**
     * Vérifie si les conditions équitables sont respectées
     */
    public boolean verifierConditionsEquitables() {
        if (!estEquitable) {
            return false;
        }
        return nombreOuvriers >= 30 && 
               salaireMiniJournalier >= 4.0 && 
               !travailEnfant;
    }
    
    /**
     * Retourne le coût du label équitable (tous les 2 next = 2 étapes)
     */
    public double getCoutLabelEquitable(int etapeActuelle) {
        if (!estEquitable || !verifierConditionsEquitables()) {
            return 0.0;
        }
        
        // Paiement tous les 2 next
        if (etapeLastCoutLabel == -1) {
            this.etapeLastCoutLabel = etapeActuelle;
            return coutLabelMensuel;
        }
        
        if (etapeActuelle - etapeLastCoutLabel >= 2) {
            this.etapeLastCoutLabel = etapeActuelle;
            return coutLabelMensuel;
        }
        
        return 0.0;
    }
    
    /**
     * Retourne le coût ajusté des ouvriers pour l'équitable
     */
    public double getCoutOuvriersEquitable() {
        if (!estEquitable || !verifierConditionsEquitables()) {
            return parcelles * salaire_employe;
        }
        // Coût = nombre d'ouvriers × salaire journalier × nombre de jours par step (2 jours)
        double coutEquitable = nombreOuvriers * salaireMiniJournalier * 2; // 2 jours par step environ
        return coutEquitable;
    }
    
    // ========== Getters/Setters ==========
    public boolean estEquitable() {
        return estEquitable;
    }
    
    public int getNombreOuvriers() {
        return nombreOuvriers;
    }
    
    public void setNombreOuvriers(int nombreOuvriers) {
        this.nombreOuvriers = nombreOuvriers;
    }
    
    public boolean hasTravailEnfant() {
        return travailEnfant;
    }
    
    public void setTravailEnfant(boolean travailEnfant) {
        this.travailEnfant = travailEnfant;
    }
    
    public double getSalaireMiniJournalier() {
        return salaireMiniJournalier;
    }
    
    public void setSalaireMiniJournalier(double salaire) {
        this.salaireMiniJournalier = salaire;
    }
    
    public double getStock_max() {
        return stock_max;
    }
    
    public void setStock_max(double stock_max) {
        this.stock_max = stock_max;
    }
}