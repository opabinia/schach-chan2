package org.rorschach.samplejunk;

import org.rorschach.complex.ValidationDriver;

public class Main {

    public static void main(String[] args) {
        // init object
        Bom bom = new Bom("1000000000", "2", "all work and no play makes jack a dull boy.");
        BomVerifier verifier = new BomVerifier();
        ValidationDriver<BomVerifier, Bom> driver = new ValidationDriver<>();

        // run validator
        System.out.println(driver.runValidation(verifier, bom));

        // get validation context and display console
        verifier.errorMsg.forEach(System.out::println);
    }

}
