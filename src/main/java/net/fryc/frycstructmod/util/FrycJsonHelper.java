package net.fryc.frycstructmod.util;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.fryc.frycstructmod.FrycStructMod;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class FrycJsonHelper {

    public static ImmutableSet<Block> getExcludedBlocks(JsonArray jsonArray, Identifier fileId){

        // TODO dodac obsluge tagow (musialbym jakos przechowywac nazwy tagow a potem dopiero z tych nazw brac tagi)
        ImmutableSet.Builder<Block> builder = ImmutableSet.builder();
        for(JsonElement element : jsonArray){
            try{
                Item blockItem = JsonHelper.asItem(element, "blockFromJsonArray");
                if(blockItem instanceof BlockItem block){
                    builder.add(block.getBlock());
                }
            } catch (Exception e) {
                FrycStructMod.LOGGER.error("Error occurred while loading excluded blocks from the following file: " + fileId.toString(), e);
            }
        }

        return builder.build();
    }
}
