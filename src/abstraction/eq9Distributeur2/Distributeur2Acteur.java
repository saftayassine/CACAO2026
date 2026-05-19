package abstraction.eq9Distributeur2;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import abstraction.eqXRomu.filiere.IMarqueChocolat;
import abstraction.eqXRomu.clients.ClientFinal;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IActeur;
import abstraction.eqXRomu.filiere.IDistributeurChocolatDeMarque;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.IProduit;

public class Distributeur2Acteur implements IActeur, IDistributeurChocolatDeMarque, IMarqueChocolat {
	protected int cryptogramme;
	protected Journal journal;
	protected Journal journalStocks;
	protected Journal journalCC;
	protected Journal journalAO;
	protected Journal journalFinancier;
	protected Map<IProduit, Double> stock;
	protected Variable indicateurStockTotal;
    protected Map<ChocolatDeMarque, Double> prix;
    protected double capaciteRayonKg = 500000.0;
    
    protected EQ9_StrategieFixationPrix strategieFixationPrix;
    

    public static final String NOM_MARQUE = "EQ9";
    protected Variable indicateurMargeMoyenne;
    protected Variable indicateurMixMarquePrivee;
    protected Variable indicateurProfitBrutEtape;

	/**
     * @author Paul Juhel
     */
	public Distributeur2Acteur() {
		this.journal = new Journal("Journal EQ9 - Général", this);
		this.journalStocks = new Journal("Journal EQ9 - Stocks", this);
		this.journalCC = new Journal("Journal EQ9 - Contrats Cadres", this);
		this.journalAO = new Journal("Journal EQ9 - Appels d'offres", this);
		this.journalFinancier = new Journal("Journal EQ9 - Finances", this);
		this.stock = new HashMap<>();
		this.indicateurStockTotal = new Variable("EQ9_stock_total", this, 0.0);
		this.indicateurMargeMoyenne = new Variable("EQ9_marge_moyenne", this, 18.0);
		this.indicateurMixMarquePrivee = new Variable("EQ9_pct_marque_privee", this, 40.0);
		this.indicateurProfitBrutEtape = new Variable("EQ9_profit_brut", this, 0.0);
	}


	// Ajoute 100 tonnes d'un produit en rayon
		/**
         * @author Anass Ouisrani et Paul Juhel
         */
	public void initialiser() {
        this.stock.clear();
        List<ChocolatDeMarque> produits = Filiere.LA_FILIERE.getChocolatsProduits();

        // Initialiser le stock pour TOUS les produits disponibles (pas seulement le premier)
        if (produits != null && !produits.isEmpty()) {
            for (ChocolatDeMarque choco : produits) {
                // Stock initial réaliste : 200 tonnes par produit
                this.stock.put(choco, 200.0); // 200 tonnes = 200 000 kg
            }
        }

        this.indicateurStockTotal.setValeur(this, getStockTotal());

        // Initialisation des prix selon la qualité du chocolat
        this.prix = new HashMap<>();
        
        this.strategieFixationPrix = new EQ9_StrategieFixationPrix(journal);
        
        this.indicateurMargeMoyenne.setValeur(this, 18.0);
        this.indicateurMixMarquePrivee.setValeur(this, 40.0);
        this.indicateurProfitBrutEtape.setValeur(this, 0.0);

        journal.ajouter("Initialisation terminée : " + produits.size() + " produits en stock");
    }

    
        

	public String getNom() {// NE PAS MODIFIER
		return "EQ9";
	}

	public String toString() {// NE PAS MODIFIER
		return this.getNom();
	}

	////////////////////////////////////////////////////////
	//         En lien avec l'interface graphique         //
	////////////////////////////////////////////////////////

		/**
         * @author Paul Juhel
		 * @author Anass Ouisrani
         */
	public void next() {
    int etape = Filiere.LA_FILIERE.getEtape();
    this.journal.ajouter("=== ETAPE " + etape + " ===");
    // Cette méthode est surchargée par Distributeur2AcheteurCC
    // Elle ne doit pas être appelée directement
}
		/** @author Anass Ouisrani*/
	protected double getStockTotal() {
		double total = 0.0;
		for (double q : stock.values()) {
			total += q;
		}
		return total;
	}

	public Color getColor() {// NE PAS MODIFIER
		return new Color(245, 155, 185);
	}

	public String getDescription() {
		return "Distributeur moderne avec stratégie de prix différenciés et gestion optimisée des stocks";
	}

		/**
         * @author Anass Ouisrani
         */
	// Renvoie les indicateurs
	public List<Variable> getIndicateurs() {
		List<Variable> res = new ArrayList<Variable>();
		res.add(indicateurStockTotal);
		res.add(indicateurMargeMoyenne);
		res.add(indicateurMixMarquePrivee);
		res.add(indicateurProfitBrutEtape);
		return res;
	}

