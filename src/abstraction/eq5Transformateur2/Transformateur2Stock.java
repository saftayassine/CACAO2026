package abstraction.eq5Transformateur2;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.lang.Integer;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IActeur;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.IProduit;

public class Transformateur2Stock extends Transformateur2Acteur{

    // Attributs
    private HashMap<ChocolatDeMarque, Double> stock_ChocolatDeMarque;
    private HashMap<Feve, Double> stock_feve;
    private HashMap<Chocolat, Double> stock_chocolat;
    private Variable stock_feve_affichage;
    private Variable stock_chocolat_affichage;
    private Variable stock_ChocolatDeMarque_affichage;
    private List<SacDeFeves> sacsHQ;
    private List<SacDeFeves> sacsMQ;
    private List<SacDeFeves> sacsBQ;
    private List<SacDeFeves> sacsHQ_E;
    private List<SacDeFeves> sacsMQ_E;
    private List<SacDeFeves> sacsBQ_E;
    
    // Constructeur

    /** @author Pierre
     * @author Raphaël
     * @author Maxence
    **/ 
    public Transformateur2Stock(){
        this.stock_feve = new HashMap<Feve, Double>();
        this.stock_feve.put(Feve.F_BQ, 0.0);
        this.stock_feve.put(Feve.F_BQ_E, 0.0);
        this.stock_feve.put(Feve.F_MQ, 0.0);
        this.stock_feve.put(Feve.F_MQ_E, 0.0);
        this.stock_feve.put(Feve.F_HQ, 0.0);
        this.stock_feve.put(Feve.F_HQ_E, 0.0);

        this.stock_feve_affichage=new Variable("EQ5 Stock Fève", this, 0.0);

        this.sacsHQ=new LinkedList<SacDeFeves>();
        this.sacsMQ=new LinkedList<SacDeFeves>();
        this.sacsBQ=new LinkedList<SacDeFeves>();
        this.sacsHQ_E=new LinkedList<SacDeFeves>();
        this.sacsMQ_E=new LinkedList<SacDeFeves>();
        this.sacsBQ_E=new LinkedList<SacDeFeves>();

        this.stock_ChocolatDeMarque = new HashMap<ChocolatDeMarque,Double>();
        if(this instanceof Transformateur2FabriquantChocolatDeMarque){
            for (ChocolatDeMarque choco : ((Transformateur2FabriquantChocolatDeMarque)this).getChocolatsProduits()) {
                this.stock_ChocolatDeMarque.put(choco, 0.0);
            }
        }
        
        this.stock_ChocolatDeMarque_affichage=new Variable("EQ5 Stock Chocolat de marque",this,0.0);
    }

    // Méthodes
    
	/** @author Pierre
    **/
	public List<Variable> getIndicateurs() {
		List<Variable> res = super.getIndicateurs();
        res.add(this.stock_feve_affichage);
        res.add(this.stock_ChocolatDeMarque_affichage);
		return res;
	}

    /** @author Pierre
    **/
    public Double getStock_feve_total(){
        return this.stock_feve.get(Feve.F_BQ) + this.stock_feve.get(Feve.F_BQ_E) + this.stock_feve.get(Feve.F_MQ) + this.stock_feve.get(Feve.F_MQ_E) + this.stock_feve.get(Feve.F_HQ) + this.stock_feve.get(Feve.F_HQ_E);
        }
    
    /**
     * @author Maxence
     */
    public Double getStock_feve(IProduit q){
        return this.stock_feve.get(q);
    }

    /** @author Pierre et Maxence
     * 
    **/
    public void add_feve(Double n, Feve q){
        assert n >= 0;
        this.stock_feve.put(q, this.stock_feve.get(q) + n);
        this.getJournaux().get(1).ajouter("Ajout de" + (n).toString()+ "de fève de qualité" + (q).toString() + "\n");
        this.stock_feve_affichage.ajouter(this,n);
        
        int etape = Filiere.LA_FILIERE.getEtape();
        if(q==Feve.F_HQ){
            SacDeFeves sac=new SacDeFeves(q,n,etape+6);
            this.sacsHQ.add(sac);
            }
        if(q==Feve.F_HQ_E){
            SacDeFeves sac=new SacDeFeves(q,n,etape+6);
            this.sacsHQ_E.add(sac);
            }
        if(q==Feve.F_MQ){
            SacDeFeves sac=new SacDeFeves(q,n,etape+12);
            this.sacsMQ.add(sac);
            }
        if(q==Feve.F_MQ_E){
            SacDeFeves sac=new SacDeFeves(q,n,etape+12);
            this.sacsMQ_E.add(sac);
            }
        if(q==Feve.F_BQ){
            SacDeFeves sac=new SacDeFeves(q,n,etape+24);
            this.sacsBQ.add(sac);
        }
        else{
            SacDeFeves sac=new SacDeFeves(q,n,etape+24);
            this.sacsBQ_E.add(sac);
        }

    }

