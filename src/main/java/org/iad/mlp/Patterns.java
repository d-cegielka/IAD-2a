package org.iad.mlp;

import java.util.*;

public class Patterns {
    HashMap<String,ArrayList<Double[]>> allPatterns;
    HashMap<String,ArrayList<Double[]>> trainingPatterns;
    HashMap<String,ArrayList<Double[]>> testingPatterns;
    HashMap<String,ArrayList<Double[]>> validationPatterns;
    TreeMap<String, Double[]> classTypes;

    public Patterns() {
        allPatterns = new HashMap<>();
        trainingPatterns = new HashMap<>();
        testingPatterns = new HashMap<>();
        validationPatterns = new HashMap<>();
        classTypes = new TreeMap<>();
    }

    public TreeMap<String, Double[]> getClassTypes() {
        return classTypes;
    }

    public HashMap<String, ArrayList<Double[]>> getTestingPatterns() {
        return testingPatterns;
    }

    public void addClassType(String classtype){
        String[] classType = classtype.split(",");
        Double[] output = new Double[classType.length];
        for (int j = 0; j < output.length; j++) {
            output[j] = Double.parseDouble(classType[j]);
        }
        classTypes.put(classtype,output);
    }

    public void initializePatterns(){
        for(String type: classTypes.keySet()){
            allPatterns.put(type,new ArrayList<>());
        }
    }

    public void addPattern(String classtype, Double[] value) {
        allPatterns.get(classtype).add(value);
    }

    public void createSubsetsPatterns(double collectionDivision, double collectionTrainingDivision) {
        for(Map.Entry<String,ArrayList<Double[]>> obj : allPatterns.entrySet()){
            Collections.shuffle(obj.getValue());
            //trainingSet ma procent zbioru do testowania, reszta do testowania, podział wg. collectionDivision
            List<Double[]> trainingSet = obj.getValue().subList(0, (int) (obj.getValue().size() * collectionDivision));
            testingPatterns.put(obj.getKey(), new ArrayList<>(obj.getValue().subList((int) (obj.getValue().size() * collectionDivision), obj.getValue().size())));
            //Wydzielenie zbioru walidacyjnego z zbioru testowego
            trainingPatterns.put(obj.getKey(), new ArrayList<>(trainingSet.subList(0, (int) (trainingSet.size()*collectionTrainingDivision))));
            validationPatterns.put(obj.getKey(), new ArrayList<>(trainingSet.subList((int) (trainingSet.size()*collectionTrainingDivision),trainingSet.size())));
        }
    }

    public List<Double[]> getTrainingInputs(){
        ArrayList<Double[]> inputs = new ArrayList<>();
        for(ArrayList<Double[]> obj : trainingPatterns.values()){
            inputs.addAll(obj);
        }
        return inputs;
    }


    public List<Double[]> getTrainingOutputs(){
        ArrayList<Double[]> outputs = new ArrayList<>();
        for(String obj: classTypes.keySet()){
            for (int i = 0; i < trainingPatterns.get(obj).size(); i++) {
                outputs.add(classTypes.get(obj));
            }
        }
        return outputs;
    }

    public List<Double[]> getValidationInputs(){
        ArrayList<Double[]> inputs = new ArrayList<>();
        for(ArrayList<Double[]> obj : validationPatterns.values()){
            inputs.addAll(obj);
        }
        return inputs;
    }

    public List<Double[]> getValidationOutputs(){
        ArrayList<Double[]> outputs = new ArrayList<>();
        for(String obj: classTypes.keySet()){
            for (int i = 0; i < validationPatterns.get(obj).size(); i++) {
                outputs.add(classTypes.get(obj));
            }
        }
        return outputs;
    }

    public List<Double[]> getTestingInputs(){
        ArrayList<Double[]> inputs = new ArrayList<>();
        for(ArrayList<Double[]> obj : testingPatterns.values()){
            inputs.addAll(obj);
        }
        return inputs;
    }

    public List<Double[]> getTestingOutputs(){
        ArrayList<Double[]> outputs = new ArrayList<>();
        for(String obj: classTypes.keySet()){
            for (int i = 0; i < testingPatterns.get(obj).size(); i++) {
                outputs.add(classTypes.get(obj));
            }
        }
        return outputs;
    }

    public HashMap<String, Integer> createClassificationMap(){
        HashMap<String, Integer> result = new HashMap<>();
        for(String key: classTypes.keySet()){
            result.put(key,0);
        }
        return result;
    }


}
