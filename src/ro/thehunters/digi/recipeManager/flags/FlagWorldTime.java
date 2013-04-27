package ro.thehunters.digi.recipeManager.flags;

public class FlagWorldTime extends Flag
{
    // Flag definition and documentation
    
    private static final FlagType TYPE;
    protected static final String[] A;
    protected static final String[] D;
    protected static final String[] E;
    
    static
    {
        TYPE = FlagType.WORLDTIME;
        
        A = new String[]
        {
            "{flag} ...",
        };
        
        D = new String[]
        {
            "FLAG NOT YET IMPLEMENTED!",
        };
        
        E = new String[]
        {
            "{flag} ...",
        };
    }
    
    // Flag code
    
    public FlagWorldTime()
    {
    }
}