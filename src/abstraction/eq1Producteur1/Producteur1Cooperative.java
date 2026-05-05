package abstraction.eq1Producteur1;
import abstraction.eqXRomu.filiere.Banque;
import abstraction.eqXRomu.filiere.Filiere;
import java.util.HashMap;




public class Producteur1Cooperative extends Producteur1Planteur{
    
    HashMap<String, Double> coopNonEq = new HashMap<>();
    HashMap<String, Double> coopEq = new HashMap<>();
    double pourcentageEnfant = 0.5 ;

    public Producteur1Cooperative(){
        super();
        this.coopEq.put("salaire adultes",2.);
        this.coopEq.put("taille",this.getTaillePlantation(true));


        this.coopNonEq.put("salaire adultes",0.5);
        this.coopNonEq.put("salaire enfants",0.2);

        //initilisation nombre de salariés eq
        this.coopNonEq.put("taille",this.getTaillePlantation(false));

        int nbAdulteEq =  (int) Math.floor(this.getTaillePlantation(true)*30);
        this.coopEq.put( "nombre adultes", (double) nbAdulteEq);


        //initilisation nombre de salariés non eq
        int nbForceNonEq = (int) Math.floor(this.getTaillePlantation(false)*30);
        double prop = 1/(2+ (1-this.pourcentageEnfant)/this.pourcentageEnfant);

        double nbEnfant = (double) (int) Math.floor(prop * nbForceNonEq);
        this.coopNonEq.put("nombre enfants", (double) nbEnfant ) ;
        this.coopNonEq.put( "nombre adultes", (double) nbForceNonEq - nbEnfant*2);

        System.err.println("nb enfant : " + this.coopNonEq.get("nombre enfants"));
        System.err.println("nb adulte non Eq : " + this.coopNonEq.get("nombre adultes"));
        System.err.println("nb adulte Eq : " + this.coopEq.get("nombre adultes"));
    }

    public double getSalaireAdulte(boolean equitable){
        if (equitable) {
            return this.coopEq.get("salaire adultes");
            
        }

        else{
            return this.coopNonEq.get("salaire adultes");
        }
    }

    public double getNombreAdultes(boolean equitable){
        if (equitable) {
            return this.coopEq.get("nombre adultes");
            
        }

        else{
            return this.coopNonEq.get("nombre adultes");
        }
    }

    public double getNombreEnfant(){
        return this.coopNonEq.get("nombre enfants");
    }

    public double getSalaireEnfant(){
        return this.coopEq.get("salaire enfants");
    }

    public double getPourcentageEnfant(){
        return this.pourcentageEnfant;
    }


    public void setPourcentageEnfants(double pourcent){

        if (pourcent <= 0 && pourcent >= 100){
            this.pourcentageEnfant = pourcent;

            //Mise à jour du nombre d'enfants et d'adultes en non Eq
            int nbForceNonEq = (int) Math.floor(this.getTaillePlantation(false)*30);
            double prop = 1/(2+ (1-this.pourcentageEnfant)/this.pourcentageEnfant);

            double nbEnfant = (double) (int) Math.floor(prop * nbForceNonEq);
            this.coopNonEq.put("nombre enfants", (double) nbEnfant ) ;
            this.coopNonEq.put( "nombre adultes", (double) nbForceNonEq - nbEnfant*2);
        }
    }

    public void payerSalaire(){

        double montant = this.coopEq.get("nombre adultes")* this.coopEq.get("salaire adultes")
        + this.coopNonEq.get("nombre adultes")*this.coopNonEq.get("salaire adultes") 
        +this.coopNonEq.get("salaire enfants")*this.coopNonEq.get("nombre enfants");


        Banque banque=Filiere.LA_FILIERE.getBanque();
        banque.payerCout(this, this.cryptogramme, "Masse salariales" , 15 * montant);
        this.journal.ajouter("Salaire payé : " + montant);
    }


    public void setSalaire(boolean adulte, boolean equitable, double salaire){
        if(equitable){
            this.coopEq.put("salaire adultes", salaire);
        }

        else if(adulte){
            this.coopNonEq.put("salaire adultes", salaire);
        }

        else{
            this.coopNonEq.put("salaire enfants", salaire);
        }
    }


    public void next(){

        super.next();
        this.payerSalaire();

    }

}
