package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import auction.Auction;
import auction.Bid;
import util.SignedResponse;

public interface ICalc extends Remote {
    public SignedResponse openAuction (Auction auction) throws RemoteException;
    public SignedResponse closeAuction (int auctionId, String username, String winner) throws RemoteException;
    public SignedResponse placeBid (int auctionId, Bid bid) throws RemoteException;
    public SignedResponse inspectAuctions () throws RemoteException;
    public SignedResponse signUp(String username, String password) throws RemoteException;
    public SignedResponse logIn(String username, String password) throws RemoteException;
}
