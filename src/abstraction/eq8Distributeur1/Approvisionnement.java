package abstraction.eq8Distributeur1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Gamme;
import abstraction.eqXRomu.produits.IProduit;

/** @author Ewen Landron */
public class Approvisionnement extends ChocolatDistributeur1 {

    protected Map<ChocolatDeMarque, Double> prixDAchat;
    protected Map<ChocolatDeMarque, Double> stockPredit;
    private Map<String, List<ChocolatDeMarque>> classements;
    protected List<ExemplaireContratCadre> mesContrats;
    protected double pourcentBQ, pourcentBQ_E, pourcentMQ, pourcentMQ_E, pourcentHQ, pourcentHQ_E;
    protected Map<ChocolatDeMarque, Double> stockPreditTG;

    public Approvisionnement() {
        super();
        this.prixDAchat = new HashMap<>();
        this.classements = new HashMap<>();
        this.classements.put("BQ", new ArrayList<>());
        this.classements.put("BQ_EQUITABLE", new ArrayList<>());
        this.classements.put("MQ", new ArrayList<>());
        this.classements.put("MQ_EQUITABLE", new ArrayList<>());
        this.classements.put("HQ", new ArrayList<>());
        this.classements.put("HQ_EQUITABLE", new ArrayList<>());
    
        this.mesContrats = new ArrayList<>();
        this.stockPredit = new HashMap<>();
        this.stockPreditTG = new HashMap<>();

        // Initialisation des pourcentages de répartition (Total = 1.0)
        this.pourcentBQ = 0.30;   // 30% Bas de gamme standard
        this.pourcentBQ_E = 0.1166; // 11.66%  Bas de gamme équitable
        this.pourcentMQ = 0.2166;   // 21.66% Milieu de gamme standard
        this.pourcentMQ_E = 0.0633; // 6.33% Milieu de gamme équitable
        this.pourcentHQ = 0.20;   // 20% Haut de gamme standard
        this.pourcentHQ_E = 0.1033; // 10.33% Haut de gamme équitable
    }

    /**
    * Calcule le prix d'achat moyen pondéré par les quantités pour chaque chocolat de marque
    * en se basant sur les contrats cadres en cours.
    */
    protected void actualiserPrixDachatParContrats() {
        for (ChocolatDeMarque cdm : Filiere.LA_FILIERE.getChocolatsProduits()) {
            double quantiteTotale = 0;
            double coutTotal = 0;

            for (ExemplaireContratCadre contrat : this.mesContrats) {
                if (contrat.getProduit().equals(cdm)) {
                    // On récupère la quantité totale restant à livrer ou livrée dans ce contrat
                    double qteContrat = contrat.getQuantiteTotale();
                    quantiteTotale += qteContrat;
                    coutTotal += qteContrat * contrat.getPrix();
                }
            }

            if (quantiteTotale > 0) {
                double prixMoyenPondere = coutTotal / quantiteTotale;
                this.prixDAchat.put(cdm, prixMoyenPondere);
            }
        }
        // Si aucun contrat n'existe, on garde la valeur précédente (initialisée par le ClientFinal)
    }
    

    /**
     * Répartit les chocolats de la filière dans les 6 listes et les trie par prix de vente croissant
     */
    public void trierChocolatsParPrix() {
        for (List<ChocolatDeMarque> liste : classements.values()) {
            liste.clear();
        }

        List<ChocolatDeMarque> tousLesChocolats = Filiere.LA_FILIERE.getChocolatsProduits();

        for (ChocolatDeMarque cdm : tousLesChocolats) {
            String cle = genererCle(cdm);
            if (classements.containsKey(cle)) {
                classements.get(cle).add(cdm);
            }
        }

        for (List<ChocolatDeMarque> liste : classements.values()) {
            Collections.sort(liste, new Comparator<ChocolatDeMarque>() {
                public int compare(ChocolatDeMarque c1, ChocolatDeMarque c2) {
                    double prix1 = prixDAchat.getOrDefault(c1, 1000.0);
                    double prix2 = prixDAchat.getOrDefault(c2, 1000.0);
                    return Double.compare(prix1, prix2);
                }
            });
        }
    }

