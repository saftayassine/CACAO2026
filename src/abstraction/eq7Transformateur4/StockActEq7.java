package abstraction.eq7Transformateur4;

import abstraction.eqXRomu.produits.Gamme;

public class StockActEq7 implements Comparable<StockActEq7> {
    /*
    Cette classe vise à modéliser et automatiser la diminution naturelle de la qualité des fèves.
    Une action a trois attributs : 
    - int timer : nombre de next avant la baisse de qualité
    - Gamme quality : qualité des fèves
    - double quantity : quantité de fève

    Exemple : 
    Lors de l'achat d'une quantité N de fèves HQ, on représente cet achat par l'action (6, HQ, N).
    En effet, après l'achat, les N tonnes de fèves HQ passent en MQ au bout de 3 mois soit 6 next. 
    A chaque next, le compteur timer est décrémenté de 1 et lorsque ce dernier arrive à 0, on procède au changement de quality.
    Dans ce cas, au 6e next après l'achat, on enlève N tonnes du stock HQ et on rajoute N tonnes du stock MQ

    Auteur -> Matteo
    */
    private int timer; 
    private Gamme quality;
    private double quantity;
    public StockActEq7(Gamme gamme, double valeur){
        if (gamme==Gamme.HQ){
            new StockActEq7(6,gamme,valeur);
        }
        else {
            if (gamme==Gamme.MQ){
                new StockActEq7(12, gamme, valeur);
            }
            else {
                new StockActEq7(24, gamme, valeur);
            }
        }
    }
    public StockActEq7(int time_init, Gamme gamme, double valeur){
        this.timer=time_init;
        this.quality=gamme;
        this.quantity = valeur;
    }

    

    public int getTimer(){
        return this.timer;
    }

    public Gamme getGamme(){
        return this.quality;
    }

    public double getQuantity(){
        return this.quantity;
    }
    
    public boolean isHighQ(){
        return this.quality==Gamme.HQ;
    }

    public boolean isMedQ(){
        return this.quality==Gamme.MQ;
    }

    public boolean isLowQ(){
        return this.quality == Gamme.BQ;
    }

    public void RemoveQuant(double valeur){
        this.quantity=this.quantity - valeur;
    }
    public String toString(){
        return "Timer : " + Integer.toString(this.timer) + ", Quantité : " + Double.toString(this.quantity); 
    }

    public int compareTo(StockActEq7 o) {
        if (this.timer<o.timer){
            return -1;
        }
        else{
            if (this.timer > o.timer){
                return 1;
            }
            else{
                return 0;
            }
        }
    }

    public void next(){
        this.timer=this.timer - 1;
    }
}
