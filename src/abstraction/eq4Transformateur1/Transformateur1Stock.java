/**@author Ewan Lefort */

package abstraction.eq4Transformateur1;

import java.util.*;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.IProduit;
import abstraction.eqXRomu.acteurs.Romu;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IFabricantChocolatDeMarque;
import abstraction.eqXRomu.filiere.IMarqueChocolat;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;

public class Transformateur1Stock extends Transformateur1Acteur implements IFabricantChocolatDeMarque, IMarqueChocolat{

    private HashMap<IProduit, Double> stock;
    private HashMap<IProduit, Double> stockPrévu;
    public double ChocoProduit;
    public ChocolatDeMarque ProntellaM= new ChocolatDeMarque(Chocolat.C_MQ, "Prontella", 65);
    public ChocolatDeMarque ProntellaB= new ChocolatDeMarque(Chocolat.C_BQ, "Prontella", 50);
    public ChocolatDeMarque ProntellaH= new ChocolatDeMarque(Chocolat.C_HQ, "Prontella", 65);
    public ChocolatDeMarque ProntellaBE= new ChocolatDeMarque(Chocolat.C_BQ_E, "Prontella", 50);
    public ChocolatDeMarque ProntellaHE= new ChocolatDeMarque(Chocolat.C_HQ_E, "Prontella", 65);
    private HashMap<Integer, List<Double>> PeremptionProntellaB= new HashMap<Integer, List<Double>>();
    private HashMap<Integer, List<Double>> PeremptionProntellaM= new HashMap<Integer, List<Double>>();
    private HashMap<Integer, List<Double>> PeremptionProntellaH= new HashMap<Integer, List<Double>>();
    private HashMap<Integer, List<Double>> PeremptionProntellaBE= new HashMap<Integer, List<Double>>();
    private HashMap<Integer, List<Double>> PeremptionProntellaHE= new HashMap<Integer, List<Double>>();
    private int NumLotMinB=0;
    private int NumLotMinM=0;
    private int NumLotMinH=0;
    private int NumLotMinBE=0;
    private int NumLotMinHE=0;
    private int NumLotMaxB=0;
    private int NumLotMaxM=0;   
    private int NumLotMaxH=0;
    private int NumLotMaxBE=0;  
    private int NumLotMaxHE=0;

    public Transformateur1Stock(){
        super();
        this.stock=new HashMap<IProduit, Double>();
        this.stockPrévu=new HashMap<IProduit, Double>();
        this.ChocoProduit=0;
        this.PeremptionProntellaB.put(0, new ArrayList<Double>());
        this.PeremptionProntellaM.put(0, new ArrayList<Double>());  
        this.PeremptionProntellaH.put(0, new ArrayList<Double>());
        this.PeremptionProntellaBE.put(0, new ArrayList<Double>());
        this.PeremptionProntellaHE.put(0, new ArrayList<Double>());

    }
    public List<ChocolatDeMarque> getChocolatsProduits(){
		List<ChocolatDeMarque> ListeChoco=new ArrayList<ChocolatDeMarque>();
		ListeChoco.add(ProntellaM);
		ListeChoco.add(ProntellaB);
		ListeChoco.add(ProntellaH);
		ListeChoco.add(ProntellaBE);
		ListeChoco.add(ProntellaHE);
        
		return ListeChoco;
	}
	public List<String> getMarquesChocolat(){
		List<String> ListeNoms= new ArrayList<String>();
		ListeNoms.add("Prontella");
		return ListeNoms;
	}
    public void initialiser(){
        this.stock.put(Feve.F_BQ,0.0);
        this.stock.put(Feve.F_MQ,0.0);
        this.stock.put(Feve.F_HQ,0.0);
        this.stock.put(Feve.F_BQ_E,0.0);
        this.stock.put(Feve.F_MQ_E,0.0);
        this.stock.put(Feve.F_HQ_E,0.0);
        this.stock.put(Chocolat.C_BQ,0.0);
        this.stock.put(Chocolat.C_MQ,0.0);
        this.stock.put(Chocolat.C_HQ,0.0);
        this.stock.put(Chocolat.C_BQ_E,0.0);
        this.stock.put(Chocolat.C_MQ_E,0.0);
        this.stock.put(Chocolat.C_HQ_E,0.0);
        this.stock.put(ProntellaM,0.0);
        this.stock.put(ProntellaB,0.0);
        this.stock.put(ProntellaH,0.0);
        this.stock.put(ProntellaBE,0.0);
        this.stock.put(ProntellaHE,0.0);
        this.stockPrévu.put(ProntellaB,0.0);
        this.stockPrévu.put(ProntellaM,0.0);
        this.stockPrévu.put(ProntellaBE,0.0);
        this.stockPrévu.put(ProntellaH,0.0);
        this.stockPrévu.put(ProntellaHE,0.0);
        this.stockPrévu.put(Chocolat.C_BQ,0.0);
        this.stockPrévu.put(Chocolat.C_BQ_E,0.0);
        this.stockPrévu.put(Chocolat.C_MQ,0.0);
        this.stockPrévu.put(Chocolat.C_MQ_E,0.0);
        this.stockPrévu.put(Chocolat.C_HQ,0.0);
        this.stockPrévu.put(Chocolat.C_HQ_E,0.0);


    }

