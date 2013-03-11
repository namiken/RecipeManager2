package ro.thehunters.digi.recipeManager.flags;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

public enum FlagType
{
    // Shared flags
    MESSAGE(FlagMessage.class, Bit.NONE, "craftmsg"),
    COMMANDS(FlagCommands.class, Bit.NONE, "command", "cmd"),
    PERMISSION(FlagPermission.class, Bit.NONE, "perm"),
    HOLD(Flag.class, Bit.NONE),
    PLAYTIME(Flag.class, Bit.NONE),
    ONLINETIME(Flag.class, Bit.NONE),
    GAMEMODE(Flag.class, Bit.NONE),
    MODEXP(FlagModExp.class, Bit.NONE, "expmod", "modxp", "xpmod", "exp", "xp"),
    REQEXP(FlagReqExp.class, Bit.NONE, "expreq", "reqxp", "xpreq", "needexp", "needxp"),
    MODLEVEL(Flag.class, Bit.NONE, "levelmod", "level"),
    REQLEVEL(Flag.class, Bit.NONE, "levelreq", "needlevel"),
    MODMONEY(Flag.class, Bit.NONE, "moneymod", "money"),
    REQMONEY(Flag.class, Bit.NONE, "moneyreq", "needmoney"),
    LAUNCHFIREWORK(FlagLaunchFirework.class, Bit.NONE),
    SOUND(FlagSound.class, Bit.NONE, "playsound"),
    EFFECT(FlagEffect.class, Bit.NONE, "playeffect", "fx"), // TODO finish
    CREATURE(FlagCreature.class, Bit.NONE, "spawncreature"), // TODO finish
    SECRET(FlagSecret.class, Bit.NO_VALUE, "hide"),
    DEBUG(FlagDebug.class, Bit.NO_VALUE, "monitor", "log"),
    
    // Recipe only flags
    DESCRIPTION(FlagDescription.class, Bit.RECIPE, "recipeinfo", "info"),
    FAILMESSAGE(FlagFailMessage.class, Bit.RECIPE, "failmsg"),
    HIDERESULTS(FlagHideResults.class, Bit.RECIPE | Bit.NO_VALUE),
    GETBOOK(FlagGetBook.class, Bit.RECIPE, "getrecipebook", "recipebook"), // TODO finsih
    REMOVE(FlagRemove.class, Bit.RECIPE | Bit.NO_VALUE, "delete"),
    RESTRICT(FlagRestrict.class, Bit.RECIPE | Bit.NO_VALUE, "denied", "deny"),
    OVERRIDE(FlagOverride.class, Bit.RECIPE | Bit.NO_VALUE, "overwrite", "supercede", "replace"),
    
    // Result only flags
    CLONEINGREDIENT(FlagCloneIngredient.class, Bit.RESULT, "clone", "copy", "copyingredient"),
    NAME(FlagName.class, Bit.RESULT | Bit.NO_STORE, "itemname", "displayname"),
    LORE(FlagLore.class, Bit.RESULT | Bit.NO_STORE, "itemlore", "itemdescription"),
    LEATHERCOLOR(FlagLeatherColor.class, Bit.RESULT | Bit.NO_STORE, "leathercolour", "color", "colour", "itemcolor", "itemcolour"),
    BOOK(FlagBook.class, Bit.RESULT | Bit.NO_STORE, "bookitem", "itembook"),
    BOOKPAGE(FlagBookPage.class, Bit.RESULT | Bit.NO_STORE, "bookitempage", "page", "addpage"),
    MAP(FlagMap.class, Bit.RESULT | Bit.NO_STORE, "mapitem", "itemmap"),
    FIREWORK(FlagFirework.class, Bit.RESULT | Bit.NO_STORE, "fireworkrocket"),
    FIREWORKCHARGE(FlagFireworkCharge.class, Bit.RESULT | Bit.NO_STORE, "fireworkeffect"),
    SKULL(FlagSkull.class, Bit.RESULT | Bit.NO_STORE, "skullowner"),
    POTION(FlagPotion.class, Bit.RESULT | Bit.NO_STORE, "potionitem"),
    ENCHANT(FlagEnchant.class, Bit.RESULT | Bit.NO_STORE, "enchantment"),
    ENCHANTBOOK(FlagEnchantBook.class, Bit.RESULT | Bit.NO_STORE, "enchantedbook");
    
    private final Class<? extends Flag> flagClass;
    private final String[]              names;
    private final int                   bits;
    
    private FlagType(Class<? extends Flag> flagClass, int bits, String... aliases)
    {
        this.flagClass = flagClass;
        this.bits = bits;
        
        this.names = new String[aliases.length + 1];
        this.names[0] = name().toLowerCase();
        
        for(int i = 0; i < aliases.length; i++)
        {
            this.names[i + 1] = aliases[i];
        }
    }
    
    public boolean hasBit(int bit)
    {
        return (bits & bit) == bit;
    }
    
    /**
     * @return the class asigned to this type (not the instance)
     */
    public Class<? extends Flag> getFlagClass()
    {
        return flagClass;
    }
    
    public String[] getNames()
    {
        return names;
    }
    
    /**
     * @return a new instance of the class asigned to this type or null if failed and prints stack trace.
     */
    public Flag createFlagClass()
    {
        try
        {
            return flagClass.newInstance();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Gets the <code>@flag</code> style flag name
     */
    public String toString()
    {
        return "@" + name().toLowerCase();
    }
    
    // Static stuff
    
    private static final Map<String, FlagType>                nameMap  = new HashMap<String, FlagType>();
    private static final Map<Class<? extends Flag>, FlagType> classMap = new HashMap<Class<? extends Flag>, FlagType>();
    
    static
    {
        Permission parent = new Permission("recipemanager.noflag.*", PermissionDefault.FALSE);
        Permission p;
        Bukkit.getPluginManager().addPermission(parent);
        
        for(FlagType type : values())
        {
            classMap.put(type.getFlagClass(), type);
            
            for(String name : type.names)
            {
                nameMap.put(name, type);
                
                p = new Permission("recipemanager.noflag." + name, PermissionDefault.FALSE);
                p.addParent(parent, true);
                Bukkit.getPluginManager().addPermission(p);
            }
        }
    }
    
    /**
     * Get the FlagType object for inputted flag name or alias.<br>
     * This method is faster than {@link #compare(String)} because it uses a HashMap to look for the name.
     * 
     * @param flag
     * @return
     */
    public static FlagType getByName(String flag)
    {
        return nameMap.get(flag);
    }
    
    public static FlagType getByClass(Class<? extends Flag> flagClass)
    {
        return classMap.get(flagClass);
    }
    
    /**
     * Flag bits to configure special behaviour
     */
    public class Bit
    {
        public static final byte NONE     = 0;
        
        /**
         * Flag only works in recipes
         */
        public static final byte RECIPE   = 1 << 1;
        
        /**
         * Flag only works on results
         */
        public static final byte RESULT   = 1 << 2;
        
        /**
         * No value is allowed for this flag
         */
        public static final byte NO_VALUE = 1 << 3;
        
        /**
         * Disables flag from being stored - used on flags that directly affect result's metadata
         */
        public static final byte NO_STORE = 1 << 4;
        
        /**
         * Disables "false" or "remove" values from removing the flag
         */
        public static final byte NO_FALSE = 1 << 5;
    }
}