package ua.kpi.ipze.ontology.service.io;

import ua.kpi.ipze.ontology.dto.ClassRelation;

import java.util.Scanner;

public class ConsoleIOService implements IOService {

    @Override
    public ClassRelation askForRelation(String class1, String class2) {
        Scanner input = new Scanner(System.in);
        System.out.println("Are " +
                class1 +
                " in system ontology and " +
                class2 +
                " from uploaded ontology equal? ( 1 - yes, 2 - first class is subclass of second one, 3 - second class is subclass of the first, 4 - no");
        String answer = input.nextLine();
        return ClassRelation.readFromOption(answer);
    }

}
