
public class Node {
    //The seat object associated with this node
    private Seat seat;
    //Reference to the next seat in the same row (right neighbor)
    private Node right;
    //Reference to the seat directly below in the next row (down neighbor)
    private Node down;
    
    //Constructor to create a new node with a given seat
    //Initializes links to null (no neighbors initially)
    public Node(Seat seat) {
        this.seat = seat;
        this.right = null;
        this.down = null;
    }
    
    //Returns the seat object stored in this node
    public Seat getSeat() {
        return seat;
    }
    
    //Returns the reference to the node on the right (same row)
    public Node getRight() {
        return right;
    }
    
    //Sets the reference to the node on the right (same row)
    public void setRight(Node right) {
        this.right = right;
    }
    
    //Returns the reference to the node below (next row)
    public Node getDown() {
        return down;
    }
    
    //Sets the reference to the node below (next row)
    public void setDown(Node down) {
        this.down = down;
    }
}