    /** @author Pierre et maxence
    **/
    public void remove_feve(Double n, Feve q){
        assert n >= 0;
        if (n <= this.stock_feve.get(q)){
            this.stock_feve.put(q, this.stock_feve.get(q) - n);
            this.getJournaux().get(1).ajouter("Déstockage de" + (n).toString()+ "de fève de qualité" + (q).toString() + "\n");
            this.stock_feve_affichage.retirer(this,n);

            if(q==Feve.F_HQ){
                Double resteAEnlever=n;
                while (resteAEnlever>0.0) {
                    resteAEnlever=this.sacsHQ.get(0).remove_feve(resteAEnlever);
                    if(this.sacsHQ.get(0).getQuantite()==0.0){
                        this.sacsHQ.remove(0);
                    }
                }
                }
            if(q==Feve.F_HQ_E){
                Double resteAEnlever=n;
                while (resteAEnlever>0.0) {
                    resteAEnlever=this.sacsHQ_E.get(0).remove_feve(resteAEnlever);
                    if(this.sacsHQ_E.get(0).getQuantite()==0.0){
                        this.sacsHQ_E.remove(0);
                    }
                }
            }
            if(q==Feve.F_MQ){
                Double resteAEnlever=n;
                while (resteAEnlever>0.0) {
                    resteAEnlever=this.sacsMQ.get(0).remove_feve(resteAEnlever);
                    if(this.sacsMQ.get(0).getQuantite()==0.0){
                        this.sacsMQ.remove(0);
                    }
                }
            }
            if(q==Feve.F_MQ_E){
                Double resteAEnlever=n;
                while (resteAEnlever>0.0) {
                    resteAEnlever=this.sacsMQ_E.get(0).remove_feve(resteAEnlever);
                    if(this.sacsMQ_E.get(0).getQuantite()==0.0){
                        this.sacsMQ_E.remove(0);
                    }
                }
            }
            if(q==Feve.F_BQ){
                Double resteAEnlever=n;
                while (resteAEnlever>0.0) {
                    resteAEnlever=this.sacsBQ.get(0).remove_feve(resteAEnlever);
                    if(this.sacsBQ.get(0).getQuantite()==0.0){
                        this.sacsBQ.remove(0);
                    }
                }
            }
            else{
                Double resteAEnlever=n;
                while (resteAEnlever>0.0) {
                    resteAEnlever=this.sacsBQ_E.get(0).remove_feve(resteAEnlever);
                    if(this.sacsBQ_E.get(0).getQuantite()==0.0){
                        this.sacsBQ_E.remove(0);
                    }
                }
            }
        }       
    }

        /**@author Maxence
        **/
        public void update_peremption(){
            int etape = Filiere.LA_FILIERE.getEtape();
            while(this.sacsHQ.get(0).getDatePeremption()==etape){
                SacDeFeves sac=this.sacsHQ.remove(0);
                SacDeFeves newSac=new SacDeFeves(Feve.F_MQ, sac.getQuantite());
                this.sacsMQ.add(newSac);
            }
            while(this.sacsHQ_E.get(0).getDatePeremption()==etape){
                SacDeFeves sac=this.sacsHQ_E.remove(0);
                SacDeFeves newSac=new SacDeFeves(Feve.F_MQ_E, sac.getQuantite());
                this.sacsMQ_E.add(newSac);
            }
            while(this.sacsMQ.get(0).getDatePeremption()==etape){
                SacDeFeves sac=this.sacsMQ.remove(0);
                SacDeFeves newSac=new SacDeFeves(Feve.F_BQ, sac.getQuantite());
                this.sacsBQ.add(newSac);
            }
            while(this.sacsMQ_E.get(0).getDatePeremption()==etape){
                SacDeFeves sac=this.sacsMQ_E.remove(0);
                SacDeFeves newSac=new SacDeFeves(Feve.F_BQ_E, sac.getQuantite());
                this.sacsBQ_E.add(newSac);
            }
            while(this.sacsBQ.get(0).getDatePeremption()==etape){
                this.sacsBQ.remove(0);
            }
            while(this.sacsBQ_E.get(0).getDatePeremption()==etape){
                this.sacsBQ_E.remove(0);
            }
        }

       /**@author Maxence */
    public void add_chocolatDeMarque(ChocolatDeMarque choco, Double quantite){
        Double stockActuel = this.getStock_chocolatDeMarque(choco);
        this.stock_ChocolatDeMarque.put(choco, stockActuel + quantite);
        this.stock_ChocolatDeMarque_affichage.ajouter(this, quantite);
        this.getJournaux().get(2).ajouter("Ajout de " + quantite.toString() + " de chocolat de marque " + choco.getNom() + "\n");
    }

    /**@author Maxence */
    public void remove_chocolatDeMarque(ChocolatDeMarque choco, Double n){
        assert n >= 0;
        Double stockActuel = this.getStock_chocolatDeMarque(choco);
        if (n <= stockActuel){
            this.stock_ChocolatDeMarque.put(choco, stockActuel - n); 
            this.getJournaux().get(2).ajouter("Déstockage de " + n.toString() + " de chocolat de marque " + choco.getNom() + "\n");
            this.stock_ChocolatDeMarque_affichage.retirer(this, n);
        }
    }

    /**@author Maxence */
    public Double getStock_chocolatDeMarque(ChocolatDeMarque choco){
        if (this.stock_ChocolatDeMarque.containsKey(choco)) {
            return this.stock_ChocolatDeMarque.get(choco);
        }
        return 0.0;
    }

}
