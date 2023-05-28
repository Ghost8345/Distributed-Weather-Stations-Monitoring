package Bitcask;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class CLI {
    private static Bitcask bitcask;

    public static void main(String[] args) throws IOException, EntryNotFoundException {
        bitcask = new Bitcask();

        System.out.println("Bitcask CLI");
        System.out.println("Available commands: get <key>, put <key> <value>");

        File inputFile = new File("test/notest");

        if (inputFile.exists() && inputFile.isFile()) {
            readFromFile(inputFile);
        }
        realTimeRead();
    }

    private static void readFromFile(File inputFile) throws IOException, EntryNotFoundException {
        Scanner scanner = new Scanner(inputFile);
        while (scanner.hasNextLine()) {
            readOneCommand(scanner);
        }
        System.out.println(inputFile.getPath() + " successfully read");
        scanner.close();
    }

    private static void realTimeRead() throws IOException, EntryNotFoundException {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            boolean isDone = readOneCommand(scanner);
            if (isDone){
                System.out.println("Terminated");
                break;
            }
        }
    }

    private static boolean readOneCommand(Scanner scanner) throws EntryNotFoundException, IOException {
        String command = scanner.nextLine();
        if (command.equals("#")){
            return true;
        }
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
        return false;
    }

}
