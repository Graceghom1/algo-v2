package org.example;

import java.io.*;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;


public class FusionCompressionHuffman {

    public static void main(String[] args) throws IOException {
        /**
         * Fichiers à fusionner
         */

        File file1 = new File("D:/projet1 algo/releve1.html");
        File file2 = new File("D:/projet1 algo/releve2.html");
        File file3 = new File("D:/projet1 algo/releve3.html");
        File file4 = new File("D:/projet1 algo/releve4.html");


        /**
         *  Extraire l'en-tête commun (tout jusqu'au body)
          */

        String entete = extraireEntete(file1);
        if (!entete.equals(extraireEntete(file2)) || !entete.equals(extraireEntete(file3)) || !entete.equals(extraireEntete(file4))) {
            System.out.println("Les en-têtes ne sont pas identiques !");
            return;
        }

        /**
         * Fusionner les corps des fichiers
          */

        String corpsFusionne =  "\n ----------- fichier1 ---------------- \n" + lireCorpsSansEntete(file1) +
                                "\n ----------- fichier2 ---------------- \n" + lireCorpsSansEntete(file2) +
                                "\n ----------- fichier3 ---------------- \n" +  lireCorpsSansEntete(file3)+
                               "\n ----------- fichier4 ---------------- \n"+  lireCorpsSansEntete(file4);

        /**
         * Compresser l'en-tête une seule fois
          */

        String enteteCompresse = compresserEntete(entete);
        System.out.println("En-tête compressé : " + enteteCompresse);

        /**
         * Compresser le corps fusionné
          */

        Map<Character, Integer> frequences = calculerFrequences(corpsFusionne);
        Map<Character, String> codes = genererCodesHuffman(frequences);
        String corpsCompresse = compresserTexte(corpsFusionne, codes);
        System.out.println("Corps compressé : " + corpsCompresse);

        /**
         * Fusionner le tout dans un fichier unique
          */

        File fichierFinal = new File("fichier_fusionne.txt");
        ecrireFichierFusionne(fichierFinal, enteteCompresse, corpsCompresse);

        /**
         * Décompresser le fichier
         */

        ArbreBinaire racine = reconstruireArbre(frequences);
        String corpsDecompresse = decomprimerTexte(corpsCompresse, racine);
        System.out.println("Corps décompressé : " + corpsDecompresse);

        /**
         * Calculer le taux de compression
         */
        calculerTauxDeCompression(corpsFusionne, corpsCompresse);
    }