    public HashMap<IProduit, Double> getStock(){
        return this.stock;
    }

    public HashMap<IProduit, Double> getStockPrevu(){
        return this.stockPrévu;
    }

    public IProduit getChoco(IProduit f){
        if (f instanceof Feve){
        if (f==Feve.F_BQ){
            return ProntellaB;
        }
        else if (f==Feve.F_MQ){
            return ProntellaM;
        }
        else if (f==Feve.F_HQ){
            return ProntellaH;
        }
        else if (f==Feve.F_BQ_E){
            return ProntellaBE;
        }
        else if (f==Feve.F_HQ_E){
            return ProntellaHE;
        }
        else{
            return null;
        }}
        else{
            return null;
        }
    }


    public double getStocksProduit(IProduit produit){
        if (this.getStock().keySet().contains(produit)){
        return this.getStock().get(produit);
    }
        else{
            return 0;
        }
    }

    public double getStocksPrevuProduit(IProduit produit){
        if (this.getStockPrevu().keySet().contains(produit)){
        return this.getStockPrevu().get(produit);
    }
        else{
            return 0;
        }
    }

    public double getTotalStocksFeves(){
        double totalstock=0;
        for (IProduit feve: stock.keySet()){
            if (feve.getType()=="Feve"){
            totalstock+=this.getStocksProduit(feve);
            }
        }
        return totalstock;
    }

    public double getTotalStocksChoco(){
        double totalstock=0;
        for (IProduit choco: stock.keySet()){
            if (choco.getType()=="Chocolat"){
            totalstock+=this.getStocksProduit(choco);
            }
        }
        return totalstock;
    }

    public double getTotalStocksPrevuChoco(){
        double totalstock=0;
        for (IProduit choco: stockPrévu.keySet()){
            if (choco.getType()=="Chocolat"){
            totalstock+=this.getStocksPrevuProduit(choco);
            }
        }
        return totalstock;
    }

    public double getTotalStocksChocoMarque(){
        double totalstock=0;
        for (IProduit choco: stock.keySet()){
            if (choco.getType()=="ChocolatDeMarque"){
            totalstock+=this.getStocksProduit(choco);
        }
        }
        return totalstock;
    }

    public double getTotalStocksPrevuChocoMarque(){
        double totalstock=0;
        for (IProduit choco: stockPrévu.keySet()){
            if (choco.getType()=="ChocolatDeMarque"){
            totalstock+=this.getStocksPrevuProduit(choco);
        }
        }
        return totalstock;
    }

    public double getTotalStocks(){
        return this.getTotalStocksChoco()+this.getTotalStocksFeves()+this.getTotalStocksChocoMarque();
    }

    public double getTotalStocksPrevu(){
        return this.getTotalStocksPrevuChoco()+this.getTotalStocksFeves()+this.getTotalStocksPrevuChocoMarque();
    }
   
    public void setStocksProduit(IProduit p, double QuantiteEnT){
        if (this.getStock().containsKey(p)){
        this.getStock().put(p,QuantiteEnT);
    }
    }

