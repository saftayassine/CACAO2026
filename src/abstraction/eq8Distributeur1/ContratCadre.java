package abstraction.eq8Distributeur1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.Color;

import abstraction.eqXRomu.contratsCadres.Echeancier;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.contratsCadres.IAcheteurContratCadre;
import abstraction.eqXRomu.contratsCadres.IVendeurContratCadre;
import abstraction.eqXRomu.contratsCadres.SuperviseurVentesContratCadre;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.IProduit;

/** @author Ewen Landron */
public class ContratCadre extends Approvisionnement2 implements IAcheteurContratCadre {
    
    private Map<IProduit, Double> prixCibleVoulu;
    private Map<IProduit, Double> prixMaxVoulu;
    protected List<ExemplaireContratCadre> mesContrats;
    private Map<IProduit, Double> quantiteParEtapeVoulue;

    public ContratCadre() {
        super();
        this.prixCibleVoulu = new HashMap<>();
        this.prixMaxVoulu = new HashMap<>();
        this.mesContrats = new ArrayList<>();
        this.quantiteParEtapeVoulue = new HashMap<>();
    }

    @Override
    protected double methodeIntermediaireAchat(ChocolatDeMarque cdm, double besoinParEtape, double prixCible, double prixMax) {
        this.prixCibleVoulu.put(cdm, prixCible);
        this.prixMaxVoulu.put(cdm, prixMax);
        this.quantiteParEtapeVoulue.put(cdm, besoinParEtape);

        SuperviseurVentesContratCadre sup = (SuperviseurVentesContratCadre) (Filiere.LA_FILIERE.getActeur("Sup.CCadre"));
        List<IVendeurContratCadre> vendeurs = sup.getVendeurs(cdm);
        
        if (vendeurs.size() > 0) {
            // On propose 12 étapes avec précisément notre besoin par étape
            // Quantité totale du contrat = besoinParEtape * 12
            Echeancier ech = new Echeancier(Filiere.LA_FILIERE.getEtape() + 1, 12, besoinParEtape);
            
            ExemplaireContratCadre c = sup.demandeAcheteur(this, vendeurs.get(0), cdm, ech, this.cryptogramme, false);
            
            if (c != null) {
                this.mesContrats.add(c);
                return c.getQuantiteTotale();
            }
        }
        return 0.0;
    }

    public boolean achete(IProduit produit) {
        // On n'accepte la négociation que si nous avons défini un besoin (prixCible présent)
        return produit instanceof ChocolatDeMarque && this.prixCibleVoulu.containsKey(produit);
    }

    public Echeancier contrePropositionDeLAcheteur(ExemplaireContratCadre contrat) {
        double monBesoin = this.quantiteParEtapeVoulue.getOrDefault(contrat.getProduit(), 0.0);
        if (monBesoin <= 0) return null; // Sécurité

        Echeancier echVendeur = contrat.getEcheancier();
        Echeancier echReponse = new Echeancier(echVendeur.getStepDebut());

        for (int step = echVendeur.getStepDebut(); step <= echVendeur.getStepFin(); step++) {
            double qteVendeur = echVendeur.getQuantite(step);

            if (qteVendeur > monBesoin) {
                // Cas 1 : Trop de chocolat -> On impose strictement notre besoin
                echReponse.set(step, monBesoin);
            } 
            else if (Math.abs(qteVendeur - monBesoin) < 0.01) {
                // Cas 2 : Égalité -> On garde la valeur
                echReponse.set(step, qteVendeur);
            } 
            else {
                // Cas 3 : Inférieur au besoin -> On vise le milieu (compromis)
                double milieu = (qteVendeur + monBesoin) / 2.0;
                echReponse.set(step, milieu);
            }
        }

        return echReponse;
    }

    public double contrePropositionPrixAcheteur(ExemplaireContratCadre contrat) {
        double pCible = this.prixCibleVoulu.getOrDefault(contrat.getProduit(), 1000.0);
        double pMax = this.prixMaxVoulu.getOrDefault(contrat.getProduit(), 2000.0);
        double pVendeur = contrat.getPrix(); // Dernière proposition du vendeur

        // 1. Si le prix vendeur est déjà très bon (< 90% du prix cible), on accepte direct
        if (pVendeur <= pCible * 0.9) {
            return pVendeur;
        }

        // 2. Définition de la stratégie de progression linéaire
        double debutNego = pCible * 0.9;
        double margeTotale = pMax - debutNego;
    
        // On calcule le nombre de propositions faites par l'acheteur jusqu'ici.
        // La liste contient : [Vendeur1, Acheteur1, Vendeur2, Acheteur2, Vendeur3...]
        // Le nombre de contre-propositions de l'acheteur est donc la taille de la liste divisée par 2.
        int tourDeNego = contrat.getListePrix().size() / 2;

        // 3. Calcul de notre nouvelle offre
        // On ajoute une "marche" fixe à chaque tour (1/10ème de la marge totale)
        double nouvelleOffre = debutNego + (tourDeNego * (margeTotale / 10.0));

        // 4. Gestion de la fin de négociation
        if (tourDeNego >= 10) {
            // Au bout de 10 tours, on s'arrête. 
            // Si le prix du vendeur est en dessous de notre max, on fait un dernier effort et on accepte
            // Sinon, on retourne -1 pour casser la négociation.
            return (pVendeur <= pMax) ? pVendeur : -1.0;
        }

        // Si notre calcul nous amène au-dessus du prix vendeur, on ne surenchérit pas, 
        // on accepte simplement son prix.
        if (nouvelleOffre >= pVendeur) {
            return pVendeur;
        }

        return nouvelleOffre;
    }

    public void notificationNouveauContratCadre(ExemplaireContratCadre contrat) {
        // Notification de réussite des négociations
        this.journal5.ajouter(Color.GREEN, Color.BLACK, "CC conclu : " + contrat.toString());
        if (!this.mesContrats.contains(contrat)) {
            this.mesContrats.add(contrat);
        }
    }

    public void receptionner(IProduit p, double quantiteEnTonnes, ExemplaireContratCadre contrat) {
        // Mise à jour du stock lors de la livraison effective
        double stockActuel = this.Stock.getOrDefault(p, 0.0);
        this.Stock.put(p, stockActuel + quantiteEnTonnes);
        this.journal5.ajouter("Réception de " + quantiteEnTonnes + "T de " + p);
    }
}