    /**
     * Extraire l'en-tête du fichier
     * @param fichier
     * @return
     * @throws IOException
     */
    public static String extraireEntete(File fichier) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fichier));
        StringBuilder entete = new StringBuilder();
        String ligne;
        while ((ligne = reader.readLine()) != null) {
            entete.append(ligne).append("\n");
            if (ligne.contains("<body>")) {
                break;
            }
        }
        reader.close();
        return entete.toString();
    }

    /**
     * Lire le corps du fichier sans l'en-tête
     * @param fichier
     * @return
     * @throws IOException
     */
    public static String lireCorpsSansEntete(File fichier) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fichier));
        StringBuilder corps = new StringBuilder();
        String ligne;
        boolean dansBody = false;
        while ((ligne = reader.readLine()) != null) {
            if (ligne.contains("<body>")) {

                dansBody = true;
            }

            if (dansBody) {
                corps.append(ligne).append("\n");
            }
        }
        reader.close();
        return corps.toString();
    }

    /**
     * Compression de l'en-tête avec Huffman
     * @param entete
     * @return
     */
    public static String compresserEntete(String entete) {
        Map<Character, Integer> frequences = calculerFrequences(entete);
        Map<Character, String> codes = genererCodesHuffman(frequences);
        return compresserTexte(entete, codes);
    }

    /**
     * Compresser un texte à partir des codes de Huffman
     * @param texte
     * @param codes
     * @return
     */
    public static String compresserTexte(String texte, Map<Character, String> codes) {
        StringBuilder texteCompresse = new StringBuilder();
        for (char c : texte.toCharArray()) {
            texteCompresse.append(codes.get(c));
        }
        return texteCompresse.toString();
    }

    /**
     * Décompresser un texte compressé
     * @param texteCompresse
     * @param racine
     * @return
     */
    public static String decomprimerTexte(String texteCompresse, ArbreBinaire racine) {
        StringBuilder texteDecompresse = new StringBuilder();
        ArbreBinaire courant = racine;
        for (char bit : texteCompresse.toCharArray()) {
            courant = (bit == '0') ? courant.gauche : courant.droite;
            if (courant.estFeuille()) {
                texteDecompresse.append(courant.lettre);
                courant = racine;
            }
        }
        return texteDecompresse.toString();
    }

    /**
     * Calculer les fréquences des caractères
     * @param texte
     * @return
     */
    public static Map<Character, Integer> calculerFrequences(String texte) {
        Map<Character, Integer> frequences = new HashMap<>();
        for (char c : texte.toCharArray()) {
            frequences.put(c, frequences.getOrDefault(c, 0) + 1);
        }
        return frequences;
    }

    /**
     * Générer les codes de Huffman
     * @param frequences
     * @return
     */
    public static Map<Character, String> genererCodesHuffman(Map<Character, Integer> frequences) {
        PriorityQueue<ArbreBinaire> queue = new PriorityQueue<>(Comparator.comparingInt(a -> a.frequence));
        for (Map.Entry<Character, Integer> entree : frequences.entrySet()) {
            queue.add(new ArbreBinaire(entree.getKey(), entree.getValue()));
        }
        while (queue.size() > 1) {
            ArbreBinaire gauche = queue.poll();
            ArbreBinaire droite = queue.poll();
            ArbreBinaire fusion = new ArbreBinaire(gauche, droite);
            queue.add(fusion);
        }
        ArbreBinaire racine = queue.poll();
        Map<Character, String> codes = new HashMap<>();
        construireCodes(racine, "", codes);
        return codes;
    }

    /**
     * Construire les codes de Huffman
     * @param courant
     * @param code
     * @param codes
     */
    private static void construireCodes(ArbreBinaire courant, String code, Map<Character, String> codes) {
        if (courant.estFeuille()) {
            codes.put(courant.lettre, code);
        } else {
            construireCodes(courant.gauche, code + "0", codes);
            construireCodes(courant.droite, code + "1", codes);
        }
    }

    /**
     * Écrire dans le fichier final (fusionné)
     * @param fichier
     * @param enteteCompresse
     * @param corpsCompresse
     * @throws IOException
     */
    public static void ecrireFichierFusionne(File fichier, String enteteCompresse, String corpsCompresse) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fichier));
        writer.write("En-tête compressé : " + enteteCompresse);
        writer.newLine();
        writer.write("Corps compressé : " + corpsCompresse);
        writer.close();
    }

    /**
     * Calculer le taux de compression
     * @param texteOriginal
     * @param texteCompresse
     */
    public static void calculerTauxDeCompression(String texteOriginal, String texteCompresse) {
        int tailleOriginale = texteOriginal.length() * 8;
        int tailleCompressee = texteCompresse.length();
        double tauxCompression = (1 - (double) tailleCompressee / tailleOriginale) * 100;
        System.out.println("Taux de compression : " + tauxCompression + "%");
    }

    /**
     * Fonction pour reconstruire l'arbre de Huffman
     * @param frequences
     * @return
     */
    public static ArbreBinaire reconstruireArbre(Map<Character, Integer> frequences) {
        PriorityQueue<ArbreBinaire> queue = new PriorityQueue<>(Comparator.comparingInt(a -> a.frequence));
        for (Map.Entry<Character, Integer> entree : frequences.entrySet()) {
            queue.add(new ArbreBinaire(entree.getKey(), entree.getValue()));
        }
        while (queue.size() > 1) {
            ArbreBinaire gauche = queue.poll();
            ArbreBinaire droite = queue.poll();
            ArbreBinaire fusion = new ArbreBinaire(gauche, droite);
            queue.add(fusion);
        }
        return queue.poll();
    }
}

/**
 * Classe pour l'arbre binaire de Huffman
 */
class ArbreBinaire {
    char lettre;
    int frequence;
    ArbreBinaire gauche, droite;

    /**
     * Constructeur pour une feuille
     * @param lettre
     * @param frequence
     */
    public ArbreBinaire(char lettre, int frequence) {
        this.lettre = lettre;
        this.frequence = frequence;
        this.gauche = null;
        this.droite = null;
    }

    /**
     * Constructeur pour un nœud interne
     * @param gauche
     * @param droite
     */
    public ArbreBinaire(ArbreBinaire gauche, ArbreBinaire droite) {
        this.lettre = '\0';
        this.frequence = gauche.frequence + droite.frequence;
        this.gauche = gauche;
        this.droite = droite;
    }

    /**
     * Vérifie si c'est une feuille
     * @return
     */
    public boolean estFeuille() {
        return (gauche == null && droite == null);
    }
}
