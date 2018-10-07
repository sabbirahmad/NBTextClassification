import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class NBTextClassify {
	private String trainFile;
	private String testFile;
	
	private final String comWords="the be to of and is was its so by in that have it for not on with he as you do at this but his by from they we an or will if a b c d e f g h i j k l m n o p q r s t u v w x y z";
	Map<String, Integer> cwMap;
	
	private double SM=1;
	
	private int noOfClass=0;
	private int noOfDocuments=0;
	private int noOfTestDoc;
	
	ArrayList<DocClass> trainDocClass;
	ArrayList<Doc> trainDoc;
	ArrayList<Doc> testDoc;
	
	Map<String, Integer> className;
	Map<String, Integer> words;
	
	public NBTextClassify(String trainFile, String testFile) throws FileNotFoundException {
		this.trainFile=trainFile;
		this.testFile=testFile;
		
		cwMap=new HashMap<String, Integer>();
		createCommonMap();
		
		className=new HashMap<String, Integer>();
		words=new HashMap<String, Integer>();
		trainDocClass=new ArrayList<DocClass>();
		testDoc=new ArrayList<Doc>();
		trainDoc=new ArrayList<Doc>();
		
		parseTrainFile(this.trainFile, trainDocClass);
		parseTestFile(this.testFile,testDoc);
		//parseTestFile(this.trainFile,trainDoc);
		
		System.out.println("Class: "+noOfClass);
		System.out.println("Train Documents: "+noOfDocuments);
		System.out.println("No of Test Documents: "+testDoc.size());
		System.out.println("Words: "+words.size());
		
		//System.out.println(className);
		
		noOfTestDoc=testDoc.size();
		
		calculateProbability(trainDocClass);
		
		System.out.println("Probability calculated!");
		
		/*
		System.out.println("logV: "+Math.log10((SM)/(words.size()*SM)));
		//System.out.println(className.get("earn"));
		int cl=className.get("earn");
		System.out.println(trainDocClass.get(cl).pC);
		System.out.println("LogPC: "+Math.log10(trainDocClass.get(cl).pC));
		System.out.println("label: "+trainDocClass.get(cl).label);
		System.out.println(trainDocClass.get(cl).prob);
		//System.out.println(trainDocClass.get(cl).freq);
		*/
		
		//NB(trainDocClass, testDoc);
		//NB(trainDocClass, trainDoc);
		
		
		diffSM();
	}
	
	private void diffSM(){
		SM=1;
		NB(trainDocClass, testDoc);
		for(double sm=0.5;sm>.000000000004;sm=sm/10){
			SM=sm;
			NB(trainDocClass, testDoc);
		}
	}
	
	private void createCommonMap(){
		String[] wordStr=comWords.split(" ");
		//System.out.println(wordStr.length);
		int len=wordStr.length;
		for(int i=0;i<len;i++){
			cwMap.put(wordStr[i], 1);
		}
		cwMap.put(" ",1);
		cwMap.put("",1);
		//System.out.println(cwMap.size());
	}
	
	
	private void parseTrainFile(String fileName, ArrayList<DocClass> classList) throws FileNotFoundException{
		Scanner input=new Scanner(new File(fileName));
		String line="";
		int classNo;
		while(input.hasNext()){
			noOfDocuments++;
			String str="";
			//Doc doc=new Doc();
			line=input.nextLine();//label
			//doc.label=new String(line);
			//check if the class already exists
			if(!className.containsKey(line)){
				className.put(line, noOfClass);
				DocClass docClass=new DocClass();
				docClass.label=line;
				classList.add(docClass);
				classNo=noOfClass;
				noOfClass++;
			}
			else{
				classNo=className.get(line);
				classList.get(classNo).nD=classList.get(classNo).nD + 1;
			}
			
			line=input.nextLine();//blank
			//read title
			while(true){
				line=input.nextLine();
				if(line.equals("")){
					//doc.title=str;
					break;
				}
				str+=(line+" ");
			}
			
			//read address
			while(true){
				line=input.nextLine();
				if(line.equals("")){
					break;
				}
			}
			
			//read data	
			while(true){
				if(input.hasNext()){
					line=input.nextLine();
				}
				else{
					break;
				}
				if(line.equals("")){
					if(input.hasNext())
						line=input.nextLine();
					break;
				}
				str+=(line+" ");
			}
			int nWClass=0;		
			str=str.toLowerCase();
			String[] strs=str.split("\\W+|\\d+");
			int length=strs.length;
			for(int i=0;i<length;i++){
				//if(strs[i].equals(""))
				if(cwMap.containsKey(strs[i]))
					continue;
				nWClass++;
				if(classList.get(classNo).freq.containsKey(strs[i])){
					int fr=classList.get(classNo).freq.get(strs[i]);
					classList.get(classNo).freq.put(strs[i], fr+1);
				}
				else{
					classList.get(classNo).freq.put(strs[i], 1);
				}
				if(!words.containsKey(strs[i])){
					words.put(strs[i], 1);
				}
			}
			classList.get(classNo).nW=classList.get(classNo).nW + nWClass;
			
		}
		
		input.close();
	}
	private void parseTestFile(String fileName, ArrayList<Doc> docList) throws FileNotFoundException{
		Scanner input=new Scanner(new File(fileName));
		String line="";
		while(input.hasNext()){
			String str="";
			Doc doc=new Doc();
			line=input.nextLine();//label
			doc.label=new String(line);
			line=input.nextLine();//blank
			//read title
			while(true){
				line=input.nextLine();
				if(line.equals("")){
					doc.title=str;
					break;
				}
				str+=(line+" ");
			}
			
			//read address
			while(true){
				line=input.nextLine();
				if(line.equals("")){
					break;
				}
			}
			
			//read data	
			while(true){
				if(input.hasNext()){
					line=input.nextLine();
				}
				else{
					break;
				}
				if(line.equals("")){
					if(input.hasNext())
						line=input.nextLine();
					break;
				}
				str+=(line+" ");
			}
			
			str=str.toLowerCase();
			String[] strs=str.split("\\W+|\\d+");
			int length=strs.length;
			for(int i=0;i<length;i++){
				//if(strs[i].equals(""))
				if(cwMap.containsKey(strs[i]))
					continue;
				
				if(doc.eucVec.containsKey(strs[i])){
					int val=doc.eucVec.get(strs[i]);
					doc.eucVec.put(strs[i], val+1);
				}
				else{
					doc.eucVec.put(strs[i], 1);
				}
				if(!words.containsKey(strs[i])){
					words.put(strs[i], 1);
				}
				
			}
			
			docList.add(doc);
		}
		
		input.close();
	}
	
	private void calculateProbability(ArrayList<DocClass> classList){
		int smD=words.size();
		for(String key:className.keySet()){//for every class
			int classNo=className.get(key);
			DocClass docClass=classList.get(classNo);
			docClass.pC=docClass.nD/(noOfDocuments*1.0);//probability of this class
			int nW=docClass.nW;
			//System.out.println(docClass.label+": "+nW+"\tpC: "+docClass.pC);
			double pW;
			for(String word:docClass.freq.keySet()){//for every word in this class
				pW=(SM+docClass.freq.get(word))/(smD*SM+nW*1.0);
				docClass.prob.put(word, pW);
			}
		}
	}
	
	private void NB(ArrayList<DocClass> classList, ArrayList<Doc> testDoc){
		int testSize=testDoc.size();
		int classSize=classList.size();
		int accuracy=0;
		for(int i=0;i<testSize;i++){
			int bestClass=0;
			double bestProbVal=-999999999.0;
			double tempProbVal;

			Doc doc=testDoc.get(i);
			DocClass docClass=null;
			for(int j=0;j<classSize;j++){
			//for(int j=56;j<57;j++){
				docClass=classList.get(j);
				tempProbVal=probDocInClass(docClass,doc);
				//System.out.println("tempProbVal: "+tempProbVal);
				if(tempProbVal>bestProbVal){//class with better probability 
					bestClass=j;
					bestProbVal=tempProbVal;
					//System.out.println("bestClass: "+bestClass);
				}
			}
			//check if class is correct
			//System.out.println("tl: "+doc.label+"\tcl: "+classList.get(bestClass).label);
			if(doc.label.equals(classList.get(bestClass).label)){
				accuracy++;
			}
		}
		System.out.println("SMF: "+SM+"\taccuracy: "+(accuracy*100.0)/noOfTestDoc+"%");
		//System.out.println("SMF: "+SM+"\taccuracy: "+(accuracy*100.0)/trainDoc.size()+"%");
		
	}
	
	private double probDocInClass(DocClass docClass, Doc doc){
		int smD=words.size();
		double probability=0;
		probability+=Math.log10(docClass.pC);
		for(String word:doc.eucVec.keySet()){
			if(docClass.freq.containsKey(word)){
				//System.out.println("word: "+word);
				//System.out.println("freq: "+doc.eucVec.get(word));
				//System.out.println(Math.pow(Math.log10(docClass.prob.get(word)), doc.eucVec.get(word)));
				probability+=(doc.eucVec.get(word) * Math.log10(docClass.prob.get(word)));
				//System.out.println("if: "+probability);
			}
			else{
				probability+=Math.log10(SM/(smD*SM+docClass.nW));
				//System.out.println(probability);
			}
		}
		return probability;
	}
	
	
	public static void main(String[] args) throws FileNotFoundException {
		String trainFile="//Users//ahmadsabbir//Documents//workspace//NBTextClassification//resource//training.data";
		String testFile="//Users//ahmadsabbir//Documents//workspace//NBTextClassification//resource//test.data";
		new NBTextClassify(trainFile, testFile);
	}

}

