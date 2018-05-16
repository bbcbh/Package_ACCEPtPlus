/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import person.AbstractIndividualInterface;

/**
 *
 * @author Bhui
 */
public class Classifier_Gender_Infection implements PersonClassifier {

    @Override
    public int classifyPerson(AbstractIndividualInterface p) {
        return p.isMale()? 0:1;
    }

    @Override
    public int numClass() {
        return 2;
    }
    
    
}
