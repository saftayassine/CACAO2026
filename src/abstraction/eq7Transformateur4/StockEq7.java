package abstraction.eq7Transformateur4;

import java.util.ArrayList;
import java.util.Collections;

import abstraction.eqXRomu.filiere.IActeur;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.produits.Gamme;

public class StockEq7 {
    /*
    Cette classe implémente un stock de fèves dynamique où on peut ajouter des fèves, en retirer. La gestion de la baisse
    naturelle de la qualité de fèves se gère automatiquement.
    Auter -> Matteo
    */
    private Variable LowQ; // Stock baisse qualité
    private Variable MedQ; // Stock moyenne qualité
    private Variable HighQ; // Stock Haute qualité
    private IActeur createur;
    private ArrayList<StockActEq7> actions; //Liste des actions à réaliser visant à modéliser la baisse naturelle de la qualité des fèves

    public StockEq7(IActeur acteur){
        this.createur=acteur;
        this.LowQ = new Variable("LowQ", "Stock des fèves de faible qualité", acteur, 0.,9999999.,0.);
        this.MedQ = new Variable("MedQ", "Stock des fèves de moyenne qualité", acteur, 0.,9999999.,0.);
        this.HighQ= new Variable("HighQ", "Stock des fèves de haute qualité", acteur, 0.,9999999.,0.);
        this.actions=new ArrayList<>();
    }

    public IActeur getCreateur(){
        return this.createur;
    }

    public double getLowQ(){
        return this.LowQ.getValeur();
    }
    
    public double getMedQ(){
        return this.MedQ.getValeur();
    }

    public double getHighQ(){
        return this.HighQ.getValeur();
    }

    public String toString(){
        return "Nom du propriétaire du stock : " + this.getCreateur().getNom() + ". Stock de basse qualité : " + Double.toString(this.getLowQ()) + ". Stock de moyenne qualité : " + Double.toString(this.getMedQ()) + ". Stock de haute qualité : " + Double.toString(this.getHighQ())  ;
    }

    public void AddLowQ(double valeur){
        this.actions.add(new StockActEq7(24,Gamme.BQ, valeur));
        this.LowQ.ajouter(this.createur, valeur);
    }

    public void AddMedQ(double valeur){
        this.actions.add(new StockActEq7(12,Gamme.MQ, valeur));
        this.MedQ.ajouter(this.createur, valeur);
    }

    public void AddHighQ(double valeur){
        this.actions.add(new StockActEq7(6,Gamme.HQ, valeur));
        this.HighQ.ajouter(this.createur, valeur);
    }


    public void add(double valeur, Gamme quality){
        //Traite la qualité
        if (quality==Gamme.HQ){
            this.AddHighQ(valeur);
        }
        else{
            if(quality==Gamme.MQ){
                this.AddMedQ(valeur);
            }
            else{
                this.AddLowQ(valeur);
            }
        }
    }

    //Enlève dans la file d'action la quantité quantity avec la qualité quality
    public void RemoveAction(Gamme quality,double quantity){
        int i=0;
        while (quantity>0.){
            if (this.actions.get(i).getGamme()==quality){
                if (this.actions.get(i).getQuantity()<quantity){
                    quantity=quantity - this.actions.get(i).getQuantity();
                    this.actions.remove(i);
                }
                else{
                    this.actions.get(i).RemoveQuant(quantity);
                    quantity=0.;
                }
            }
            else{
                i++;
            }
        }
    }

    public void RemoveLowQ(double valeur){
        if (valeur<this.getLowQ()){
            this.LowQ.retirer(this.createur, valeur);
            this.RemoveAction(Gamme.BQ, valeur);
        }
        else {
            System.out.println("Stock de fèves BQ insuffisant");
        }
    }

    public void RemoveMedQ(double valeur){
        if (valeur<this.getMedQ()){
            this.MedQ.retirer(this.createur, valeur);
            this.RemoveAction(Gamme.MQ, valeur);
        }
        else{
            System.out.println("Stock de fèves MQ insuffisant");
        }
    }


    public void RemoveHighQ(double valeur){
        if (valeur<this.getHighQ()){
        this.HighQ.retirer(this.createur, valeur);
        this.RemoveAction(Gamme.HQ, valeur);
        }
        else{
            System.out.println("Stock de fèves HQ insuffisant");
        }
    }
    public void remove(double valeur, Gamme quality){
        //Traite la qualité
        if (quality==Gamme.HQ){
            this.RemoveHighQ(valeur);
        }
        else{
            if(quality==Gamme.MQ){
                this.RemoveMedQ(valeur);
            }
            else{
                this.RemoveLowQ(valeur);
            }
        }
    }


    public void SetLowQ(double valeur){
        this.LowQ.setValeur(this.createur, valeur);
    }

    public void SetMedQ(double valeur){
        this.MedQ.setValeur(this.createur, valeur);
    }

    public void SetHighQ(double valeur){
        this.HighQ.setValeur(this.createur, valeur);
    }

    public void next(){
        //On décrémente les timers de chaque action et 
        for (int i=0; i<this.actions.size(); i++){
            this.actions.get(i).next();
        } 
        int i=0;
        Collections.sort(this.actions);
        //Pour chaque action dont le timer est à 0, on réalise les modifications nécessaires
        while ((i<this.actions.size() && this.actions.get(i).getTimer()<=0)){
            StockActEq7 action= this.actions.get(i);
            if (action.isHighQ()){
                this.HighQ.retirer(this.createur, action.getQuantity());
                this.MedQ.ajouter(this.createur, action.getQuantity());
                this.actions.add(new StockActEq7(12,Gamme.MQ, action.getQuantity()));
                this.actions.remove(i);
            }
            else{
                if (action.isMedQ()){
                    this.MedQ.retirer(this.createur, action.getQuantity());
                    this.LowQ.ajouter(this.createur, action.getQuantity());
                    this.actions.add(new StockActEq7(24,Gamme.BQ, action.getQuantity()));
                    this.actions.remove(i);
                }
                else{
                    this.LowQ.retirer(this.createur, action.getQuantity());
                    this.actions.remove(i);
                }
            }
        }
    }
    /* public static void main(String[] args) {
        IActeur acteur = new Transformateur4Acteur();
        StockEq7 stock = new StockEq7(acteur);
        stock.add(90.,Gamme.MQ);
        System.out.println(stock.actions.toString());
        System.out.println(stock.toString());

        stock.next();
        stock.add(80.,Gamme.MQ);
        System.out.println(stock.toString());

        stock.next();
        stock.RemoveMedQ(40.);
        System.out.println(stock.actions.toString());
        System.out.println(stock.toString());

        stock.next();
        stock.RemoveMedQ( 70.);
        System.out.println(stock.actions.toString());
        System.out.println(stock.toString());

        
        for (int i=0; i<6; i++){
            stock.next();
        }
        System.out.println(stock.toString());
        System.out.println(stock.actions.toString());
        for (int i=0; i<28; i++){
            stock.next();
        }
        stock.RemoveHighQ(50.);
        System.out.println(stock.toString());
        System.out.println(stock.actions.toString());
    }  */
        

}