    public void setStocksPrevuProduit(IProduit p, double QuantiteEnT){
        if (this.getStockPrevu().containsKey(p)){
        this.getStockPrevu().put(p,QuantiteEnT);
    }
    }

    public void addPeremption(double quantite, IProduit p){
        if (p.equals(ProntellaB)){
            List<Double> infoPeremption= new ArrayList<Double>();
            infoPeremption.add((double)Filiere.LA_FILIERE.getEtape()+18);
            infoPeremption.add(quantite*2);
            this.PeremptionProntellaB.put(this.NumLotMaxB, infoPeremption);
            this.NumLotMaxB++;
        }
        else if (p.equals(ProntellaM)){
            List<Double> infoPeremption= new ArrayList<Double>();
            infoPeremption.add((double)Filiere.LA_FILIERE.getEtape()+24);
            infoPeremption.add(quantite*(1/0.65));
            this.PeremptionProntellaM.put(this.NumLotMaxM, infoPeremption);
            this.NumLotMaxM++;
        }
        else if (p.equals(ProntellaH)){
            List<Double> infoPeremption= new ArrayList<Double>();
            infoPeremption.add((double)Filiere.LA_FILIERE.getEtape()+48);
            infoPeremption.add(quantite*(1/0.65));
            this.PeremptionProntellaH.put(this.NumLotMaxH, infoPeremption);
            this.NumLotMaxH++;
        }
        else if (p.equals(ProntellaBE)){
            List<Double> infoPeremption= new ArrayList<Double>();
            infoPeremption.add((double)Filiere.LA_FILIERE.getEtape()+18);
            infoPeremption.add(quantite*2);
            this.PeremptionProntellaBE.put(this.NumLotMaxBE, infoPeremption);
            this.NumLotMaxBE++;
        }
        else if (p.equals(ProntellaHE)){
            List<Double> infoPeremption= new ArrayList<Double>();
            infoPeremption.add((double)Filiere.LA_FILIERE.getEtape()+48);
            infoPeremption.add(quantite*(1/0.65));
            this.PeremptionProntellaHE.put(this.NumLotMaxHE, infoPeremption);
            this.NumLotMaxHE++;
        }
    }

    public Integer getNumLotMin(IProduit p){
        if (p.equals(ProntellaB)){
            return this.NumLotMinB;
        }
        else if (p.equals(ProntellaM)){
            return this.NumLotMinM;
        }
        else if (p.equals(ProntellaH)){
            return this.NumLotMinH;
        }
        else if (p.equals(ProntellaBE)){
            return this.NumLotMinBE;
        }
        else if (p.equals(ProntellaHE)){
            return this.NumLotMinHE;
        }
        else{
            return null;
        }
    }

    public Integer getNumLotMax(IProduit p){
        if (p.equals(ProntellaB)){
            return this.NumLotMaxB;
        }
        else if (p.equals(ProntellaM)){
            return this.NumLotMaxM;
        }
        else if (p.equals(ProntellaH)){
            return this.NumLotMaxH;
        }
        else if (p.equals(ProntellaBE)){
            return this.NumLotMaxBE;
        }
        else if (p.equals(ProntellaHE)){
            return this.NumLotMaxHE;
        }
        else{
            return null;
        }
    }

    public Integer setNumLotMin(IProduit p, int numLot){
        if (p.equals(ProntellaB)){
            return numLot;
        }
        else if (p.equals(ProntellaM)){
            return numLot;
        }
        else if (p.equals(ProntellaH)){
            return numLot;
        }
        else if (p.equals(ProntellaBE)){
            return numLot;
        }
        else if (p.equals(ProntellaHE)){
            return numLot;
        }
        else{
            return null;
        }
    }

