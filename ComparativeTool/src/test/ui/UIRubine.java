package test.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import data.Dot;
import data.RecordParser;

import algorithm.rubine.Classifier;
import algorithm.rubine.Gesture;
import algorithm.rubine.GestureClass;


import test.TraceWriter;
import test.Utils;

public class UIRubine{

	private int[][] confusionMatrix;
	private double[] usersTrainingRecognitionRate;
	private double[] usersRecognitionRate;
	private TraceWriter tw;
	private int category;
	
	public UIRubine(int category){
		this.category = category;
		switch(category){
		case 1: tw = new TraceWriter("digits/ui/UIDigitsRubine.txt");break;
		case 2: tw = new TraceWriter("gestures/ui/UIGesturesRubine.txt");break;
		case 3: tw = new TraceWriter("letters/ui/UILettersRubine.txt");break;
		case 4: tw = new TraceWriter("shapes/ui/UIShapesRubine.txt");break;
		}
	}
	
	
	
	public void UI(){
		tw.println("-------------------------------------------------- User Independent -------------------------------------------------");
		tw.println();
		for(int numberTrainingExamples = 2; numberTrainingExamples <= 9; numberTrainingExamples++){
			tw.println("------------------------------------------- numberTrainingExamples="+numberTrainingExamples+" ---------------------------------------");
			tw.println();
			usersTrainingRecognitionRate = new double[5];
			usersRecognitionRate = new double[5];
			confusionMatrix = new int[Utils.maxGestureIndex(category)][Utils.maxGestureIndex(category)];
			for(int user = 1; user <= 5; user++){
				UI(user,numberTrainingExamples);
			}
		
			tw.println("numberTrainingExamples="+numberTrainingExamples+" :");
			tw.println();
			tw.println(Utils.matrixToString(confusionMatrix,category));
			tw.println(Utils.matrixToStringForLatex(confusionMatrix,category));
			double[] informations = Utils.informations(confusionMatrix,category);
			tw.println();
			tw.println("umberTrainingExamples="+numberTrainingExamples+" :");
			tw.println();
			for(int user = 0; user < 5; user++)
				tw.println("Recognition rate for training whith user"+(user+1)+" = "+usersTrainingRecognitionRate[user]);
			tw.println();
			for(int user = 0; user < 5; user++)
				tw.println("Recognition rate for user"+(user+1)+" = "+usersRecognitionRate[user]);
			tw.println();
			tw.println("Goodclass examples = "+informations[0]);
			tw.println("Badclass examples = "+informations[1]);
			tw.println("total examples = "+informations[2]);
			tw.println("Recognition rate = "+informations[3]);
			tw.println("----------------------------------------------------------------------------------------------------------------");
			tw.println();
			tw.println();
			tw.println();
			
			tw.println("================================================================================================================");
			tw.println();
		}
		tw.close();
	}
	
	public void UI(int user, int numberTrainingExamples){
		int[][] userTrainingConfusionMatrix = new int[Utils.maxGestureIndex(category)][Utils.maxGestureIndex(category)];
		List<int[][]> usersConfusionMatrixes = new ArrayList<int[][]>();
		for(int i = 0; i < 5; i++)
			usersConfusionMatrixes.add(new int[Utils.maxGestureIndex(category)][Utils.maxGestureIndex(category)]);
			
		for(int firstTeIndex = 1; firstTeIndex <= 10; firstTeIndex++){
			Classifier classifier = new Classifier();
			for(int gestureIndex = 0; gestureIndex < Utils.maxGestureIndex(category); gestureIndex++){
				GestureClass gc = new GestureClass(Utils.intToGesture(gestureIndex,category)+"");
				int mod = 0;
				for(int teIndex = firstTeIndex; mod*10+teIndex < firstTeIndex+numberTrainingExamples; teIndex++){
					Gesture gest = new Gesture();
					List<Dot> dotSet = (new RecordParser("records/"+user+"/"+Utils.intToGesture(gestureIndex,category)+"/"+Utils.intToGesture(gestureIndex,category)+teIndex+".txt")).parse();
					gest.addGesture(dotSet, false, false, false, false);
					gc.addExample(gest);
					if(teIndex == 10){
						teIndex = 0;
						mod = 1;
					}
				}
				classifier.addClass(gc);
			}
			classifier.compile();
			
		
			for(int gestureIndex = 0; gestureIndex < Utils.maxGestureIndex(category); gestureIndex++){
				int tmp = (firstTeIndex+numberTrainingExamples)%10;
				if(tmp == 0) tmp = 10;
				for(int teIndex = tmp; teIndex != firstTeIndex ; teIndex = (teIndex%10)+1){
					for(int otherUser = 1; otherUser <= 5; otherUser++)
						if(otherUser != user){
							
							Gesture toRecognize = new Gesture();
							List<Dot> dotSet = (new RecordParser("records/"+otherUser+"/"+Utils.intToGesture(gestureIndex,category)+"/"+Utils.intToGesture(gestureIndex,category)+teIndex+".txt")).parse();
							toRecognize.addGesture(dotSet, false, false, false, false);
							
							String foundGesture = classifier.classify(toRecognize).getName();
							confusionMatrix[gestureIndex][Utils.gestureToInt(foundGesture,category)]++;
							userTrainingConfusionMatrix[gestureIndex][Utils.gestureToInt(foundGesture,category)]++;
							usersConfusionMatrixes.get(otherUser-1)[gestureIndex][Utils.gestureToInt(foundGesture,category)]++;
						}
				}
			}
			
			System.out.println("nte="+numberTrainingExamples+"|user="+user+"|ftei="+firstTeIndex+" recognized");
		}
		
		usersTrainingRecognitionRate[user-1] = Utils.recognitionRate(userTrainingConfusionMatrix,category);
		for(int i = 0; i < 5; i++)
			if(i != user-1)
				usersRecognitionRate[i] += Utils.recognitionRate(usersConfusionMatrixes.get(i),category)/4;
	}
	
	

	
	public static void main(String[] args){
		int category = Integer.parseInt(args[0]);
		UIRubine uir = new UIRubine(category);
		uir.UI();
	}
	
	
}