	// Renvoie les parametres
	public List<Variable> getParametres() {
		List<Variable> res=new ArrayList<Variable>();
		return res;
	}

	// Renvoie les journaux
		/**
         * @author Paul Juhel
         */
	public List<Journal> getJournaux() {
		List<Journal> res=new ArrayList<Journal>();
		res.add(journal);
		res.add(journalStocks);
		res.add(journalCC);
		res.add(journalAO);
		res.add(journalFinancier);
		return res;
	}

	////////////////////////////////////////////////////////
	//               En lien avec la Banque               //
	////////////////////////////////////////////////////////
    /**
         * @author Paul Juhel et Paul Rossignol
         */
	// Appelee en debut de simulation pour communiquer cryptogramme personnel
	public void setCryptogramme(Integer crypto) {
		this.cryptogramme = crypto;
	}

	// Appelee lorsqu'un acteur fait faillite 
	
	public void notificationFaillite(IActeur acteur) {
		if (acteur != null) {
			journal.ajouter("Information : " + acteur.getNom() + " a fait faillite");
		}
	}

	//Nous informer apres chaque operation sur votre compte bancaire, 
	public void notificationOperationBancaire(double montant) {
		if (montant > 0) {
			journal.ajouter("Crédit bancaire : +" + montant + "€");
		} else {
			journal.ajouter("Débit bancaire : " + montant + "€");
		}
	}

	// Renvoie le solde actuel 
	protected double getSolde() {
		return Filiere.LA_FILIERE.getBanque().getSolde(Filiere.LA_FILIERE.getActeur(getNom()), this.cryptogramme);
	}

	////////////////////////////////////////////////////////
	//        Pour la creation de filieres de test        //
	////////////////////////////////////////////////////////

	// Renvoie la liste des filieres proposees 
	public List<String> getNomsFilieresProposees() {
		ArrayList<String> filieres = new ArrayList<String>();
		return(filieres);
	}

	// Renvoie une instance d'une filiere d'apres son nom
	public Filiere getFiliere(String nom) {
		return Filiere.LA_FILIERE;
	}