    public void Vente(IProduit p, double quantite){
        if (p.equals(ProntellaB)){
            Set<Integer> numLots= this.PeremptionProntellaB.keySet();
            if(!numLots.isEmpty()){
            Integer numLot= Collections.min(numLots);   
             Double Q=this.PeremptionProntellaB.get(numLot).get(1);
             if (quantite>=Q){
                this.PeremptionProntellaB.remove(numLot);
                this.setNumLotMin(p, numLot);
                Vente(p, quantite-Q);
             }
             else{
                double Qrestante=this.PeremptionProntellaB.get(numLot).get(1)-quantite;
                List<Double> infoPeremption= new ArrayList<Double>();
                infoPeremption.add(this.PeremptionProntellaB.get(numLot).get(0));
                infoPeremption.add(Qrestante);
                this.PeremptionProntellaB.put(numLot, infoPeremption);
             }}
        }
        else if (p.equals(ProntellaM)){
            Set<Integer> numLots= this.PeremptionProntellaM.keySet();
            if(!numLots.isEmpty()){
            Integer numLot= Collections.min(numLots);   
             Double Q=this.PeremptionProntellaM.get(numLot).get(1);
             if (quantite>=Q){
                this.PeremptionProntellaM.remove(numLot);
                this.setNumLotMin(p, numLot);
                Vente(p, quantite-Q);
             }
             else{
                double Qrestante=this.PeremptionProntellaM.get(numLot).get(1)-quantite;
                List<Double> infoPeremption= new ArrayList<Double>();
                infoPeremption.add(this.PeremptionProntellaM.get(numLot).get(0));
                infoPeremption.add(Qrestante);
                this.PeremptionProntellaM.put(numLot, infoPeremption);
             }}
        }
        else if (p.equals(ProntellaH)){
            Set<Integer> numLots= this.PeremptionProntellaH.keySet();
            if(!numLots.isEmpty()){
            Integer numLot= Collections.min(numLots);   
             Double Q=this.PeremptionProntellaH.get(numLot).get(1);
             if (quantite>=Q){  
                this.PeremptionProntellaH.remove(numLot);
                this.setNumLotMin(p, numLot);
                Vente(p, quantite-Q);
             }
             else{
                double Qrestante=this.PeremptionProntellaH.get(numLot).get(1)-quantite;
                List<Double> infoPeremption= new ArrayList<Double>();
                infoPeremption.add(this.PeremptionProntellaH.get(numLot).get(0));
                infoPeremption.add(Qrestante);
                this.PeremptionProntellaH.put(numLot, infoPeremption);
             }}
        }
        else if (p.equals(ProntellaBE)){
            Set<Integer> numLots= this.PeremptionProntellaBE.keySet();
            if(!numLots.isEmpty()){
            Integer numLot= Collections.min(numLots);   
            Double Q=this.PeremptionProntellaBE.get(numLot).get(1);
            if (quantite>=Q){
                this.PeremptionProntellaBE.remove(numLot);
                this.setNumLotMin(p, numLot);
                Vente(p, quantite-Q);
            }
            else{
                double Qrestante=this.PeremptionProntellaBE.get(numLot).get(1)-quantite;
                List<Double> infoPeremption= new ArrayList<Double>();
                infoPeremption.add(this.PeremptionProntellaBE.get(numLot).get(0));
                infoPeremption.add(Qrestante);
                this.PeremptionProntellaBE.put(numLot, infoPeremption);
            }}
        }
        
        else if (p.equals(ProntellaHE)){
            Set<Integer> numLots= this.PeremptionProntellaHE.keySet();
            if(!numLots.isEmpty()){
            Integer numLot= Collections.min(numLots);   
            Double Q=this.PeremptionProntellaHE.get(numLot).get(1);
            if (quantite>=Q){
                this.PeremptionProntellaHE.remove(numLot);
                this.setNumLotMin(p, numLot);
                Vente(p, quantite-Q);
            }
            else{
                double Qrestante=this.PeremptionProntellaHE.get(numLot).get(1)-quantite;
                List<Double> infoPeremption= new ArrayList<Double>();
                infoPeremption.add(this.PeremptionProntellaHE.get(numLot).get(0));
                infoPeremption.add(Qrestante);
                this.PeremptionProntellaHE.put(numLot, infoPeremption);
            }}
        }
    }

