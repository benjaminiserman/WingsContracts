package dev.biserman.wingscontracts.core.block.state.properties;
import net.minecraft.util.StringRepresentable;

public enum ContractPortalMode implements StringRepresentable {
    UNLIT("UNLIT", 0, "unlit"), 
    LIT("LIT", 1, "lit"),
    COIN("COIN", 2, "coin");
    
    private final String name;

    private ContractPortalMode(final String key, final int i, final String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return this.name;
    }

    public String getSerializedName() {
        return this.name;
    }
}
