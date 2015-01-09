package haveric.recipeManager.flags;

import haveric.recipeManager.ErrorReporter;
import haveric.recipeManager.RecipeManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


public class FlagExplode extends Flag {
    // Flag definition and documentation

    private static final FlagType TYPE = FlagType.EXPLODE;
    protected static final String[] A = new String[] {
        "{flag} <arguments or false>", };

    protected static final String[] D = new String[] {
        "Makes the workbench/furnace/player explode when recipe is crafted.",
        "This flag can only be declared once per recipe and once per result.",
        "",
        "Replace <arguments> with the following arguments separated by | character:",
        "  power <0.0 to ...>     = (default 2.0) Set the explosion power, value multiplied by 2 is the range in blocks; TNT has 4.0",
        "  fire                   = (default not set) Explosion sets fires.",
        "  nobreak                = (default not set) Makes explosion not break blocks.",
        "  nodamage [self]        = (default not set) Explosion doesn't damage players or only the crafter if 'self' is specified.",
        "  fail                   = (default not set) Explode if recipe failed as opposed to succeed.",
        "All arguments are optional and you can specify these arguments in any order.", };

    protected static final String[] E = new String[] {
        "{flag} // will explode when recipe succeeds with power 2, 100% chance and breaks blocks",
        "{flag} nobreak | fire | chance 25% | power 6 // will explode 25% of time without block damage but sets fires",
        "{flag} fail | power 2 | chance 75% // will explode 75% of the time when recipe fails", };

    // Flag code

    private float power = 2.0f;
    private boolean fire = false;
    private boolean noBreak = false;
    private byte noDamage = 0;
    private boolean failure = false;

    public FlagExplode() {
    }

    public FlagExplode(FlagExplode flag) {
        power = flag.power;
        fire = flag.fire;
        noBreak = flag.noBreak;
        noDamage = flag.noDamage;
        failure = flag.failure;
    }

    @Override
    public FlagExplode clone() {
        super.clone();
        return new FlagExplode(this);
    }

    @Override
    public FlagType getType() {
        return TYPE;
    }

    public float getPower() {
        return power;
    }

    public void setPower(float newPower) {
        power = newPower;
    }

    public boolean getFire() {
        return fire;
    }

    public void setFire(boolean newFire) {
        fire = newFire;
    }

    public boolean getFailure() {
        return failure;
    }

    public void setFailure(boolean newFailure) {
        failure = newFailure;
    }

    public boolean getNoBreak() {
        return noBreak;
    }

    public void setNoBreak(boolean newNoBreak) {
        noBreak = newNoBreak;
    }

    public boolean isNoDamageEnabled() {
        return noDamage > 0;
    }

    public boolean isNoDamageSelf() {
        return noDamage == 2;
    }

    public void setNoDamage(boolean enable) {
        setNoDamage(enable, false);
    }

    public void setNoDamage(boolean enable, boolean self) {
        if (enable) {
            if (self) {
                noDamage = 2;
            } else {
                noDamage = 1;
            }
        } else {
            noDamage = 0;
        }
    }

    @Override
    protected boolean onParse(String value) {
        if (value == null) {
            return true; // accepts null value
        }

        String[] args = value.toLowerCase().split("\\|");

        for (String arg : args) {
            arg = arg.trim().toLowerCase();

            if (arg.equals("fire")) {
                setFire(true);
            } else if (arg.equals("fail")) {
                setFailure(true);
            } else if (arg.equals("nobreak")) {
                setNoBreak(true);
            } else if (arg.startsWith("nodamage")) {
                value = arg.substring("nodamage".length()).trim();

                setNoDamage(true, value.equals("self"));
            } else if (arg.startsWith("power")) {
                value = arg.substring("power".length()).trim();

                try {
                    setPower(Float.valueOf(value));
                } catch (NumberFormatException e) {
                    ErrorReporter.warning("Flag " + getType() + " has 'power' argument with invalid number: " + value);
                    continue;
                }
            } else {
                ErrorReporter.warning("Flag " + getType() + " has unknown argument: " + arg);
            }
        }

        return true;
    }

    @Override
    protected void onCrafted(final Args a) {
        new BukkitRunnable() {
            @Override
            public void run() {
                boom(a);
            }
        }.runTaskLater(RecipeManager.getPlugin(), 1);
    }

    private void boom(Args a) {

        if (!a.hasLocation()) {
            a.addCustomReason("Need a location!");
            return;
        }

        if (failure && !a.hasResult()) {
            a.addCustomReason("Needs a result!");
            return;
        }

        boolean failed;
        if (failure) {
            failed = a.result().getType() == Material.AIR;
        } else {
            failed = false;
        }

        if (failure == failed) {
            Map<LivingEntity, Double> entities = new HashMap<LivingEntity, Double>();
            Location loc = a.location();
            World world = loc.getWorld();
            double x = loc.getX() + 0.5;
            double y = loc.getY() + 0.5;
            double z = loc.getZ() + 0.5;

            if (isNoDamageEnabled()) {
                double distanceSquared = power * 2.0;
                distanceSquared *= distanceSquared;

                if (isNoDamageSelf()) {
                    if (a.hasPlayer()) {
                        Player p = a.player();
                        Location l = p.getLocation();

                        if (l.distanceSquared(loc) <= distanceSquared) {
                            entities.put(p, p.getLastDamage());
                            p.setNoDamageTicks(p.getMaximumNoDamageTicks());
                            p.setLastDamage(Integer.MAX_VALUE);
                        }
                    } else {
                        a.addCustomReason("Can't protect crafter, no player!");
                    }
                } else {
                    for (LivingEntity e : world.getLivingEntities()) {
                        if (e.getLocation().distanceSquared(loc) <= distanceSquared) {
                            entities.put(e, e.getLastDamage());
                            e.setNoDamageTicks(e.getMaximumNoDamageTicks());
                            e.setLastDamage(Integer.MAX_VALUE);
                        }
                    }
                }
            }

            world.createExplosion(x, y, z, getPower(), getFire(), !getNoBreak());

            for (Entry<LivingEntity, Double> e : entities.entrySet()) {
                e.getKey().setNoDamageTicks(0);
                e.getKey().setLastDamage(e.getValue());
            }
        }
    }
}
