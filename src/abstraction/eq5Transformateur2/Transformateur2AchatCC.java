package abstraction.eq5Transformateur2;


import abstraction.eqXRomu.bourseCacao.BourseCacao;
import abstraction.eqXRomu.contratsCadres.*;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.produits.IProduit;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Feve;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


/**
 * @author Pierre GUTTIEREZ
 */
public class Transformateur2AchatCC extends Transformateur2VendeurAuxEncheres implements IAcheteurContratCadre{

	protected List<ExemplaireContratCadre> mesContratsEnCours;

    public Transformateur2AchatCC() {
        super();
		this.mesContratsEnCours = new LinkedList<ExemplaireContratCadre>();
    }

	/**
	 * Methode appelee par le superviseur afin de savoir si l'acheteur est pret a
	 * faire un contrat cadre sur le produit indique.
	 * 
	 * @param produit
	 * @return Retourne false si l'acheteur ne souhaite pas etablir de contrat a
	 *         cette etape pour ce type de produit (retourne true si il est pret a
	 *         negocier un contrat cadre pour ce type de produit).
	 */
	public boolean achete(IProduit produit){
		if (produit instanceof Feve){
			Feve f = (Feve) produit;
			if (f.isEquitable()){
				return false;
			} else {
			return true;
			}
		} else {
			return false;
		}
	}
	/**
	 * Methode appelee par le SuperviseurVentesContratCadre lors des negociations
	 * sur l'echeancier afin de connaitre la contreproposition de l'acheteur. Les
	 * precedentes propositions d'echeancier peuvent etre consultees via un appel a
	 * la methode getEcheanciers() sur le contrat passe en parametre.
	 * 
	 * @param contrat. Notamment, getEcheancier() appelee sur le contrat retourne
	 *                 l'echeancier que le vendeur vient de proposer.
	 * @return Retoune null si l'acheteur souhaite mettre fin aux negociation (et
	 *         abandonner du coup ce contrat). Retourne le meme echeancier que celui
	 *         du contrat (contrat.getEcheancier()) si l'acheteur est d'accord pour
	 *         un tel echeancier. Sinon, retourne un nouvel echeancier que le
	 *         superviseur soumettra au vendeur.
	 * @author Pierre et Maxence
	 */
	public Echeancier contrePropositionDeLAcheteur(ExemplaireContratCadre contrat){
			Echeancier echeancier=contrat.getEcheancier();
			superviseurCC.recapitulerContratsEnCours();;
			if(echeancier.getNbEcheances()<6 && echeancier.getQuantiteTotale() > 5*10000){
				return echeancier;
			}
			else{
				Integer debut = echeancier.getStepDebut();
				Echeancier proposition = new Echeancier(debut);
				for (int index = debut; index < debut+5; index++) {
					if(echeancier.getQuantite(index)<10000){
						proposition.ajouter(10000);
					}
					else{proposition.ajouter(echeancier.getQuantite(index));}
				}
				if(proposition.echeancierAcceptable()){
					return proposition;
				}
				else{
					return null;
				}
			}

	}

	/**
	 * Methode appelee par le SuperviseurVentesContratCadre lors des negociations
	 * sur le prix a la tonne afin de connaitre la contreproposition de l'acheteur.
	 * L'acheteur peut consulter les precedentes propositions via un appel a la
	 * methode getListePrix() sur le contrat. En particulier la methode getPrix()
	 * appelee sur contrat indique la derniere proposition faite par le vendeur.
	 * 
	 * @param contrat
	 * @return Retourne un prix negatif ou nul si l'acheteur souhaite mettre fin aux
	 *         negociations (en renoncant a ce contrat). Retourne le prix actuel
	 *         (contrat.getPrix()) si il est d'accord avec ce prix. Sinon, retourne
	 *         un autre prix correspondant a sa contreproposition.
	 */
	public double contrePropositionPrixAcheteur(ExemplaireContratCadre contrat){
		List<Double> listePrix = contrat.getListePrix();
		BourseCacao bourse = (BourseCacao)(Filiere.LA_FILIERE.getActeur("BourseCacao"));
		Feve feve = (Feve)contrat.getProduit();
		Variable coursBourse = bourse.getCours(feve);
		Double coursActuel = coursBourse.getValeur();
        
        // SÉCURITÉ CRITIQUE : Évite le crash (IndexOutOfBoundsException) si c'est la première proposition
        if (listePrix.size() < 2) {
            // Au premier tour de négociation, on tente un prix 10% plus bas que celui de la bourse
            return coursActuel * 0.90; 
        }
        
        // Ensuite, on négocie en faisant une moyenne entre son prix et notre ancienne offre
        return Math.min(((contrat.getPrix() + listePrix.get(listePrix.size() - 2)) / 2) * 0.95,coursActuel * 0.97);
		}

