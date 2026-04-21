package abstraction.eq1Producteur1;
import abstraction.eqXRomu.filiere.Banque;
import abstraction.eqXRomu.filiere.Filiere;
import java.util.HashMap;




public class Producteur1Cooperative extends Producteur1Planteur{
    
    HashMap<String, Double> coopNonEq = new HashMap<>();
    HashMap<String, Double> coopEq = new HashMap<>();

    public Producteur1Cooperative(){
        this.coopEq.put("salaire adultes",2.);
        this.coopEq.put("taille",this.getTaillePlantation(true));


        this.coopNonEq.put("salaire adultes",0.5);
        this.coopNonEq.put("salaire enfants",0.2);
        this.coopNonEq.put("taille",this.getTaillePlantation(false));

        int nbAdulteEq =  (int) Math.floor(this.getTaillePlantation(true)*30);
        this.coopEq.put( "nombre adultes", (double) nbAdulteEq);

        int nbAdulteNonEq = (int) Math.floor(this.getTaillePlantation(false)*30);
        this.coopNonEq.put("nombre enfants", (double) nbAdulteNonEq ) ;
        this.coopNonEq.put( "nombre adultes", (double) (int) Math.floor(0.5 * nbAdulteNonEq));

        
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
        return this.coopEq.get("nombre enfants");
    }

    public double getSalaireEnfant(){
        return this.coopEq.get("salaire enfants");
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
