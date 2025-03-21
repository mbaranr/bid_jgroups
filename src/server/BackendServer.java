package server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import auction.Auction;
import auction.AuctionItem;
import auction.Bid;
import auction.ReverseAuction;
import jgroups.GroupUtils;

import org.jgroups.JChannel;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.RspList;
import org.jgroups.util.Rsp;

public class BackendServer implements IAuctionService {
    private Map<Integer, Auction> auctions = new HashMap<>();
    private Map<String, String> userDatabase = new HashMap<>();

    // jgroups
    private RpcDispatcher dispatcher;
    private static final int DISPATCHER_TIMEOUT = 1000;

    public BackendServer() {
        super();

        JChannel groupChannel = GroupUtils.connect();
        dispatcher = new RpcDispatcher(groupChannel, this);
        
        // synchronize at startup
        syncStates();
    }

    public static void main(String[] args) {
        try {
            BackendServer s = new BackendServer();
        } catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }

    public void syncStates() {
        try {
            System.out.println("Syncing auctions with existing servers...");
    
            RspList<Map<Integer, Auction>> incomingAuctions = dispatcher.callRemoteMethods(
                null,
                "getAuctions",
                new Object[]{}, 
                new Class[]{},
                new RequestOptions(ResponseMode.GET_ALL, DISPATCHER_TIMEOUT)
            );

            RspList<Map<String, String>> incomingUsers = dispatcher.callRemoteMethods(
                null,
                "getUserDatabase",
                new Object[]{}, 
                new Class[]{},
                new RequestOptions(ResponseMode.GET_ALL, DISPATCHER_TIMEOUT)
            );
    
            if (!incomingAuctions.isEmpty()) {
                for (Rsp<Map<Integer, Auction>> rsp : incomingAuctions) {  // Iterate over all responses
                    Map<Integer, Auction> receivedAuctions = rsp.getValue();
                    if (receivedAuctions != null) {
                        System.out.println("Found server for synchronization.");
                        for (Map.Entry<Integer, Auction> entry : receivedAuctions.entrySet()) {
                            if (!auctions.containsKey(entry.getKey())) {  // Avoid overwriting existing auctions
                                auctions.put(entry.getKey(), entry.getValue());
                            }
                        }
                    }
                }
                System.out.println("Auctions synchronized successfully.");
            } else {
                System.out.println("No active servers for auction synchronization.");
            }

            if (!incomingUsers.isEmpty()) {
                for (Rsp<Map<String, String>> rsp : incomingUsers) {  // Iterate over all responses
                    Map<String, String> receivedUsers = rsp.getValue();
                    if (receivedUsers != null) {
                        System.out.println("Found server for synchronization.");
                        for (Map.Entry<String, String> entry : receivedUsers.entrySet()) {
                            if (!userDatabase.containsKey(entry.getKey())) {  // Avoid overwriting existing auctions
                                userDatabase.put(entry.getKey(), entry.getValue());
                            }
                        }
                    }
                }
                System.out.println("Users synchronized successfully.");
            } else {
                System.out.println("No active servers for user synchronization.");
            }

        } catch (Exception e) {
            System.err.println("Error syncing states: " + e.getMessage());
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) { return e.getMessage(); }
    }

    private boolean verifyPassword(String inputPassword, String storedHash) {
        return hashPassword(inputPassword).equals(storedHash);
    }

    @Override
    public String signUp(String username, String password) {
        if (userDatabase.containsKey(username)) { return "Username already exists!"; }
        userDatabase.put(username, hashPassword(password));
        return "Account created successfully!";
    }

    @Override
    public String logIn(String username, String password) {
        if (!userDatabase.containsKey(username)) { return "Username does not exist!"; }
        if (!verifyPassword(password, userDatabase.get(username))) { return "Incorrect password!"; }
        return "Login successful!";
    }

    @Override
    public String openAuction(Auction auction, int id) {
        auction.setAuctionId(id);
        auctions.put(id, auction);
        return ""+id;
    }

    @Override
    public String closeAuction(int auctionId, String username, String winner) {
        Auction auction = auctions.get(auctionId);

        if (auction.isClosed()) return "This auction is already closed.";
        if (!auction.getAuctioneer().equals(username)) return "This auction is not yours!";

        // handle special case for reverse auctions
        if (auction instanceof ReverseAuction && winner == null) {
            return handleReverseAuction((ReverseAuction) auction, username);
        }

        String result;
        try {
            result = auction.closeAuction(winner);
        } catch (Exception e) {
            result = e.getMessage();
        }
        return result;
    }

    /**
     * Handles the closing logic for reverse auctions, where the winner is not specified.
     */
    private String handleReverseAuction(ReverseAuction auction, String username) {
        LinkedList<Bid> sellerBids = auction.getSellerBids();
        try { auction.closeAuction(null); } 
        catch (Exception e) {
            if (sellerBids.isEmpty()) return e.getMessage();
        }
        StringBuilder sb = new StringBuilder("resend\n").append("=====================================\n")
            .append("Available Sellers:\n").append("-------------------------------------\n");

        Bid lowestBid = auction.getLowestBid();
        for (Bid bid : sellerBids) {
            sb.append(bid.equals(lowestBid) ? "*" : " ")
            .append(String.format("Name: %s | Selling Price: %f\n", 
                    bid.getBidder(), bid.getPrice()));
        }
        sb.append("-------------------------------------\n");
        return sb.toString();
    }

    @Override
    public String placeBid(int auctionId, Bid bid) {
        Auction auction = auctions.get(auctionId);
        try {
            auction.placeBid(bid);
            return "Bid placed successfully.";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @Override
    public String inspectAuctions() {
        StringBuilder sb = new StringBuilder();
        int auctionCount = auctions.size();
        int currentIndex = 0;

        for (Integer k : auctions.keySet()) {
            Auction auction = auctions.get(k);
            try {
                sb.append(auction.getData());
            } catch (Exception e) {
                sb.append(e.getMessage());
            }
            currentIndex++;
            if (currentIndex < auctionCount) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
    
    public Map<Integer, Auction> getAuctions() {
        return auctions;
    }

    public Map<String, String> getUserDatabase() {
        return userDatabase;
    }
}