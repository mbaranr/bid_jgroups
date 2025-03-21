package auction;

import java.io.Serializable;

public abstract class Auction implements Serializable {
    
    protected AuctionItem item;                    // item being auctioned
    protected boolean isClosed;
    protected String auctioneer;
    protected int auctionId;

    public Auction(int auctionId, String auctioneer, AuctionItem item) {
        this.item = item;   
        this.auctionId = auctionId;   
        this.auctioneer = auctioneer;
        this.isClosed = false;
    }

    public abstract String closeAuction(String winner) throws Exception;

    public abstract void placeBid(Bid bid) throws Exception;

    public abstract String getData() throws Exception;
    
    public boolean isClosed() {
        return isClosed;
    }

    public int getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(int auctionId) {
        this.auctionId = auctionId;
    }

    public AuctionItem getItem() {
        return item;
    }   

    public String getAuctioneer() {
        return auctioneer;
    }
}
