package auction;

public class ForwardAuction extends Auction {
    
    private float reservePrice;       // secret minimum price
    protected Bid currentBid;       // current highest buyer bid
    
    public ForwardAuction(int auctionId, String auctioneer, AuctionItem item, float reservePrice) {
        super(auctionId, auctioneer, item);
        this.reservePrice = reservePrice;
        currentBid = null;
    }

    @Override
    public void placeBid(Bid bid) throws Exception {
        if (isClosed) { throw new Exception("This auction is closed!"); }
        if (!bid.isBuyer()) { throw new Exception("This bid is from a seller!"); } 
        if (currentBid == null || bid.getPrice() > currentBid.getPrice()) {
            currentBid = bid;
        } else { throw new Exception("Bid is not higher than the current bid."); }
    }

    @Override
    public String closeAuction(String winner) throws Exception{
        isClosed = true;
        if (currentBid == null || currentBid.getPrice() < reservePrice) { return "Auction closed successfully. Reserve price not met."; }
        return "Auction closed successfully. Reserve price met. Item sold to: " + winner;
    }

    @Override
    public String getData() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("=====================================\n");
        sb.append("Auction ID: ").append(auctionId).append("\n");
        sb.append("Auction Type: Forward").append("\n");
        sb.append("Opened by: ").append(auctioneer).append("\n");
        sb.append("Item: ").append(item.getItemTitle()).append("\n");
        sb.append("Description: ").append(item.getItemDescription()).append("\n");
        if (currentBid != null) {
            sb.append("Current Bidding Price: ").append(currentBid.getPrice()).append("\n");
            sb.append("Bidder: ").append(currentBid.getBidder()).append("\n");
        } else {
            sb.append("Current Bidding Price: None\n");
            sb.append("Bidder: None\n");
        }
        sb.append("Auction Status: ").append(isClosed ? "Closed" : "Open").append("\n");
        sb.append("=====================================");
        return sb.toString();
    }
}