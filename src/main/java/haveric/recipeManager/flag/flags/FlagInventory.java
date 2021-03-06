package haveric.recipeManager.flag.flags;

import haveric.recipeManager.ErrorReporter;
import haveric.recipeManager.flag.Flag;
import haveric.recipeManager.flag.FlagType;
import haveric.recipeManager.flag.args.Args;
import haveric.recipeManagerCommon.util.RMCUtil;
import org.bukkit.event.inventory.InventoryType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FlagInventory extends Flag {

    @Override
    public String getFlagType() {
        return FlagType.INVENTORY;
    }

    @Override
    protected String[] getArguments() {
        return new String[] {
            "{flag} <inventory type> , ... | [arguments]", };
    }

    @Override
    protected String[] getDescription() {
        return new String[] {
            "Checks if crafting in the specific type of inventory",
            "",
            "The <inventory type> argument is required",
            "  Values: " + RMCUtil.collectionToString(Arrays.asList(InventoryType.values())).toLowerCase(),
            "",
            "Can declare multiple inventory types separated by commas",
            "",
            "",
            "Optional arguments:",
            "  title <text>      - Add an inventory title restriction",
            "",
            "  failmsg <message> - Overwrite the fail message or you can use 'false' to hide it.",
            "    In the message the following variables can be used:",
            "      {inventory} = name of inventory type(s)",
            "      {title}     = title of inventory", };
    }

    @Override
    protected String[] getExamples() {
        return new String[] {
            "{flag} crafting // Player crafting menu",
            "{flag} workbench // Must use a crafting table",
            "{flag} workbench | title Custom // Must use a crafting table named 'Custom'", };
    }


    private List<InventoryType> inventories = new ArrayList<>();
    private String title;
    private String failMessage;

    public FlagInventory() {
    }

    public FlagInventory(FlagInventory flag) {
        inventories = flag.inventories;
        title = flag.title;
        failMessage = flag.failMessage;
    }

    @Override
    public FlagInventory clone() {
        return new FlagInventory((FlagInventory) super.clone());
    }

    public List<InventoryType> getInventories() {
        return inventories;
    }

    public void setInventories(List<InventoryType> listInventories) {
        inventories = listInventories;
    }

    public void addInventory(InventoryType inventory) {
        inventories.add(inventory);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String newTitle) {
        title = newTitle;
    }

    public boolean hasTitle() {
        return title != null;
    }

    public String getFailMessage() {
        return failMessage;
    }

    public void setFailMessage(String newFailMessage) {
        failMessage = newFailMessage;
    }

    @Override
    public boolean onParse(String value) {
        String[] split = value.split("\\|");

        if (split.length > 1) {
            for (int i = 1; i < split.length; i++) {
                value = split[i].trim();

                if (value.toLowerCase().startsWith("title")) {
                    setTitle(value.substring("title".length()).trim());
                } else if (value.toLowerCase().startsWith("failmsg")) {
                    setFailMessage(value.substring("failmsg".length()).trim());
                }
            }
        }

        split = split[0].toLowerCase().split(",");

        for (String arg : split) {
            try {
                addInventory(InventoryType.valueOf(arg.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                return ErrorReporter.getInstance().error("Flag " + getFlagType() + "  has unknown inventory type(s): " + value);
            }
        }

        return true;
    }

    @Override
    public void onCheck(Args a) {
        boolean success = false;

        if (a.hasInventory()) {
            InventoryType craftedType = a.inventory().getType();

            for (InventoryType type : getInventories()) {
                if (craftedType.equals(type)) {
                    success = true;
                    break;
                }
            }

            if (hasTitle()) {
                success = getTitle().equals(a.inventory().getTitle());
            }
        }

        if (!success) {
            a.addReason("flag.inventory", failMessage, "{inventory}", getInventories().toString(), "{title}", a.inventory().getTitle());
        }
    }
}