    private String genererCle(ChocolatDeMarque cdm) {
        String gamme = cdm.getGamme().toString();
        return cdm.isEquitable() ? gamme + "_EQUITABLE" : gamme;
    }

    public List<ChocolatDeMarque> getListeTriee(Gamme gamme, boolean equitable) {
        String cle = gamme.toString() + (equitable ? "_EQUITABLE" : "");
        return this.classements.getOrDefault(cle, new ArrayList<>());
    }

    public void lancerApprovisionnementGeneral(double volumeCibleTotal) {

        // Calcul des cibles pour les 6 catégories
        acheterParCategorie(Gamme.BQ, false, volumeCibleTotal * this.pourcentBQ);
        acheterParCategorie(Gamme.BQ, true,  volumeCibleTotal * this.pourcentBQ_E);
    
        acheterParCategorie(Gamme.MQ, false, volumeCibleTotal * this.pourcentMQ);
        acheterParCategorie(Gamme.MQ, true,  volumeCibleTotal * this.pourcentMQ_E);
    
        acheterParCategorie(Gamme.HQ, false, volumeCibleTotal * this.pourcentHQ);
        acheterParCategorie(Gamme.HQ, true,  volumeCibleTotal * this.pourcentHQ_E);
    }

    private void acheterParCategorie(Gamme gamme, boolean equitable, double volumeCibleCategorie) {
        List<ChocolatDeMarque> produits = getListeTriee(gamme, equitable);
    
        // On calcule le stock spécifique à cette catégorie (ex: MQ équitable uniquement)
        double stockActuelCategorie = calculerStockCategorie(gamme, equitable);

        if (stockActuelCategorie < volumeCibleCategorie) {
            double besoinGamme = volumeCibleCategorie - stockActuelCategorie;
        
            // On répartit ce besoin entre les différents chocolats de la liste (triés par prix)
            parcourirEtAcheter(produits, besoinGamme);
        }
    }

    private double calculerStockCategorie(Gamme gamme, boolean equitable) {
        double total = 0;
        for (Map.Entry<ChocolatDeMarque, Double> entry : stockPredit.entrySet()) {
            ChocolatDeMarque cdm = entry.getKey();
            if (cdm.getGamme() == gamme && cdm.isEquitable() == equitable) {
                total += entry.getValue();
            }
        }
        return total;
    }

    private void parcourirEtAcheter(List<ChocolatDeMarque> liste, double besoinCategorie) {
        if (liste.isEmpty()) return;

        for (int i = 0; i < liste.size(); i++) {
            ChocolatDeMarque actuel = liste.get(i);
            double prixCible = this.prixDAchat.getOrDefault(actuel, 15.0);
            double prixMax = prixCible * 1.3;

            if (i < liste.size() - 1) {
                prixMax = this.prixDAchat.getOrDefault(liste.get(i + 1), prixMax);
            }

            // --- NOUVELLE LOGIQUE TÊTE DE GONDOLE (TG) ---
            boolean demandeTG = false;
            double quantiteAcheter = besoinCategorie;
            
            // 1. On ne demande la TG que pour les produits équitables
            if (actuel.isEquitable()) {
                double capaciteMaxTG = this.TailleRayon * 0.1;
                double occupationActuelleTG = getQuantiteTotaleTG();
                double placeRestanteTG = getPlaceRestanteTG();

                // 2. Condition de seuil : on n'initie de TG que si moins de 80% de l'espace TG est pris
                if (occupationActuelleTG < (capaciteMaxTG * 0.8)) {
                    demandeTG = true;
                    
                    // 3. Sécurité de volume : si le besoin dépasse la place restante, on plafonne
                    if (besoinCategorie > placeRestanteTG) {
                        quantiteAcheter = Math.max(0, placeRestanteTG);
                    }
                }
            }

            // Si après plafonnement il reste quelque chose à acheter, on lance le contrat
            if (quantiteAcheter > 0.1) {
                methodeIntermediaireAchat(actuel, quantiteAcheter, prixCible, prixMax, demandeTG);
            }
        }
    }

