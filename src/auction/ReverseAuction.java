package auction;

import java.util.LinkedList;

public class ReverseAuction extends Auction {
    
    private LinkedList<Bid> sellerBids;  
    private Bid lowestBid;                  // keeping track of lowest bid for optimization
  
    public ReverseAuction(int auctionId, String aucitoneer, AuctionItem item) {
        // buyer is the creator of the auction
        super(auctionId, aucitoneer, item);
        lowestBid = new Bid(Float.MAX_VALUE, null, false);
        sellerBids = new LinkedList<Bid>();
    }

    @Override
    public void placeBid(Bid bid) throws Exception {
        if (isClosed) { throw new Exception("This auction is closed!"); }
        if (bid.isBuyer()) { throw new Exception("This auction is only available for sellers!"); } 
        // same seller cannot be added twice
        String seller = bid.getBidder();
        for (Bid sellerBid : sellerBids) {
            if (sellerBid.getBidder().equals(seller)) {
                sellerBids.remove(sellerBid);
                break;
            }
        }
        sellerBids.add(bid);
        if (bid.getPrice() < lowestBid.getPrice()) {
            lowestBid = bid;
        } else {
            throw new Exception("Warning: Bid is not lower than the current lowest bid.");
        }
    }

    @Override
    public String closeAuction(String winner) throws Exception{
        if (winner == null) { throw new Exception("Auction closed succesfully. No sellers available."); }
        isClosed = true;
        if (lowestBid.getBidder().equals(null)) { return "Auction closed successfully. No selling bids registered."; }
        if (winner.equals("")) { return "Auction closed successfully. Item bought from: " + lowestBid.getBidder(); }
        boolean found = false;
        for (Bid sellerBid : sellerBids) {
            if (sellerBid.getBidder().equals(winner)) {
                found = true;
                break;
            }
        }
        if (!found) { return "Auction closed successfully. Seller not found."; }
        return "Auction closed successfully. Item bought from: " + winner;
    }

    @Override
    public String getData() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("=====================================\n");
        sb.append("Auction ID: ").append(auctionId).append("\n");
        sb.append("Auction Type: Reverse").append("\n");
        sb.append("Opened by: ").append(auctioneer).append("\n");
        sb.append("Item: ").append(item.getItemTitle()).append("\n");
        sb.append("Description: ").append(item.getItemDescription()).append("\n");
        sb.append("-------------------------------------\n");
        // seller bids
        if (sellerBids.size() > 0) {
            sb.append("Seller Bids:\n");
            for (Bid bid : sellerBids) {
                sb.append("-------------------------------------\n");
                sb.append("\tSelling Price: ").append(bid.getPrice()).append("\n");
                sb.append("\tSeller: ").append(bid.getBidder()).append("\n");
            }
        } else { sb.append("No sellers registered.\n"); }
        sb.append("-------------------------------------\n");
        sb.append("Auction Status: ").append(isClosed ? "Closed" : "Open").append("\n");
        sb.append("=====================================");
        return sb.toString();
    }

    public LinkedList<Bid> getSellerBids() {
        return sellerBids;
    }

    public Bid getLowestBid() {
        return lowestBid;
    }
}
