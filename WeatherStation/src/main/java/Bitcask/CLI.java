package Bitcask;

import java.io.IOException;
import java.util.Scanner;

public class CLI {
    public static void main(String[] args) throws IOException, EntryNotFoundException {
        Bitcask bitcask = new Bitcask();

        Scanner scanner = new Scanner(System.in);
        String command;

        System.out.println("Bitcask CLI");
        System.out.println("Available commands: get <key>, put <key> <value>");

        while (true) {
            System.out.print("> ");
            command = scanner.nextLine();

            String[] tokens = command.split(" ");
            if (tokens.length > 0) {
                String action = tokens[0];

                if (action.equals("get")) {
                    if (tokens.length == 2) {
                        String key = tokens[1];
                        byte[] value = bitcask.get(key);
                        String valueString = new String(value);
                        System.out.println(valueString);
                    } else {
                        System.out.println("Invalid command. Usage: get <key>");
                    }
                } else if (action.equals("put")) {
                    if (tokens.length == 3) {
                        String key = tokens[1];
                        byte[] value = tokens[2].getBytes();
                        bitcask.put(key, value);
                    } else {
                        System.out.println("Invalid command. Usage: put <key> <value>");
                    }
                } else {
                    System.out.println("Invalid command. Available commands: get <key>, put <key> <value>");
                }
            }
        }
    }
}
