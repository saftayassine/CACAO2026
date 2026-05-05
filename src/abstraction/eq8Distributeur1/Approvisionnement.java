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
public class Approvisionnement extends Distributeur1Acteur {

    protected Map<ChocolatDeMarque, Double> prixDAchat;
    private Map<ChocolatDeMarque, Double> stockPredit;
    private Map<String, List<ChocolatDeMarque>> classements;
    protected List<ExemplaireContratCadre> mesContrats;
    protected double pourcentBQ, pourcentBQ_E, pourcentMQ, pourcentMQ_E, pourcentHQ, pourcentHQ_E;

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

        // Initialisation des pourcentages de répartition (Total = 1.0)
        this.pourcentBQ = 0.15;   // 15% Bas de gamme standard
        this.pourcentBQ_E = 0.05; // 5%  Bas de gamme équitable
        this.pourcentMQ = 0.35;   // 35% Milieu de gamme standard
        this.pourcentMQ_E = 0.10; // 10% Milieu de gamme équitable
        this.pourcentHQ = 0.20;   // 20% Haut de gamme standard
        this.pourcentHQ_E = 0.15; // 15% Haut de gamme équitable
    }

    /**
     * Initialise les prix avec les données historiques (1 an en arrière)
     */
    protected void initialiserPrixReferenceUniquementChocolats() {
        // On récupère directement la liste des chocolats de marque enregistrés dans la filière
        for (ChocolatDeMarque cdm : Filiere.LA_FILIERE.getChocolatsProduits()) {
        
            int etapeActuelle = Filiere.LA_FILIERE.getEtape();
            int etapeCible = Math.max(0, etapeActuelle - 1);
            
            double prixRef = Filiere.LA_FILIERE.prixMoyen(cdm, etapeCible);
            
            if (prixRef <= 0) {
                // Initialisation par défaut selon la gamme
                switch (cdm.getGamme()) {
                    case HQ: prixRef = 15.0; break;
                    case MQ: prixRef = 10.0; break;
                    case BQ: prixRef = 6.0;  break;
                    default: prixRef = 8.0;
                }
            }
            this.prixDAchat.put(cdm, prixRef);
        }
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
        this.stockPredit = initialiserStockPredit();

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
        // Si on a plusieurs marques dans la même catégorie, on divise le besoin par le nombre de marques
        // pour ne pas tout acheter chez le premier (ou on peut tout mettre sur le moins cher)
        if (liste.isEmpty()) return;

        for (int i = 0; i < liste.size(); i++) {
            ChocolatDeMarque actuel = liste.get(i);
            double prixCible = this.prixDAchat.getOrDefault(actuel, 15.0);
        
            double prixMax = prixCible * 1.3; // Marge de négo de 30%
            if (i < liste.size() - 1) {
                prixMax = this.prixDAchat.getOrDefault(liste.get(i + 1), prixMax);
            }

            // On tente d'acheter une fraction du besoin pour cette marque
            // Ici, on est agressif : on demande tout le besoin au moins cher, 
            // ce qui restera sera demandé au suivant au tour d'après si le contrat échoue.
            methodeIntermediaireAchat(actuel, besoinCategorie, prixCible, prixMax);
        }
    }

    protected void methodeIntermediaireAchat(ChocolatDeMarque cdm, double besoin, double prixCible, double prixMax) {
    // Vide ici, implémentée dans ContratCadre
    }

    /**
     * Correction : Création d'une nouvelle Map pour le stock prédit afin d'éviter
     * de modifier le stock réel pendant les simulations de calcul.
     */
    private Map<ChocolatDeMarque, Double> initialiserStockPredit() {
        Map<ChocolatDeMarque, Double> predit = new HashMap<>();
        // On réinitialise aussi notre suivi des achats pour ce tour
        this.ChocolatsAchetes = new HashMap<>(); 
    
        int etapeActuelle = Filiere.LA_FILIERE.getEtape();

        // 1. Stock physique actuel (uniquement pour le stock prédit)
        for (IProduit p : this.Stock.keySet()) {
            if (p instanceof ChocolatDeMarque) {
                predit.put((ChocolatDeMarque)p, this.Stock.get(p));
                // On initialise les achats à 0.0 pour chaque produit connu en stock
                this.ChocolatsAchetes.put((ChocolatDeMarque)p, 0.0);
            }
        }
    

        // 2. Livraisons prévues pour CE tour par les contrats CADRES EXISTANTS
        for (ExemplaireContratCadre contrat : this.mesContrats) {
            IProduit p = (IProduit) contrat.getProduit();
            if (p instanceof ChocolatDeMarque) {
                ChocolatDeMarque cdm = (ChocolatDeMarque) p;
                double quantiteAttendue = contrat.getEcheancier().getQuantite(etapeActuelle);
                
                // Mise à jour Stock Predit
                double stockActuel = predit.getOrDefault(cdm, 0.0);
                predit.put(cdm, stockActuel + quantiteAttendue);
                
                // ACTION : On remplit ChocolatsAchetes avec ce qui est livré ce tour
                double achatsActuels = this.ChocolatsAchetes.getOrDefault(cdm, 0.0);
                this.ChocolatsAchetes.put(cdm, achatsActuels + quantiteAttendue);
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
        
            // 1. Mise à jour du Stock Predit (pour la logique de mise en rayon)
            double ancienStockPredit = this.stockPredit.getOrDefault(cdm, 0.0);
            this.stockPredit.put(cdm, ancienStockPredit + livraisonImmediate);
            
            // 2. ACTION : Mise à jour de ChocolatsAchetes (suivi des flux du tour)
            double anciensAchats = this.ChocolatsAchetes.getOrDefault(cdm, 0.0);
            this.ChocolatsAchetes.put(cdm, anciensAchats + livraisonImmediate);
        }
    }
}
}