package abstraction.eq3Producteur3;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.Gamme;

public class Arbre_Hectare extends Producteur3VendeurBourse{
    /** @author Victor Vannier-Moreau */
    private int age;
    private Feve feve;
    private int nb_arbre;

    public Arbre_Hectare(Feve feve){
        this.age = 0; //donné en période 
        this.feve = feve;
        this.nb_arbre = 1000;
    }

    public int getAge(){
        return this.age;
    }

    public Feve getFeve(){
        return this.feve;
    }

    public int getProductionCabosse(){
        if (this.age < 72 || this.age > 960 ) { 
            return 0; // arbre de moins de 3 ans ou plus de 40 ars, il ne produit pas de fève.
        }
        
        // Cas 3 à 5 ans
        if (this.age < 120) {
            return 10 * this.nb_arbre;
        }
        
        // Cas 5 à 25 ans
        else if (this.age < 600) {
            return 50 * this.nb_arbre;
        }
        
        // Cas 25 à 40 ans 
        else {
            return 25 * this.nb_arbre;
        }   
    }

    public int getProductionFeve(){
        int productionCabosse = this.getProductionCabosse();
        if (this.feve.getGamme() == Gamme.BQ){
            return 50*productionCabosse;
        }

        else if (this.feve.getGamme() == Gamme.MQ){
            return 40*productionCabosse;
        }

        else {
            return 30*productionCabosse;
        }
    }

    public void ageIncr(){
        this.age++;
    }

}
