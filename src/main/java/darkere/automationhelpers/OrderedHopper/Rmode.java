package darkere.automationhelpers.OrderedHopper;

public enum Rmode {
    roff (0,0,"roff"),
    ron(16,0,"ron"),
    on(32,0,"on"),
    off(48,0,"off");
    private int x,y;
    private String mode;
    public int getX(){
        return x;
    }
    public int getY(){
        return y;
    }
    public String getMode(){
        return mode;
    }

    @Override
    public String toString() {
        return getMode();
    }

    Rmode(int x, int y, String mode)
    {
        this.x = x;
        this.y = y;
        this.mode = mode;
    }

}