package ro.thehunters.digi.recipeManager.recipes;

import org.bukkit.inventory.Recipe;

import ro.thehunters.digi.recipeManager.RecipeManager;
import ro.thehunters.digi.recipeManager.Recipes;
import ro.thehunters.digi.recipeManager.flags.Arguments;
import ro.thehunters.digi.recipeManager.flags.Flag;
import ro.thehunters.digi.recipeManager.flags.FlagType;
import ro.thehunters.digi.recipeManager.flags.Flaggable;
import ro.thehunters.digi.recipeManager.flags.Flags;

public class BaseRecipe implements Flaggable
{
    public enum RecipeType
    {
        ANY(null),
        CRAFT("craft"),
        COMBINE("combine"),
        WORKBENCH(null),
        SMELT("smelt"),
        SMELTPAIR("smeltpair"),
        FUEL("fuel");
        
        private final String directive;
        
        private RecipeType(String directive)
        {
            this.directive = directive;
        }
        
        public String getDirective()
        {
            return directive;
        }
    }
    
    private Flags flags;
    protected int hash;
    
    public BaseRecipe()
    {
    }
    
    public BaseRecipe(BaseRecipe recipe)
    {
        this.flags = recipe.getFlags().clone(this);
    }
    
    public BaseRecipe(Flags flags)
    {
        this.flags = flags.clone(this);
    }
    
    /**
     * See: {@link Recipes #getRecipeInfo(BaseRecipe)}
     * 
     * @return
     *         Recipe info or null if doesn't exist
     */
    public RecipeInfo getInfo()
    {
        return RecipeManager.getRecipes().getRecipeInfo(this);
    }
    
    public boolean checkFlags(Arguments a)
    {
        return (flags == null ? true : flags.checkFlags(a));
    }
    
    public boolean applyFlags(Arguments a)
    {
        return (flags == null ? true : flags.applyFlags(a));
    }
    
    public void sendFailed(Arguments a)
    {
        if(flags != null)
            flags.sendFailed(a);
    }
    
    public RecipeType getRecipeType()
    {
        return null;
    }
    
    public boolean isValid()
    {
        return false; // empty recipe, invalid!
    }
    
    public int getIndex()
    {
        return hash;
    }
    
    @Override
    public int hashCode()
    {
        return hash;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if(obj == this)
            return true;
        
        if(obj == null || obj instanceof BaseRecipe == false)
            return false;
        
        return obj.hashCode() == hash;
    }
    
    /**
     * Register recipe with the server and RecipeManager.<br>
     * Alias for RecipeManager.getRecipes().registerRecipe(this);
     */
    public void register()
    {
        if(!isValid())
            throw new IllegalArgumentException("Recipe is invalid ! Check ingredients and results.");
        
        RecipeManager.getRecipes().registerRecipe(this);
    }
    
    /**
     * Remove this recipe from the server and from RecipeManager.<br>
     * Alias for: RecipeManager.getRecipes().removeRecipe(this);
     * 
     * @return if recipe was succesfully removed
     */
    public boolean remove()
    {
        return RecipeManager.getRecipes().removeRecipe(this);
    }
    
    /**
     * You usually won't need this, but just in case you do, here it is.
     * 
     * @return
     *         Bukkit API version of the recipe
     */
    public Recipe toBukkitRecipe()
    {
        if(this instanceof CraftRecipe)
        {
            return ((CraftRecipe)this).toShapedRecipe();
        }
        else if(this instanceof CombineRecipe)
        {
            return ((CombineRecipe)this).toShapelessRecipe();
        }
        else if(this instanceof SmeltRecipe)
        {
            return ((SmeltRecipe)this).toFurnaceRecipe();
        }
        
        return null;
    }
    
    // From Flaggable interface
    
    @Override
    public boolean hasFlag(FlagType type)
    {
        return (flags == null ? false : flags.hasFlag(type));
    }
    
    @Override
    public boolean hasFlags()
    {
        return (flags != null);
    }
    
    @Override
    public Flag getFlag(FlagType type)
    {
        return flags.getFlag(type);
    }
    
    @Override
    public <T extends Flag>T getFlag(Class<T> flagClass)
    {
        return flags.getFlag(flagClass);
    }
    
    @Override
    public Flags getFlags()
    {
        if(flags == null)
            flags = new Flags(this);
        
        return flags;
    }
    
    @Override
    public void addFlag(Flag flag)
    {
        flags.addFlag(flag);
    }
}
