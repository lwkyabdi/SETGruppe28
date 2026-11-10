package no.hiof.gruppe28;

import org.mindrot.jbcrypt.BCrypt;

import java.util.Scanner;

public class LagPassordHash {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Skriv passordet: ");
        String passord = scanner.nextLine();

        String hash = BCrypt.hashpw(passord, BCrypt.gensalt());

        System.out.println("Hash til passord_hash:");
        System.out.println(hash);
    }
}



