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
    private boolean replante = false;

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
                this.salaire_employe = 18 ;
                break;
 
            case F_MQ:
                this.dureeDeVie = 960;
                this.tempsAvantProduction = 72; // 3 ans
                this.productionParParcelle = 85000;
                this.prix_achat = 2500;
                this.prix_vente = 1500 ;
                this.prix_replantation = 1500 ; // 1.5 euro par plant
                this.salaire_employe = 18;
                break;

            case F_HQ:
                this.dureeDeVie = 960;
                this.tempsAvantProduction = 72; // 5 ans
                this.productionParParcelle = 63000;
                this.prix_achat = 3000 ;
                this.prix_vente = 1800 ;
                this.prix_replantation = 1750 ; // 1.75 euro par plant
                this.salaire_employe = 18 ;
                break;

            case F_HQ_E:
                this.dureeDeVie = 960;
                this.tempsAvantProduction = 72; // 5 ans
                this.productionParParcelle = 63000 ;
                this.prix_achat = 3000 ;
                this.prix_vente = 1800 ;
                this.prix_replantation = 2000 ; // 2 euro par plant
                this.salaire_employe = 18 ;
                break;
            
            default:
                throw new IllegalArgumentException("Type de fève non reconnu !");
        }
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

    public void Replante() {
        age = 0;
        replante = true;
    }

    public boolean getReplante() {
        return replante;
    }

    public boolean estProductive() {
        return age >= tempsAvantProduction && age < dureeDeVie;
    }

    public double getprix_achat() {
        return prix_achat;
    }

    public double getprix_replantation() {
        return prix_replantation;
    }

    public double getcout() {
        if ((age == 0) && (replante == false)) {
            return parcelles*prix_achat;
        }
        else if ((age == 0) && (replante == true)) {
            return parcelles*prix_replantation;
        }
        else if (age <= dureeDeVie){
            return parcelles*salaire_employe;
        }
        else {
            return 0;
        }
    }

    public double getcout_amorti() {
        if ((age == 0) && (replante == false)) {
            return parcelles*prix_achat / 960;
        }
        else if ((age == 0) && (replante == true)) {
            return parcelles*prix_replantation / 960;
        }
        else if ((age <= dureeDeVie) && (replante == false)){
            return parcelles*salaire_employe + (parcelles*prix_achat / 960);
        }
        else if ((age <= dureeDeVie) && (replante == true)){
            return parcelles*salaire_employe + (parcelles*prix_replantation / 960);
        }
        else {
            return 0;
        }
    }

    public double get_prix_vente() {
        return prix_vente;
    }
}