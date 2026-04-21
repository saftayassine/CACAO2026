package abstraction.eq5Transformateur2;

import java.applet.Applet;
import java.awt.Color;
import java.util.List;

import abstraction.eqXRomu.contratsCadres.*;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IActeur;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.produits.IProduit;
import abstraction.eqXRomu.produits.Chocolat; 
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.bourseCacao.BourseCacao;

/**
 * @author Pierre GUTTIEREZ
 */
public class Transformateur2AchatCC extends Transformateur2VendeurAuxEncheres implements IAcheteurContratCadre{

    public Transformateur2AchatCC() {
        super();
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
	 */
	public Echeancier contrePropositionDeLAcheteur(ExemplaireContratCadre contrat){
			return contrat.getEcheancier();
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
		if (contrat.getPrix() <= contrat.getQuantiteTotale()*0.85*((BourseCacao) (Filiere.LA_FILIERE.getActeur("BourseCacao"))).getCours(Feve.F_MQ).getValeur()){
			return contrat.getPrix();
		} else{
			return 0.75*0.5*(contrat.getQuantiteTotale()*0.85*((BourseCacao) (Filiere.LA_FILIERE.getActeur("BourseCacao"))).getCours(Feve.F_MQ).getValeur() + contrat.getPrix());
		}
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
		this.getJournaux().get(3).ajouter(contrat.toString()+ "\n");
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
		}
	}

}
    