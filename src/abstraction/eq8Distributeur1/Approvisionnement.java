package abstraction.eq8Distributeur1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import abstraction.eqXRomu.appelDOffre.AppelDOffre;
import abstraction.eqXRomu.appelDOffre.IAcheteurAO;
import abstraction.eqXRomu.appelDOffre.OffreVente;
import abstraction.eqXRomu.appelDOffre.SuperviseurVentesAO;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Gamme;
import abstraction.eqXRomu.produits.IProduit;

/** @author Ewen Landron */
/** @author Lucas Levillain */

public class Approvisionnement extends ChocolatDistributeur1 {

  //  protected Map<ChocolatDeMarque, Double> prixDAchat;
    protected Map<ChocolatDeMarque, Double> stockPredit;
    private Map<String, List<ChocolatDeMarque>> classements;
    protected List<ExemplaireContratCadre> mesContrats;
    protected double pourcentBQ, pourcentBQ_E, pourcentMQ, pourcentMQ_E, pourcentHQ, pourcentHQ_E;
    protected Map<ChocolatDeMarque, Double> stockPreditTG;

    public Approvisionnement() {
        super();
        //this.prixDAchat = new HashMap<>();
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
                //System.out.println("init "+cdm+" a "+prixMoyenPondere+" €/T basé sur les contrats cadres");
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

        // --- ÉTAPE 1 : TOURNÉE DES CONTRATS CADRES (CC) ---
        double besoinRestantPourCC = besoinCategorie;

        for (int i = 0; i < liste.size(); i++) {
            ChocolatDeMarque actuel = liste.get(i);
            
            // PROTECTION CONCURRENT : On n'achète pas la marque du distributeur concurrent eq9
            if (actuel.getMarque() != null && actuel.getMarque().contains("eq9Distributeur2")) {
                this.journal5.ajouter("CC Refusé - Produit du concurrent boycotté : " + actuel.getMarque());
                continue; 
            }

            double prixCible = this.prixDAchat.getOrDefault(actuel, 1000.0);
            double prixMax = prixCible * 1.3;

            if (i < liste.size() - 1) {
                prixMax = this.prixDAchat.getOrDefault(liste.get(i + 1), prixMax);
            }

            // Logique Tête de Gondole (TG)
            boolean demandeTG = false;
            double quantiteAcheterCC = besoinRestantPourCC;
            
            if (actuel.isEquitable()) {
                double capaciteMaxTG = this.TailleRayon * 0.1;
                double occupationActuelleTG = getQuantiteTotaleTG();
                double placeRestanteTG = getPlaceRestanteTG();

                if (occupationActuelleTG < (capaciteMaxTG * 0.8)) {
                    demandeTG = true;
                    if (besoinRestantPourCC > placeRestanteTG) {
                        quantiteAcheterCC = Math.max(0, placeRestanteTG);
                    }
                }
            }

            // On ne tente le Contrat Cadre que s'il y a un volume pertinent
            if (quantiteAcheterCC > 0.1) {
                // Déclenchement exclusif du Contrat Cadre
                this.methodeIntermediaireAchatCC(actuel, quantiteAcheterCC, prixCible, prixMax, demandeTG);
                
                // On met à jour le besoin de la catégorie en soustrayant ce qu'on vient de tenter d'acheter en CC
                besoinRestantPourCC = Math.max(0, besoinRestantPourCC - quantiteAcheterCC);
            }
        }

        // --- ÉTAPE 2 : TOURNÉE DES APPELS D'OFFRE (AO) ---
        // On repart du besoin mis à jour après la phase des Contrats Cadres
        double besoinRestantPourAO = besoinRestantPourCC; 

        for (int i = 0; i < liste.size(); i++) {
            // Si le besoin a été totalement comblé par les contrats cadres, on arrête
            if (besoinRestantPourAO <= 0.1) break;

            ChocolatDeMarque actuel = liste.get(i);
            
            // PROTECTION CONCURRENT : Même blocage pour la phase d'Appels d'Offres
            if (actuel.getMarque() != null && actuel.getMarque().contains("eq9Distributeur2")) {
                continue; 
            }

            double prixCible = this.prixDAchat.getOrDefault(actuel, 1000.0);
            double prixMax = prixCible * 1.3;

            if (i < liste.size() - 1) {
                prixMax = this.prixDAchat.getOrDefault(liste.get(i + 1), prixMax);
            }

            // Recalcul de la logique TG pour la phase AO (les stocks TG ayant pu évoluer)
            boolean demandeTG = false;
            double quantiteAcheterAO = besoinRestantPourAO;
            
            if (actuel.isEquitable()) {
                double capaciteMaxTG = this.TailleRayon * 0.1;
                double occupationActuelleTG = getQuantiteTotaleTG();
                double placeRestanteTG = getPlaceRestanteTG();

                if (occupationActuelleTG < (capaciteMaxTG * 0.8)) {
                    demandeTG = true;
                    if (besoinRestantPourAO > placeRestanteTG) {
                        quantiteAcheterAO = Math.max(0, placeRestanteTG);
                    }
                }
            }

            // Déclenchement exclusif de l'Appel d'Offre
            if (quantiteAcheterAO > 0.1) {
                this.methodeIntermediaireAchatAO(actuel, quantiteAcheterAO, prixCible, prixMax, demandeTG);
                
                // On déduit également le volume pour les prochains chocolats de la liste s'il y en a
                besoinRestantPourAO = Math.max(0, besoinRestantPourAO - quantiteAcheterAO);
            }
        }
    }

    protected void methodeIntermediaireAchatCC(ChocolatDeMarque cdm, double besoin, double prixCible, double prixMax, boolean TG) {
        // Sera surchargée dans ContratCadre2 pour lancer sup.demandeAcheteur(...)
    }

    /** @author Lucas Levillain (Adapté pour exécution isolée de l'AO) */
    protected void methodeIntermediaireAchatAO(ChocolatDeMarque cdm, double besoin, double prixCible, double prixMax, boolean TG) {
        if (besoin < AppelDOffre.AO_QUANTITE_MIN) return;

        // Récupération du superviseur AO via son nom dans la filière
        SuperviseurVentesAO superviseur = (SuperviseurVentesAO) Filiere.LA_FILIERE.getActeur("Sup.AO");

        if (superviseur == null) {
            this.journal5.ajouter("ERREUR : SuperviseurVentesAO introuvable");
            return;
        }

        OffreVente retenue = superviseur.acheterParAO(
            (IAcheteurAO) this,
            this.cryptogramme,
            cdm,
            besoin,
            false
        );

        if (retenue != null) {
            this.journal3.ajouter(
                "AO retenu : " + retenue.getQuantiteT() + " T de " + cdm
                + " à " + retenue.getPrixT() + " €/T"
                + (TG ? " [TG]" : "")
            );
        } else {
            this.journal3.ajouter("AO sans résultat pour " + cdm);
        }
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
                int etapeActuelle = Filiere.LA_FILIERE.getEtape() + 1;
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