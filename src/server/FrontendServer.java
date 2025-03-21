package server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Base64;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicInteger;
import auction.Auction;
import auction.AuctionItem;
import auction.Bid;
import jgroups.GroupUtils;
import jgroups.MembershipListener;
import util.FileHandler;
import util.SignedResponse;

import org.jgroups.JChannel;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.RspList;
import org.jgroups.util.Rsp;
import java.rmi.Remote;


public class FrontendServer implements ICalc {

    // jgroups
    private static final int DISPATCHER_TIMEOUT = 1000;
    private RpcDispatcher dispatcher;

    private PrivateKey privateKey;

    private AtomicInteger auctionIdAllocator = new AtomicInteger(0);

    public FrontendServer() {
        super();
        JChannel groupChannel = GroupUtils.connect();
        dispatcher = new RpcDispatcher(groupChannel, this);
        dispatcher.setMembershipListener(new MembershipListener());

        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);  // 2048-bit RSA
            KeyPair pair = keyGen.generateKeyPair();
            privateKey = pair.getPrivate();
            FileHandler.writeObjectToFile(pair.getPublic(), "public.key");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            FrontendServer s = new FrontendServer();
            String name = "myserver";
            ICalc stub = (ICalc) UnicastRemoteObject.exportObject(s, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
            System.out.println("Server ready");
        } catch (Exception e) {
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }

    private SignedResponse signMessage(String message) {
        try {
            Signature rsa = Signature.getInstance("SHA256withRSA");
            rsa.initSign(privateKey);
            rsa.update(message.getBytes());
            byte[] signedBytes = rsa.sign();
            return new SignedResponse(message, Base64.getEncoder().encodeToString(signedBytes));
        } catch (Exception e) {
            e.printStackTrace();
            return new SignedResponse("Error signing message.", null);
        }
    }

    @Override
    public SignedResponse openAuction(Auction auction) throws RemoteException {
        try {
            RspList<String> responses = dispatcher.callRemoteMethods(
                null, 
                "openAuction",
                new Object[] { auction, auctionIdAllocator.incrementAndGet() }, 
                new Class[] { Auction.class, int.class },
                new RequestOptions(ResponseMode.GET_ALL, DISPATCHER_TIMEOUT)
            );
            return signMessage(returnNonNull(responses));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return signMessage("Error opening auction.");
    }

    @Override
    public SignedResponse closeAuction(int auctionId, String username, String winner) throws RemoteException {
        try {
            RspList<String> responses = dispatcher.callRemoteMethods(
                null, 
                "closeAuction",
                new Object[] { auctionId, username, winner }, 
                new Class[] { int.class, String.class, String.class },
                new RequestOptions(ResponseMode.GET_ALL, DISPATCHER_TIMEOUT)
            );
            return signMessage(returnNonNull(responses));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return signMessage("Error closing auction.");
    }

    @Override
    public SignedResponse placeBid(int auctionId, Bid bid) throws RemoteException {
        try {
            RspList<String> responses = dispatcher.callRemoteMethods(
                null, 
                "placeBid",
                new Object[] { auctionId, bid }, 
                new Class[] { int.class, Bid.class },
                new RequestOptions(ResponseMode.GET_ALL, DISPATCHER_TIMEOUT)
            );
            return signMessage(returnNonNull(responses));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return signMessage("Error placing bid.");
    }

    @Override
    public SignedResponse inspectAuctions() throws RemoteException {
        try {
            RspList<String> responses = dispatcher.callRemoteMethods(
                null, 
                "inspectAuctions",
                new Object[] {}, 
                new Class[] {},
                new RequestOptions(ResponseMode.GET_ALL, DISPATCHER_TIMEOUT)
            );
            return signMessage(returnNonNull(responses));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return signMessage("Error inspecting auctions.");
    }

    @Override
    public SignedResponse signUp(String username, String password) throws RemoteException {
        try {
            RspList<String> responses = dispatcher.callRemoteMethods(
                null, 
                "signUp",
                new Object[] { username, password }, 
                new Class[] { String.class, String.class },
                new RequestOptions(ResponseMode.GET_ALL, DISPATCHER_TIMEOUT)
            );
            return signMessage(returnNonNull(responses));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return signMessage("Error signing up.");
    }

    @Override
    public SignedResponse logIn(String username, String password) throws RemoteException {
        try {
            RspList<String> responses = dispatcher.callRemoteMethods(
                null, 
                "logIn",
                new Object[] { username, password }, 
                new Class[] { String.class, String.class },
                new RequestOptions(ResponseMode.GET_ALL, DISPATCHER_TIMEOUT)
            );
            return signMessage(returnNonNull(responses));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return signMessage("Error logging in.");
    }

    public <T> T returnNonNull(RspList<T> responses) {
        for (Rsp<T> rsp : responses) {  
            if (rsp.getValue() != null) {
                return rsp.getValue();  
            }
        }
        return null;
    }
}