    protected void methodeIntermediaireAchat(ChocolatDeMarque cdm, double besoin, double prixCible, double prixMax, boolean TG) {
        // Sera surchargée
    }

    /**
     * Correction : Création d'une nouvelle Map pour le stock prédit afin d'éviter
     * de modifier le stock réel pendant les simulations de calcul.
     */
    protected Map<ChocolatDeMarque, Double> initialiserStockPredit() {
        Map<ChocolatDeMarque, Double> predit = new HashMap<>();
        this.ChocolatsAchetes = new HashMap<>(); 
        this.stockPreditTG = new HashMap<>(); // Initialisation du dictionnaire TG
        
        int etapeActuelle = Filiere.LA_FILIERE.getEtape() + 1;

        // 1. Initialisation à 0 pour tous les chocolats de la filière
        for (ChocolatDeMarque cdm : Filiere.LA_FILIERE.getChocolatsProduits()) {
            predit.put(cdm, this.getQuantiteEnStock(cdm, this.cryptogramme));
            this.ChocolatsAchetes.put(cdm, 0.0);
            this.stockPreditTG.put(cdm, 0.0);
        }

        // 2. Prise en compte des contrats CADRES EXISTANTS (Livraisons + TG)
        for (ExemplaireContratCadre contrat : this.mesContrats) {
            IProduit p = (IProduit) contrat.getProduit();
            if (p instanceof ChocolatDeMarque) {
                ChocolatDeMarque cdm = (ChocolatDeMarque) p;
                double quantiteAttendue = contrat.getEcheancier().getQuantite(etapeActuelle);
                
                // Mise à jour Stock Predit global
                predit.put(cdm, predit.get(cdm) + quantiteAttendue);
                
                // Suivi des achats du tour
                this.ChocolatsAchetes.put(cdm, this.ChocolatsAchetes.get(cdm) + quantiteAttendue);
                
                // Suivi spécifique des TG si le contrat le stipule
                if (contrat.getTeteGondole()) {
                    this.stockPreditTG.put(cdm, this.stockPreditTG.get(cdm) + quantiteAttendue);
                }
            }
        }
        return predit;
    }

    protected void actualiserStockPredit(ExemplaireContratCadre nouveauContrat) {
        if (nouveauContrat != null) {
            IProduit p = (IProduit) nouveauContrat.getProduit();
            if (p instanceof ChocolatDeMarque) {
                ChocolatDeMarque cdm = (ChocolatDeMarque) p;
                int etapeActuelle = Filiere.LA_FILIERE.getEtape();
                double livraisonImmediate = nouveauContrat.getEcheancier().getQuantite(etapeActuelle);
            
                // 1. Stock Predit Global
                this.stockPredit.put(cdm, this.stockPredit.getOrDefault(cdm, 0.0) + livraisonImmediate);
                
                // 2. Suivi des achats
                this.ChocolatsAchetes.put(cdm, this.ChocolatsAchetes.getOrDefault(cdm, 0.0) + livraisonImmediate);
                
                // 3. Suivi TG
                if (nouveauContrat.getTeteGondole()) {
                    this.stockPreditTG.put(cdm, this.stockPreditTG.getOrDefault(cdm, 0.0) + livraisonImmediate);
                }
            }
        }
    }

    public double getQuantiteTotaleTG() {
        // Sécurité : si le dictionnaire n'est même pas encore instancié
        if (this.stockPreditTG == null) {
            return 0.0;
        }
        
        double total = 0;
        for (Double qte : this.stockPreditTG.values()) {
            // Sécurité : on vérifie que la valeur stockée n'est pas nulle
            if (qte != null) {
                total += qte;
            }
        }
        return total;
    }

    public double getPlaceRestanteTG() {
        return (this.TailleRayon * 0.1) - getQuantiteTotaleTG();
    }
}