	/**
	 * Methode appelee par le SuperviseurVentesContratCadre afin de notifier le
	 * l'acheteur de la reussite des negociations sur le contrat precise en
	 * parametre qui a ete initie par le vendeur. Le superviseur veillera a
	 * l'application de ce contrat (des appels a livrer(...) seront effectues
	 * lorsque le vendeur devra livrer afin d'honorer le contrat, et des transferts
	 * d'argent auront lieur lorsque l'acheteur paiera les echeances prevues)..
	 * 
	 * @param contrat
	 */
	public void notificationNouveauContratCadre(ExemplaireContratCadre contrat){
		this.mesContratsEnCours.add(contrat);
		if (contrat.getProduit() instanceof Feve){
			this.mesContratsEnCours.add(contrat);
			this.getJournaux().get(3).ajouter("Achat fève en CC : " + contrat.toString() + "\n");
		}
		else if (contrat.getProduit() instanceof ChocolatDeMarque){
			this.mesContratsEnCours.add(contrat);
			this.getJournaux().get(4).ajouter("Vente chocolat en CC : " + contrat.toString() + "\n");
		}
	}

	/**
	 * Methode appelee par le SuperviseurVentesContratCadre afin de notifier
	 * l'acheteur de la livraison du lot de produit precise en parametre
	 * (dans le cadre du contrat contrat). Il se peut que la quantitee livree
	 * soit inferieure a la quantite prevue par le contrat si le vendeur est dans 
	 * l'incapacite de la fournir. Dans ce cas, le vendeur aura une penalite 
	 * (un pourcentage de produit a livrer en plus). L'acheteur doit a minima 
	 * mettre ce produit dans son stock.
	 */
	public void receptionner(IProduit p, double quantiteEnTonnes, ExemplaireContratCadre contrat){
		if (p instanceof Feve){
			Feve f = (Feve) p;
			this.add_feve(quantiteEnTonnes, f);
			this.getJournaux().get(3).ajouter("Réception de " + quantiteEnTonnes + " T de " + f + " via Contrat Cadre de " + contrat.getVendeur().getNom() + "\n");
		}
	}

	@Override
    public void next() {
        super.next();

        // --- NOUVEAU : LE FREIN D'URGENCE (Limite globale 600 000 T) ---
        // On calcule d'abord combien de place on prend déjà avec nos fèves
        double stockFevesTotal = this.getStock_feve(Feve.F_HQ) + this.getStock_feve(Feve.F_MQ) + this.getStock_feve(Feve.F_BQ);
        
        // (Il faudrait idéalement ajouter ici votre stock de chocolat total si vous avez la méthode)
        // double stockChocoTotal = ...
        
        // Si nos entrepôts sont déjà dangereusement pleins (ex: plus de 500 000 T), on gèle les achats !
        if (stockFevesTotal > 650000.0) {
            return; // On arrête la méthode next() ici, on n'achète rien ce tour-ci !
        }
        // ---------------------------------------------------------------

        // 1. Définition des besoins cibles en fèves (pour couvrir vos 100k de choco)
        HashMap<Feve, Double> ciblesFeves = new HashMap<>();
        ciblesFeves.put(Feve.F_HQ, 98000.0);
        ciblesFeves.put(Feve.F_MQ, 154000.0);
        ciblesFeves.put(Feve.F_BQ, 238000.0);

        for (Feve f : ciblesFeves.keySet()) {
            double stockActuel = this.getStock_feve(f);
            
            // 2. On calcule ce qui arrive déjà par NOS contrats cadres en cours
            double attendu = 0;
            
            for (ExemplaireContratCadre c : this.mesContratsEnCours) {
                if (c.getAcheteur().equals(this) && c.getProduit().equals(f)) {
                    attendu += c.getQuantiteRestantALivrer();
                }
            }

            // 3. Si (Stock actuel + Fèves en livraison) < Cible, on cherche activement un vendeur
            if (stockActuel + attendu < ciblesFeves.get(f)) {
                double quantiteAManquer = ciblesFeves.get(f) - (stockActuel + attendu);
                
                // On cherche la liste des producteurs qui vendent cette fève
                List<IVendeurContratCadre> vendeurs = ((SuperviseurVentesContratCadre) Filiere.LA_FILIERE.getActeur("Sup.CCadre")).getVendeurs(f);
                
                if (!vendeurs.isEmpty()) {
                    
                    // --- NOUVEAU : LA RECHERCHE MULTI-VENDEURS ---
                    // On essaie les vendeurs un par un. Si un contrat est signé, on s'arrête.
                    for (IVendeurContratCadre vendeur : vendeurs) {
                        
                        Echeancier echeancier = new Echeancier(Filiere.LA_FILIERE.getEtape() + 1, 10, quantiteAManquer/10.0 );
                        
                        // On stocke le résultat de la demande dans une variable
                        ExemplaireContratCadre nouveauContrat = ((SuperviseurVentesContratCadre) Filiere.LA_FILIERE.getActeur("Sup.CCadre")).demandeAcheteur(this, vendeur, f, echeancier, cryptogramme, false);
                        
                        // Si le contrat a été accepté et signé (il n'est pas null)
                        if (nouveauContrat != null) {
                            break; // SUCCÈS ! On quitte la boucle des vendeurs, inutile de demander aux autres.
                        }
                    }
                    // ---------------------------------------------
                }
            }
        }
        
        // 4. Nettoyage : On supprime de notre liste les contrats qui sont totalement terminés
        this.mesContratsEnCours.removeIf(c -> c.getQuantiteRestantALivrer() == 0);
    }
}
    