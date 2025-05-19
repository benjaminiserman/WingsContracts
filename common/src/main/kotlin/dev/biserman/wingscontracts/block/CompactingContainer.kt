package dev.biserman.wingscontracts.block

import dev.biserman.wingscontracts.server.AvailableContractsData
import dev.biserman.wingscontracts.util.DenominationsHelper
import net.minecraft.world.SimpleContainer
import net.minecraft.world.item.ItemStack

class CompactingContainer(containerSize: Int) : SimpleContainer(containerSize) {
    private val currencyHandler get() = AvailableContractsData.fakeData.currencyHandler

    override fun setChanged() { // avoid calling anything that calls setChanged
        val currencyItems = items
            .filter { it.count <= it.maxStackSize } // to maintain invariant that compacting never increases slots used
            .groupBy { currencyHandler.itemToCurrencyMap[it.item] }

        var i = 0
        for (currencyGroup in currencyItems) {
            val currencyMap = currencyGroup.key
            if (currencyMap == null) {
                continue
            }

            val currencySum = currencyGroup.value.sumOf { it.count * currencyMap[it.item]!! }
            for (entry in DenominationsHelper.denominate(currencySum, currencyMap)) {
                items[i] = ItemStack(entry.first, entry.second)
                i += 1
            }
        }

        // add back in the above-max-stack-size items
        for (otherItem in items.filter {
            !it.isEmpty
                    && (it.count > it.maxStackSize
                    || !currencyHandler.isCurrency(it))
        }) {
            items[i] = otherItem
            i += 1
        }

        super.setChanged()
    }
}