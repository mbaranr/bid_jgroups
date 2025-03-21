package auction;

import java.io.Serializable;

public class AuctionItem  implements Serializable{
    
    private int itemId;
    private String itemTitle;
    private String itemDescription;
    private boolean isUsed;
    
    public AuctionItem(int itemId, String itemTitle, String itemDescription, boolean isUsed) {
        this.itemId = itemId;
        this.itemTitle = itemTitle;
        this.itemDescription = itemDescription;
        this.isUsed = isUsed;
    }

    public boolean isUsed() {
        return isUsed;
    }

    public int getItemId() {
        return itemId;
    }

    public String getItemTitle() {
        return itemTitle;
    }   

    public String getItemDescription() {
        return itemDescription;
    }
}