class DocClass{
	String label;
	int nD;
	int nW;
	double pC;
	HashMap<String, Integer> freq;
	HashMap<String, Double> prob;
	public DocClass() {
		super();
		this.label="";
		this.nD=1;
		this.nW=0;
		this.pC=0;
		freq=new HashMap<String, Integer>();
		prob=new HashMap<String, Double>();
	}
	public DocClass(String label, int nD, int nW, double pC, HashMap<String, Integer> freq, HashMap<String, Double> prob) {
		super();
		this.label = label;
		this.nD = nD;
		this.nW=nW;
		this.pC=pC;
		this.freq = freq;
		this.prob = prob;
	}
}

class Doc{
	String label;
	String title;
	String address;
	HashMap<String, Integer> eucVec;
	HashMap<String, Double> tfIdf;
	
	public Doc(){
		this.label="";
		this.title="";
		this.address="";
		this.eucVec=new HashMap<String, Integer>();
		this.tfIdf=new HashMap<String, Double>();
	}
	
	public Doc(Doc doc){
		this.label=new String(doc.label);
		this.title=new String(doc.title);
		this.address=new String(doc.address);
		this.eucVec=new HashMap<String, Integer>(doc.eucVec);
		this.tfIdf=new HashMap<String, Double>(doc.tfIdf);
	}
}