	public double getQuantiteEnStock(IProduit p, int cryptogramme) {
        if (this.cryptogramme==cryptogramme) {
            return this.stock.getOrDefault(p, 0.0);
        } else {
            return 0;
        }
    }
	////////////////////////////////////////////////////////
    //         IDistributeurChocolatDeMarque              //
    ////////////////////////////////////////////////////////
	/**
         * @author Anass Ouisrani et Paul Juhel
         */

@Override
    public double prix(ChocolatDeMarque choco) {
    if (!prix.containsKey(choco)) {
        switch (choco.getChocolat()) {
            case C_HQ_E: return 26000.0;
            case C_HQ:   return 22000.0;
            case C_MQ_E: return 18000.0;
            case C_MQ:   return 16000.0;
            case C_BQ_E: return 14000.0;
            case C_BQ:   return 12000.0;
            default:     return 0.0;
        }
    }
    return prix.getOrDefault(choco, 0.0);
}

@Override
    public double quantiteEnVente(ChocolatDeMarque choco, int crypto) {
        if (crypto != this.cryptogramme) {
            this.journal.ajouter("Tentative accès non autorisé quantiteEnVente");
            return 0.0;
        }
        double qStock = this.stock.getOrDefault(choco, 0.0);
        return Math.min(qStock, this.capaciteRayonKg);
    }

@Override
    public double quantiteEnVenteTG(ChocolatDeMarque choco, int crypto) {
        if (crypto != this.cryptogramme) {
            this.journal.ajouter("Tentative accès non autorisé quantiteEnVenteTG");
            return 0.0;
        }
        return quantiteEnVente(choco, crypto) * 0.10;
    }

@Override
    public void vendre(ClientFinal client, ChocolatDeMarque choco, double quantite, double montant, int crypto) {
        if (crypto != this.cryptogramme) {
            this.journal.ajouter("Tentative accès non autorisé vendre");
            return;
        }
        double stockActuel = this.stock.getOrDefault(choco, 0.0);
        if (quantite <= 0 || quantite > stockActuel) {
            this.journal.ajouter("Stock insuffisant pour " + choco.getNom() + ": demandé " + (quantite/1000) + "t, dispo " + (stockActuel/1000) + "t");
            return;
        }
        this.stock.put(choco, stockActuel - quantite);
        this.indicateurStockTotal.setValeur(this, getStockTotal());
        this.journal.ajouter("Vente de " + (quantite/1000) + "t de " + choco.getNom() + " pour " + montant + " €");
}

@Override
    public void notificationRayonVide(ChocolatDeMarque choco, int crypto) {
        if (crypto != this.cryptogramme) return;
        this.journal.ajouter("RUPTURE STOCK : " + choco.getNom() + " - Rayon vide ! Augmenter les achats.");
    }

/**
 * Paie les frais de stockage à chaque étape
 * Coût : 120 €/T (16x le coût producteur de 7.5€/T)
 * @author Anass Ouisrani
 */
protected void payerFraisStockage() {
    double coutParTonne = 120.0; // €/T
    double stockTotalEnTonnes = getStockTotal() / 1000.0;
    double coutTotal = stockTotalEnTonnes * coutParTonne;

    if (coutTotal > 0) {
        Filiere.LA_FILIERE.getBanque().payerCout(
            this,
            this.cryptogramme,
            "Frais de stockage",
            coutTotal
        );
        this.journal.ajouter("Frais de stockage payés : "
            + coutTotal + "€ pour "
            + stockTotalEnTonnes + "t stockées");
    }
}

/**
     * Calcule le prix de maniere dynamique selon la qualité du chocolat
     * 
     * @author Anass Ouisrani
	 */

protected void ajusterPrixDynamiques() {
    int etape = Filiere.LA_FILIERE.getEtape();
    if (etape < 1) return; // Pas de référence à l'étape 0

    List<ChocolatDeMarque> produits = Filiere.LA_FILIERE.getChocolatsProduits();
    if (produits == null || produits.isEmpty()) return;

    double profitBrutTotal = 0;
    double margeTotal = 0;

    for (ChocolatDeMarque choco : produits) {
        // Coût d'achat = prix moyen du marché à l'étape précédente
        double coutAchat = obtenirCoutAchat(choco);

        double stock = this.stock.getOrDefault(choco, 0.0);

        double demande = estimerDemandeClients(choco);

        double prixConcurrent = estimerPrixConcurrent(choco);

        // Calcul prix optimal via la stratégie
        double prixOptimal = strategieFixationPrix.calculerPrixVente(
            coutAchat,
            choco.getNom(),
            stock,
            demande,
            prixConcurrent
        );

        this.prix.put(choco, prixOptimal);
        this.journal.ajouter("Prix ajusté " + choco.getNom() 
            + " : " + prixOptimal + "€/T"
            + " (coût=" + coutAchat + ", demande=" + (demande/1000) + "t)");

        if (stock > 0) {
            profitBrutTotal += (prixOptimal - coutAchat) * (stock / 1000.0);
            margeTotal += ((prixOptimal - coutAchat) / coutAchat) * 100;
        }
    }

    this.indicateurProfitBrutEtape.setValeur(this, profitBrutTotal);
    this.indicateurMargeMoyenne.setValeur(this, margeTotal / produits.size());
}

/**
 * Obtient le coût d'achat pour un produit (estimation)
 */
private double obtenirCoutAchat(ChocolatDeMarque choco) {
    int etape = Filiere.LA_FILIERE.getEtape();
    if (etape < 1) return prix(choco) * 0.75; // estimation par défaut
    
    // Le prix moyen du marché est la meilleure approximation
    // du coût d'achat réel
    double prixMoyen = Filiere.LA_FILIERE.prixMoyen(choco, etape - 1);
    if (prixMoyen > 0) return prixMoyen;
    
    // Fallback : 75% de notre prix de vente
    return prix(choco) * 0.75;
}

/**
 * Estime la demande clients pour un produit (placeholder)
 */
private double estimerDemandeClients(ChocolatDeMarque choco) {
    int etape = Filiere.LA_FILIERE.getEtape();
    if (etape < 1) return 50000.0; // Pas de données : estimation par défaut
    return Filiere.LA_FILIERE.getVentes(choco, etape - 1);
}
/**
 * Estime le prix concurrent direct (placeholder)
 */
private double estimerPrixConcurrent(ChocolatDeMarque choco) {
    int etape = Filiere.LA_FILIERE.getEtape();
    if (etape < 1) return 0;
    
    // Le prix moyen du marché représente le prix concurrent
    return Filiere.LA_FILIERE.prixMoyen(choco, etape - 1);
}

////////////////////////////////////////////////////////
//              IMarqueChocolat                       //
////////////////////////////////////////////////////////
/**
 * @author Anass Ouisrani
 */
@Override
public List<String> getMarquesChocolat() {
    List<String> marques = new ArrayList<>();
    marques.add(NOM_MARQUE);
    return marques;
}
}


