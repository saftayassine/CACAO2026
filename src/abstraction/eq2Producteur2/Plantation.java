package abstraction.eq2Producteur2;
import abstraction.eqXRomu.produits.Feve;
/** @author Paul */
public class Plantation {
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

    public Plantation(Feve typeFeve, int parcelles, int age) {
        super();
        this.typeFeve = typeFeve;
        this.parcelles = parcelles;
        this.age = age; // Plantation récente
        
        // Initialisation des paramètres selon la qualité des fèves
        switch (typeFeve) {
            case F_BQ:
                this.dureeDeVie = 960;  // 40 ans
                this.tempsAvantProduction = 72;  // 3 ans
                this.productionParParcelle = 105 000 ; //  fèves par parcelle a chaque next
                this.prix_achat = ;
                this.prix_vente = ;
                this.prix_replantation = ;
                this.salaire_employe = 1800;
                break;
 
            case F_MQ:
                this.dureeDeVie = 960;
                this.tempsAvantProduction = 72; // 3 ans
                this.productionParParcelle = 85 000;
                this.prix_achat = ;
                this.prix_vente = ;
                this.prix_replantation = ;
                this.salaire_employe = 1800;
                break;

            case F_HQ:
                this.dureeDeVie = 960;
                this.tempsAvantProduction = 72; // 5 ans
                this.productionParParcelle = 63 000;
                this.prix_achat = ;
                this.prix_vente = ;
                this.prix_replantation = ;
                this.salaire_employe = 1800;
                break;

            case F_HQ_E:
                this.dureeDeVie = 960;
                this.tempsAvantProduction = 72; // 5 ans
                this.productionParParcelle = 63 000 ;
                this.prix_achat = ;
                this.prix_vente = ;
                this.prix_replantation = ;
                this.salaire_employe = 1800;
                break;
            
            default:
                throw new IllegalArgumentException("Type de fève non reconnu !");
        }
    }



    // Méthode pour vérifier si la plantation est productive
    public boolean estProductive() {
        return age >= tempsAvantProduction && age < dureeDeVie;
    }




    /**
     * Avance l'âge de la plantation d'un step et renvoie la quantité de fèves produites.
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

        // Calcul de la production en fèves sèches
        double fevesTotales = parcelles * productionParParcelle;

        return fevesTotales;
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

    // Méthode pour calculer la production totale de cette plantation
    public double produire() {
        if (age >= tempsAvantProduction && age < dureeDeVie) {
            return parcelles * productionParParcelle;
        }
        return 0.0;
    }

    // Méthode pour vieillir la plantation d'un step
    public void vieillir() {
        age++;
    }

    // Méthode pour vérifier si la plantation est productive
    public boolean estProductive() {
        return age >= tempsAvantProduction && age < dureeDeVie;
    }

    public String getEtat() {
        if (age < tempsAvantProduction) {
            return "en croissance";
        } 
    
        else if (age >= tempsAvantProduction && age < dureeDeVie) {
            return "en production";
        } 
    
        else if (age >= dureeDeVie) {
            return "mort";
        }

        else return "inconnu";
    }

}