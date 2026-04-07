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
public class Approvisionnement2 extends Distributeur1Acteur {

    protected Map<ChocolatDeMarque, Double> prixDAchat;
    private Map<ChocolatDeMarque, Double> stockPredit;
    private Map<String, List<ChocolatDeMarque>> classements;
    protected List<ExemplaireContratCadre> mesContrats;

    public Approvisionnement2() {
        super(); // Appelle le constructeur de Distributeur1Acteur
        this.prixDAchat = new HashMap<>();
        this.classements = new HashMap<>();
        this.classements.put("BQ", new ArrayList<>());
        this.classements.put("BQ_EQUITABLE", new ArrayList<>());
        this.classements.put("MQ", new ArrayList<>());
        this.classements.put("MQ_EQUITABLE", new ArrayList<>());
        this.classements.put("HQ", new ArrayList<>());
        this.classements.put("HQ_EQUITABLE", new ArrayList<>());
        this.mesContrats = new ArrayList<>();
    }

    /**
     * Initialise les prix avec les données historiques (1 an en arrière)
     */
    public void initialiserPrixReference() {
        List<ChocolatDeMarque> tousLesChocolats = Filiere.LA_FILIERE.getChocolatsProduits();
        int etapeReference = Filiere.LA_FILIERE.getEtape() - 24;

        for (ChocolatDeMarque cdm : tousLesChocolats) {
            double prixHisto = Filiere.LA_FILIERE.prixMoyen(cdm, etapeReference);
            // Sécurité si le produit n'existait pas il y a un an
            if (prixHisto <= 0) prixHisto = 1000.0; 
            this.prixDAchat.put(cdm, prixHisto);
        }
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
        // On initialise le stock prédit à partir du stock réel hérité
        this.stockPredit = initialiserStockPredit();

        double cibleBasse = volumeCibleTotal * 0.20;
        double cibleMoyenne = volumeCibleTotal * 0.45;
        double cibleHaute = volumeCibleTotal * 0.35;

        acheterParGamme(Gamme.BQ, cibleBasse);
        acheterParGamme(Gamme.MQ, cibleMoyenne);
        acheterParGamme(Gamme.HQ, cibleHaute);
    }

    private void acheterParGamme(Gamme gamme, double volumeCibleGamme) {
        List<ChocolatDeMarque> equitables = getListeTriee(gamme, true);
        List<ChocolatDeMarque> standards = getListeTriee(gamme, false);

        parcourirEtAcheter(equitables, volumeCibleGamme);
        parcourirEtAcheter(standards, volumeCibleGamme);
    }

    private void parcourirEtAcheter(List<ChocolatDeMarque> liste, double volumeCibleGamme) {
        for (int i = 0; i < liste.size(); i++) {
            ChocolatDeMarque actuel = liste.get(i);
            double prixCible = this.prixDAchat.getOrDefault(actuel, 1000.0);
            
            double prixMax;
            if (i < liste.size() - 1) {
                ChocolatDeMarque suivant = liste.get(i + 1);
                prixMax = this.prixDAchat.getOrDefault(suivant, prixCible * 1.2);
            } else {
                prixMax = prixCible * 1.2;
            }
            remplirProduit(actuel, volumeCibleGamme, prixCible, prixMax);
        }
    }

    private void remplirProduit(ChocolatDeMarque cdm, double volumeCibleGamme, double prixCible, double prixMax) {
        double stockActuelGamme = calculerStockGamme(cdm.getGamme());
    
        if (stockActuelGamme < volumeCibleGamme) {
            double besoin = volumeCibleGamme - stockActuelGamme;
            // La méthode est maintenant void, l'actualisation du stock est interne
            methodeIntermediaireAchat(cdm, besoin, prixCible, prixMax);
        }
    }



    private double calculerStockGamme(Gamme gamme) {
        double total = 0;
        for (Map.Entry<ChocolatDeMarque, Double> entry : stockPredit.entrySet()) {
            if (entry.getKey().getGamme() == gamme) {
                total += entry.getValue();
            }
        }
        return total;
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
        int etapeActuelle = Filiere.LA_FILIERE.getEtape();

        // 1. On ajoute le stock physique actuel
        for (IProduit p : this.Stock.keySet()) {
            if (p instanceof ChocolatDeMarque) {
                predit.put((ChocolatDeMarque)p, this.Stock.get(p));
            }
        }

        // 2 . On ajoute les livraisons prévues pour CE tour par les anciens contrats
        for (ExemplaireContratCadre contrat : this.mesContrats) {
            IProduit p = (IProduit) contrat.getProduit();
            if (p instanceof ChocolatDeMarque) {
                ChocolatDeMarque cdm = (ChocolatDeMarque) p;
                double quantiteAttendue = contrat.getEcheancier().getQuantite(etapeActuelle);
                double stockActuel = predit.getOrDefault(cdm, 0.0);
                predit.put(cdm, stockActuel + quantiteAttendue);
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
            
                // On récupère la livraison prévue pour l'étape en cours par ce nouveau contrat
                double livraisonImmediate = nouveauContrat.getEcheancier().getQuantite(etapeActuelle);
            
                double ancienStockPredit = this.stockPredit.getOrDefault(cdm, 0.0);
                this.stockPredit.put(cdm, ancienStockPredit + livraisonImmediate);
            }
        }
    }
}