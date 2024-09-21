package online.pigeonshouse.moreinterfaces.handlers;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MIItemStack extends MIData {
    private final MIItem item;
    private final int maxAmount;
    private int amount;
    private List<Object> lore = new ArrayList<>();

    public static MIItemStack create(MIItem item, int maxAmount) {
        return new MIItemStack(item, maxAmount);
    }

    public static MIItemStack create(MIItem item, int amount, int maxAmount) {
        return new MIItemStack(item, amount, maxAmount);
    }

    public MIItemStack(MIItem item, int maxAmount) {
        this.item = item;
        this.maxAmount = maxAmount;
    }

    public MIItemStack(MIItem item, int amount, int maxAmount) {
        this.amount = amount;
        this.maxAmount = maxAmount;
        this.item = item;
    }
}
