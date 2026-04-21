package abstraction.eq9Distributeur2;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import abstraction.eqXRomu.clients.ClientFinal;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IActeur;
import abstraction.eqXRomu.filiere.IDistributeurChocolatDeMarque;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.IProduit;

public class Distributeur2Acteur implements IActeur, IDistributeurChocolatDeMarque {
	protected int cryptogramme;
	protected Journal journal;
	protected Map<IProduit, Double> stock;
	protected Variable indicateurStockTotal;
	protected Map<ChocolatDeMarque, Double> prixParProduit;
    protected Map<ChocolatDeMarque, Double> prix;
    protected double capaciteRayonKg = 500000.0;

	/**
     * @author Paul Juhel
     */
	public Distributeur2Acteur() {
		this.journal = new Journal("Journal EQ9", this);
		this.stock = new HashMap<>();
		this.indicateurStockTotal = new Variable("EQ9_stock_total", this, 0.0);
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
                this.stock.put(choco, 200000.0); // 200 tonnes = 200 000 kg
            }
        }

        this.indicateurStockTotal.setValeur(this, getStockTotal());

        // Initialisation des prix selon la qualité du chocolat
        this.prix = new HashMap<>();

        journal.ajouter("Initialisation terminée : " + produits.size() + " produits en stock");
    }

    /**
     * Calcule le prix selon la qualité du chocolat
     * 
     * @author Anass Ouisrani
	 */

        

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

		List<ChocolatDeMarque> produits = Filiere.LA_FILIERE.getChocolatsProduits();

		// Gérer TOUS les produits (pas seulement le premier)
		if (produits != null && !produits.isEmpty()) {
			for (ChocolatDeMarque choco : produits) {
				// Réapprovisionnement réaliste : +5 tonnes par produit par étape
				double quantiteActuelle = this.stock.getOrDefault(choco, 0.0);
				double ajout = 5000.0; // 5 tonnes = 5000 kg
				this.stock.put(choco, quantiteActuelle + ajout);

				journal.ajouter("Réapprovisionnement " + choco.getNom() + " : +" + (ajout/1000) + " tonnes");
			}
		}

		// Mettre à jour l'indicateur de stock total
		this.indicateurStockTotal.setValeur(this, getStockTotal());

		// Log du stock total
		journal.ajouter("Stock total : " + (getStockTotal()/1000) + " tonnes");
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
    return prix.get(choco);
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
     * Analyse du retour sur investissement des labels – V1 .
     * On achète un produit labellisé seulement si la marge couvre
     * les coûts de certification (avec une petite marge de sécurité).
     * @author Paul Rossignol
     */
    public static class AnalyseROILabelV1 {
        private static final double MARGE_SECURITE = 1.10; // ex : demander 10% de plus que le coût

        /**
         * @param produit          produit concerné 
         * @param prixAchat        prix d'achat du produit labellisé
         * @param prixVente        prix de vente au client final
         * @param coutCertification coût additionnel lié au label (par unité)
         * @param attractivite     facteur d'attractivité (>1 si le label permet de mieux vendre)
         * @return true si l'achat du label est jugé rentable
         */
        public boolean acheterLabel(ChocolatDeMarque produit,
                                    double prixAchat,
                                    double prixVente,
                                    double coutCertification,
                                    double attractivite) {
            if (produit == null || Filiere.LA_FILIERE == null) {
                return false;
            }
            if (prixAchat < 0.0 || prixVente <= 0.0 || coutCertification < 0.0) {
                return false;
            }
            if (attractivite <= 0.0) {
                attractivite = 1.0;
            }

            double margeUnitaire = (prixVente - prixAchat) * attractivite;
            double coutTotal = coutCertification * MARGE_SECURITE;

            return margeUnitaire >= coutTotal;
        }
    }
}
