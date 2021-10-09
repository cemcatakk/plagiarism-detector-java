import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;

class Test {
    public static void main(String[] args) {
        long startTime = System.nanoTime();
        String mainDocPath = "main_doc.txt"; //Bu ikisini degistirip yorum satirini kaldirin
        String folderPath = "documents"; //<<
        int printCount=5;
        Plagiarism_Detector detector = new Plagiarism_Detector(mainDocPath, folderPath);
        detector.printAll(printCount);
        long elapsedTime = System.nanoTime() - startTime;
        System.out.println("Total execution time : " + elapsedTime/1000000+"ms");
    }
}

class SimilarityChecker{
    public static int stringDistance(String str1, String str2) {
        //levenshtein distance used to compare 2 strings
        String string1 = str1.toLowerCase();
        String string2 = str2.toLowerCase();
        int s1Len = string1.length();
        int s2Len = string2.length();
        int[] costOf = new int[s2Len + 1];
        for (int i = 0; i <= s1Len; i++) {
            int lastcost = i;
            for (int j = 0; j <= s2Len; j++) {
                if (i == 0){
                    costOf[j] = j;
                }
                else {
                    if (j > 0) {
                        int newcost = costOf[j - 1];
                        if (string1.charAt(i - 1) != string2.charAt(j - 1)){
                            newcost = Math.min(Math.min(newcost, lastcost), costOf[j]) + 1;
                        }
                        costOf[j - 1] = lastcost;
                        lastcost = newcost;
                    }
                }
            }
            if (i > 0)
                costOf[s2Len] = lastcost;
        }
        return costOf[s2Len];
    }
}

class Sentence implements Comparable{
    public String content;
    public double similarity;

    public Sentence(String content){
        this.content=content;
        similarity=content.length();
    }
    public void CalculateSimilarity(Sentence sentence){
        double tmp = SimilarityChecker.stringDistance(this.content,sentence.content);
        if (tmp<=similarity||similarity==content.length()){
            similarity=tmp;
        }
    }

    @Override
    public int compareTo(Object o) {
        if (o==this)return 0;
        if(!(o instanceof Sentence))return -1;
        Sentence temp = (Sentence)o;
        if (temp.similarity>this.similarity)return -1;
        else if(temp.similarity<this.similarity)return 1;
        else return 0;
    }
}
class Document{
    public String path;
    public ArrayList<Sentence> sentences;
    public double averageSim;
    public Document(String path){
        this.path=path;
        averageSim=0;
        sentences = new ArrayList<Sentence>();
        readFromfile();
    }
    private void readFromfile(){
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path,StandardCharsets.UTF_8));
            String line = bufferedReader.readLine();
            while (line!=null){
                if(!(line.isBlank()||line.isEmpty()))sentences.add(new Sentence(line));
                line=bufferedReader.readLine();
            }
        }catch (Exception e){
            System.out.println("Error occured when reading file: "+path);
        }
    }
    public void Sort(){
        Collections.sort(sentences);
    }
    //This method compares each sentence
    public void CompareDocuments(Document mainDocument){
        for (Sentence mainSentence : mainDocument.sentences){
            for (Sentence otherSentence : sentences){
                otherSentence.CalculateSimilarity(mainSentence);
            }
        }
        for(Sentence sent:sentences){
            averageSim+=sent.similarity;
        }
        averageSim/=sentences.size();
    }
    public void printTopX(int X){
        System.out.println("Top "+X+" similar sentences of file "+path);
        int i=0;
        while(i<sentences.size()){
            System.out.println("Score: "+sentences.get(i).similarity+" |"+sentences.get(i).content);
            i++;
            if(i==X)break;
        }
        System.out.println("Document similarity score: "+averageSim);
    }
}
class Plagiarism_Detector {
    Document mainDocument;
    ArrayList<Document> otherDocuments;

    public Plagiarism_Detector(String mainPath, String othersFolderPath) {
        mainDocument = new Document(mainPath);
        otherDocuments = new ArrayList<Document>();
        File folder = new File(othersFolderPath);
        if (folder.exists()) {
            for (File docFile : folder.listFiles()) {
                if (!docFile.isDirectory()) {
                    Document newDoc =new Document(docFile.getPath());
                    newDoc.CompareDocuments(mainDocument);
                    newDoc.Sort();
                    otherDocuments.add(newDoc);
                }
            }
        }
        else{
            System.out.println("Folder '"+othersFolderPath+"' could not found. Exiting..");
            System.exit(0);
        }
    }
    public void printAll(int X){
        for (Document document:otherDocuments){
            document.printTopX(X);
            System.out.println();
        }
    }
}