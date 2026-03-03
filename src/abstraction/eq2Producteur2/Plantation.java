package abstraction.eq2Producteur2;
import abstraction.eqXRomu.produits.Feve;
//PAUL DELACOUR
public class Plantation {
    private Feve typeFeve;          // Type de fèves cultivées
    private int parcelles;          // Nombre de parcelles de 100 ha
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
                this.productionParParcelle = 105000 ; //  fèves par parcelle a chaque next
                this.prix_achat = 0;
                this.prix_vente = 0;
                this.prix_replantation = 0;
                this.salaire_employe = 0;
                break;
 
            case F_MQ:
                this.dureeDeVie = 960;
                this.tempsAvantProduction = 72; // 3 ans
                this.productionParParcelle = 85000;
                this.prix_achat = 0;
                this.prix_vente = 0;
                this.prix_replantation =0 ;
                this.salaire_employe = 0;
                break;

            case F_HQ:
                this.dureeDeVie = 960;
                this.tempsAvantProduction = 72; // 5 ans
                this.productionParParcelle = 63000;
                this.prix_achat = 0;
                this.prix_vente = 0;
                this.prix_replantation = 0;
                this.salaire_employe =0 ;
                break;

            case F_HQ_E:
                this.dureeDeVie = 960;
                this.tempsAvantProduction = 72; // 5 ans
                this.productionParParcelle = 0;
                this.prix_achat = 0;
                this.prix_vente = 0;
                this.prix_replantation = 0;
                this.salaire_employe = 0;
                break;
            
            default:
                throw new IllegalArgumentException("Type de fève non reconnu !");
        }
    }

}