    public Double getFirstPeremptionDouble (IProduit p){
        if (p.equals(ProntellaB) && !this.PeremptionProntellaB.isEmpty()){
            return this.PeremptionProntellaB.get(this.NumLotMinB).get(0);
        }
        else if (p.equals(ProntellaM) && !this.PeremptionProntellaM.isEmpty()){
            return this.PeremptionProntellaM.get(this.NumLotMinM).get(0);
        }
        else if (p.equals(ProntellaH) && !this.PeremptionProntellaH.isEmpty()){
            return this.PeremptionProntellaH.get(this.NumLotMinH).get(0);
        }
        else if (p.equals(ProntellaBE) && !this.PeremptionProntellaBE.isEmpty()){
            return this.PeremptionProntellaBE.get(this.NumLotMinBE).get(0);
        }
        else if (p.equals(ProntellaHE) && !this.PeremptionProntellaHE.isEmpty()){
            return this.PeremptionProntellaHE.get(this.NumLotMinHE).get(0);
        }
        else{
            return null;
        }
    }


    public void next(){
        super.next();
        
        double F_BQ_ATransfo= this.getStocksProduit(Feve.F_BQ);
        double F_BQ_E_ATransfo= this.getStocksProduit(Feve.F_BQ_E);
        double F_MQ_ATransfo= this.getStocksProduit(Feve.F_MQ);
        double F_HQ_ATransfo= this.getStocksProduit(Feve.F_HQ);
        double F_HQ_E_ATransfo= this.getStocksProduit(Feve.F_HQ_E);
        double ChocoBObtenu= F_BQ_ATransfo/0.50;
        double ChocoBEObtenu= F_BQ_E_ATransfo/0.50;
        double ChocoMObtenu= F_MQ_ATransfo/0.65;
        double ChocoHObtenu= F_HQ_ATransfo/0.65;
        double ChocoHEObtenu= F_HQ_E_ATransfo/0.65;
        this.ChocoProduit=ChocoBObtenu+ChocoBEObtenu+ChocoMObtenu+ChocoHObtenu+ChocoHEObtenu;  
        this.setStocksProduit(Feve.F_BQ, this.getStocksProduit(Feve.F_BQ)-F_BQ_ATransfo);
        this.setStocksProduit(ProntellaB, this.getStocksProduit(ProntellaB)+ChocoBObtenu);
        this.setStocksPrevuProduit(ProntellaB, this.getStocksPrevuProduit(ProntellaB)+ChocoBObtenu);
        this.setStocksProduit(Feve.F_BQ_E, this.getStocksProduit(Feve.F_BQ_E)-F_BQ_E_ATransfo);
        this.setStocksProduit(ProntellaBE, this.getStocksProduit(ProntellaBE)+ChocoBEObtenu);
        this.setStocksPrevuProduit(ProntellaBE, this.getStocksPrevuProduit(ProntellaBE)+ChocoBEObtenu);
        this.setStocksProduit(Feve.F_MQ, this.getStocksProduit(Feve.F_MQ)-F_MQ_ATransfo);
        this.setStocksProduit(ProntellaM, this.getStocksProduit(ProntellaM)+ChocoMObtenu);
        this.setStocksPrevuProduit(ProntellaM, this.getStocksPrevuProduit(ProntellaM)+ChocoMObtenu);
        this.setStocksProduit(Feve.F_HQ, this.getStocksProduit(Feve.F_HQ)-F_HQ_ATransfo);
        this.setStocksProduit(ProntellaH, this.getStocksProduit(ProntellaH)+ChocoHObtenu);
        this.setStocksPrevuProduit(ProntellaH, this.getStocksPrevuProduit(ProntellaH)+ChocoHObtenu);
        this.setStocksProduit(Feve.F_HQ_E, this.getStocksProduit(Feve.F_HQ_E)-F_HQ_E_ATransfo);
        this.setStocksProduit(ProntellaHE, this.getStocksProduit(ProntellaHE)+ChocoHEObtenu);
        this.setStocksPrevuProduit(ProntellaHE, this.getStocksPrevuProduit(ProntellaHE)+ChocoHEObtenu);

        if (!this.PeremptionProntellaB.isEmpty()){
            if (!this.PeremptionProntellaB.get(Collections.min(this.PeremptionProntellaB.keySet())).isEmpty()){
                while (!this.PeremptionProntellaB.isEmpty() && this.PeremptionProntellaB.get(Collections.min(this.PeremptionProntellaB.keySet())).get(0)<=Filiere.LA_FILIERE.getEtape()){
                    double quantitePerimee= PeremptionProntellaB.get(Collections.min(this.PeremptionProntellaB.keySet())).get(1);
                    this.setStocksProduit(ProntellaB, this.getStocksProduit(ProntellaB)-quantitePerimee);
                    this.setStocksPrevuProduit(ProntellaB, this.getStocksPrevuProduit(ProntellaB)-quantitePerimee);
                    this.PeremptionProntellaB.remove(Collections.min(this.PeremptionProntellaB.keySet()));
        }
        }
    }

        if (!this.PeremptionProntellaBE.isEmpty()){
            if (!this.PeremptionProntellaBE.get(Collections.min(this.PeremptionProntellaBE.keySet())).isEmpty()){
        while (!this.PeremptionProntellaBE.isEmpty() && this.PeremptionProntellaBE.get(Collections.min(this.PeremptionProntellaBE.keySet())).get(0)<=Filiere.LA_FILIERE.getEtape()){
            double quantitePerimee= PeremptionProntellaBE.get(Collections.min(this.PeremptionProntellaBE.keySet())).get(1);
            this.setStocksProduit(ProntellaBE, this.getStocksProduit(ProntellaBE)-quantitePerimee);
            this.setStocksPrevuProduit(ProntellaBE, this.getStocksPrevuProduit(ProntellaBE)-quantitePerimee);
            this.PeremptionProntellaBE.remove(Collections.min(this.PeremptionProntellaBE.keySet()));
    }
    }
}
        if (!this.PeremptionProntellaM.isEmpty()){
            if (!this.PeremptionProntellaM.get(Collections.min(this.PeremptionProntellaM.keySet())).isEmpty()){
        while (!this.PeremptionProntellaM.isEmpty() && this.PeremptionProntellaM.get(Collections.min(this.PeremptionProntellaM.keySet())).get(0)<=Filiere.LA_FILIERE.getEtape()){
            double quantitePerimee= PeremptionProntellaM.get(Collections.min(this.PeremptionProntellaM.keySet())).get(1);
            this.setStocksProduit(ProntellaM, this.getStocksProduit(ProntellaM)-quantitePerimee);
            this.setStocksPrevuProduit(ProntellaM, this.getStocksPrevuProduit(ProntellaM)-quantitePerimee);
            this.PeremptionProntellaM.remove(Collections.min(this.PeremptionProntellaM.keySet()));
            // NumLotMinM++;
        }}
        }
        if (!this.PeremptionProntellaH.isEmpty()){
            if (!this.PeremptionProntellaH.get(Collections.min(this.PeremptionProntellaH.keySet())).isEmpty()){
        while (!this.PeremptionProntellaH.isEmpty() && this.PeremptionProntellaH.get(Collections.min(this.PeremptionProntellaH.keySet())).get(0)<=Filiere.LA_FILIERE.getEtape()){
            double quantitePerimee= PeremptionProntellaH.get(Collections.min(this.PeremptionProntellaH.keySet())).get(1);
            this.setStocksProduit(ProntellaH, this.getStocksProduit(ProntellaH)-quantitePerimee);
            this.setStocksPrevuProduit(ProntellaH, this.getStocksPrevuProduit(ProntellaH)-quantitePerimee);
            this.PeremptionProntellaH.remove(Collections.min(this.PeremptionProntellaH.keySet()));
            // NumLotMinH++;
        }}}
        if (!this.PeremptionProntellaHE.isEmpty()){
            if (!this.PeremptionProntellaHE.get(Collections.min(this.PeremptionProntellaHE.keySet())).isEmpty()){
        while (!this.PeremptionProntellaHE.isEmpty() && this.PeremptionProntellaHE.get(Collections.min(this.PeremptionProntellaHE.keySet())).get(0)<=Filiere.LA_FILIERE.getEtape()){
            double quantitePerimee= PeremptionProntellaHE.get(Collections.min(this.PeremptionProntellaHE.keySet())).get(1);
            this.setStocksProduit(ProntellaHE, this.getStocksProduit(ProntellaHE)-quantitePerimee);
            this.setStocksPrevuProduit(ProntellaHE, this.getStocksPrevuProduit(ProntellaHE)-quantitePerimee);
            this.PeremptionProntellaHE.remove(Collections.min(this.PeremptionProntellaHE.keySet()));
            // NumLotMinHE++;
        }}}
    }
}