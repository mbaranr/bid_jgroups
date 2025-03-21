package client;

import server.ICalc;
import util.SignedResponse;
import auction.AuctionItem;
import auction.Bid;
import auction.ForwardAuction;
import auction.ReverseAuction;
import auction.DoubleAuction;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.PublicKey;
import java.util.Scanner;
import util.FileHandler;

public class Client {

    private String username;
    private ICalc server;
    private Scanner scanner = new Scanner(System.in);
    private PublicKey publicKey;

    public Client() {
        try {
            publicKey = (PublicKey) FileHandler.readObjectFromFile("public.key");
        } catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.initialize();
        client.run();
    }

    public void initialize() {
        try {
            String name = "myserver";
            Registry registry = LocateRegistry.getRegistry("localhost");
            this.server = (ICalc) registry.lookup(name);
            
            System.out.println("=====================================");
            System.out.println("Welcome to the Auction House!");
            System.out.println("1. Sign Up");
            System.out.println("2. Log In");
            System.out.print("Enter your choice: ");
            int choice = getIntInput();
            
            if (choice == 1) {
                signUp();
                System.out.println("Now log in to continue.");
                while (!logIn()) {
                    System.out.println("Please try again.");
                }
            } else if (choice == 2) {
                while (!logIn()) {
                    System.out.println("Invalid credentials. Try again.");
                }
            } else {
                System.out.println("Invalid choice. Exiting...");
                System.exit(0);
            }
        } catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }

    public void signUp() {
        System.out.println("=====================================");
        System.out.print("Enter username: ");
        String newUsername = scanner.nextLine();
        System.out.print("Enter password: ");
        String newPassword = scanner.nextLine();
        System.out.println("=====================================");
    
        try {
            SignedResponse response = server.signUp(newUsername, newPassword);
            System.out.println(getVerifiedResponse(response));
        } catch (Exception e) {
            System.err.println("Exception during sign-up:");
            e.printStackTrace();
        }
    }
    
    public boolean logIn() {
        System.out.println("=====================================");
        System.out.print("Enter username: ");
        String inputUsername = scanner.nextLine();
        System.out.print("Enter password: ");
        String inputPassword = scanner.nextLine();
        System.out.println("=====================================");

    
        try {
            SignedResponse response = server.logIn(inputUsername, inputPassword);
            String message = getVerifiedResponse(response);
    
            if (message.equals("Login successful!")) {
                this.username = inputUsername; // Store logged-in username
                System.out.println("You are now logged in.");
                return true;
            } else {
                System.out.println(message);
                return false;
            }
        } catch (Exception e) {
            System.err.println("Exception during login:");
            e.printStackTrace();
            return false;
        }
    }

    private String getVerifiedResponse(SignedResponse response) {
        if (!response.verify(publicKey)) {
            throw new SecurityException("Signature verification failed! Possible tampering detected.");
        }
        return response.getMessage();
    }

    public void run() {
        while (true) {
            System.out.println("=====================================");
            System.out.println("Main Menu:");
            System.out.println("1. Open Auction");
            System.out.println("2. Place Bid");
            System.out.println("3. Inspect Auctions");
            System.out.println("4. Close Auction");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");
            int choice = getIntInput();
            System.out.println("=====================================");

            switch (choice) {
                case 1:
                    openAuction();
                    break;
                case 2:
                    placeBid();
                    break;
                case 3:
                    getAuctionDetails();
                    break;
                case 4:
                    closeAuction();
                    break;
                case 5:
                    System.out.println("Exiting...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void openAuction() {
        AuctionItem item = enterItemData();
        boolean back = false;
        while (!back) {
            System.out.println("=====================================");
            System.out.println("Auction Types:");
            System.out.println("1. Forward");
            System.out.println("2. Reverse");
            System.out.println("3. Double");
            System.out.println("4. Return to Main Menu");
            System.out.print("Enter your choice: ");
            int choice = getIntInput();
            System.out.println("=====================================");
            try{ 
                switch (choice) {
                    case 1:
                        System.out.print("Enter reserve price: ");
                        float reservePrice = getFloatInput();
                        String auctionId = getVerifiedResponse(server.openAuction(new ForwardAuction(-1, username, item, reservePrice)));
                        System.out.println("Auction opened with ID: " + auctionId);
                        back = true;
                        break;
                    case 2:
                        auctionId = getVerifiedResponse(server.openAuction(new ReverseAuction(-1, username, item)));
                        System.out.println("Auction opened with ID: " + auctionId);
                        back = true;
                        break;
                    case 3:
                        auctionId = getVerifiedResponse(server.openAuction(new DoubleAuction(-1, username, item)));
                        System.out.println("Auction opened with ID: " + auctionId);
                        back = true;
                        break;
                    case 4:
                        back = true;
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                System.err.println("Exception:");
                e.printStackTrace();
            }
        }
    }

    private void placeBid() {
        System.out.print("Enter auction ID: ");
        int auctionId = getIntInput();
        System.out.print("Enter price: ");
        float bidPrice = getFloatInput();
        System.out.print("Are you trying to sell this item? (y/n): ");
        boolean isBuyer = !getBooleanInput();
        try {
            String response = getVerifiedResponse(server.placeBid(auctionId, new Bid(bidPrice, username, isBuyer)));
            System.out.println(response);
        } catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }

    private void getAuctionDetails() {
        try {
            String details = getVerifiedResponse(server.inspectAuctions());
            System.out.println(details);
        } catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }

    private void closeAuction() {
        System.out.print("Enter auction ID: ");
        int auctionId = getIntInput();
        try {
            String response = getVerifiedResponse(server.closeAuction(auctionId, username, null));
            String[] lines = response.split("\n");
            // check for code
            if (lines.length > 0 && lines[0].equals("resend")) {
                // print response without code
                for (int i = 1; i < lines.length; i++) { System.out.println(lines[i]); }
                System.out.print("Enter seller name (empty for *): ");
                String seller = scanner.nextLine();
                response = getVerifiedResponse(server.closeAuction(auctionId, username, seller));
            }
            System.out.println(response);
        } catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }

    public AuctionItem enterItemData() {
        System.out.println("Enter item data:");
        System.out.print("Item ID: ");
        int itemId = getIntInput();
        System.out.print("Item Name: ");
        String itemTitle = scanner.nextLine();
        System.out.print("Item Description: ");
        String itemDescription = scanner.nextLine();
        System.out.print("Is item used? (y/n): ");
        boolean isUsed = getBooleanInput();
        return new AuctionItem(itemId, itemTitle, itemDescription, isUsed);
    }

    private float getFloatInput() {
        while (true) {
            try {
                return Float.parseFloat(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a valid float number: ");
            }
        }
    }

    private int getIntInput() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a valid number: ");
            }
        }
    }

    private boolean getBooleanInput() {
        while (true) {
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("y")) {
                return true;
            } else if (input.equals("n")) {
                return false;
            } else {
                System.out.print("Invalid input. Please enter 'y' or 'n': ");
            }
        }
    }
}