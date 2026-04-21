package abstraction.eq5Transformateur2;

/** @author Pierre */
public class Transformateur2Production extends Transformateur2FabriquantChocolatDeMarque{
    
    // Attributs
    private int Machine;
    private int Employé;
    private Double Encours;


    public Transformateur2Production(){
        super();
        this.Machine = 12;
        this.Employé = 1000;
        this.Encours = 0.0;
    }

    public void addEncours(Double n){
        if (this.Occupation(n)){
            this.Encours += n;
        }
    }

    public boolean AssezMachine(){
        return (this.Machine * 90 >= this.Employé);
    }
    public boolean Occupation(Double n){
        return (this.Encours + n <= this.Employé * 8.4 & this.AssezMachine());
    }
}
