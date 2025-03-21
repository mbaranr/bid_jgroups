package server;

import auction.Auction;
import auction.Bid;

public interface IAuctionService {
    public String openAuction (Auction auction, int id);
    public String closeAuction (int auctionId, String username, String winner);
    public String placeBid (int auctionId, Bid bid);
    public String inspectAuctions ();
    public String signUp(String username, String password);
    public String logIn(String username, String password);
}