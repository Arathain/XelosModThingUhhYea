package com.xeloklox.dungeons.unleashed;

import com.xeloklox.dungeons.unleashed.blockentity.*;
import com.xeloklox.dungeons.unleashed.blockentity.renderer.*;
import com.xeloklox.dungeons.unleashed.blockentity.screens.*;
import com.xeloklox.dungeons.unleashed.blocks.*;
import com.xeloklox.dungeons.unleashed.gen.*;
import com.xeloklox.dungeons.unleashed.gen.BlockStateBuilder.*;
import com.xeloklox.dungeons.unleashed.gen.ItemJsonModel.*;
import com.xeloklox.dungeons.unleashed.utils.*;
import com.xeloklox.dungeons.unleashed.utils.RegisteredBlock.*;
import com.xeloklox.dungeons.unleashed.utils.lambda.*;
import net.fabricmc.fabric.api.screenhandler.v1.*;
import net.fabricmc.fabric.api.tool.attribute.v1.*;
import net.minecraft.block.*;
import net.minecraft.client.model.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.sound.*;
import net.minecraft.state.property.*;
import net.minecraft.util.math.*;

import static com.xeloklox.dungeons.unleashed.DungeonsUnleashed.MODID;
import static com.xeloklox.dungeons.unleashed.blocks.LeydenJarBlock.MAX_CHARGE;
import static com.xeloklox.dungeons.unleashed.gen.LootTableJson.LootPool.*;
import static com.xeloklox.dungeons.unleashed.gen.LootTableJson.LootPool.LootPoolEntry.LootPoolEntryType.*;

public class ModBlocks{
    public static final RegisteredBlock END_SOIL,ENDERSEED_SOIL,END_GRASS,LEYDEN_JAR,INFUSER,END_WOOD_PLANKS;

    public static final RegisteredBlockEntity<InfuserEntity> INFUSER_ENTITY;
    public static final RegisteredBlockEntityRenderer<InfuserEntity> INFUSER_ENTITY_RENDERER;
    public static final RegisteredScreenHandler<InfuserScreenHandler,InfuserScreen> INFUSER_SCREEN;

    static {
        LootPoolCondition silktouch = (matches_tool(filter->filter.setEnchantments("minecraft:silk_touch , [1,-]")));
        //region BLOCKS
        //A simple inert block
        final String END_SOIL_model = BlockModelPresetBuilder.allSidesSame("end_soil","block/end_soil");
        END_SOIL = new RegisteredBlock("end_soil",
            Globals.bootQuery(() ->
                    new BasicBlock(Material.SOIL, settings ->
                    settings
                    .breakByHand(true)
                    .breakByTool(FabricToolTags.SHOVELS)
                    .sounds(BlockSoundGroup.SOUL_SOIL)
                    .hardness(0.5f)
                    .resistance(0.6f)
                    )
                )
            ,
            BlockStateBuilder.create().noState(randomRotationVariants(END_SOIL_model)),
        block-> { }
        );

        final String END_WOOD_PLANKS_model = BlockModelPresetBuilder.allSidesSame("end_wood_planks","block/end_wood_planks");
        END_WOOD_PLANKS = new RegisteredBlock("end_wood_planks",
                Globals.bootQuery(() ->
                        new BasicBlock(Material.WOOD, settings ->
                                settings
                                        .breakByHand(true)
                                        .breakByTool(FabricToolTags.AXES)
                                        .sounds(BlockSoundGroup.WOOD)
                                        .hardness(2f)
                                        .resistance(3f)
                        )
                )
                ,
                BlockStateBuilder.create().noState(oneVariant(END_WOOD_PLANKS_model)),
                block-> { }
        );

        //---------------------------------------------------------------------
        //A more complex block with 4 variants and a more advanced ore-like loot table.

        final String[] ENDERSEED_SOIL_model = {
            BlockModelPresetBuilder.allSidesSame("enderseed_soil","block/enderseed_soil1"),
            BlockModelPresetBuilder.allSidesSame("enderseed_soil2","block/enderseed_soil2"),
            BlockModelPresetBuilder.allSidesSame("enderseed_soil3","block/enderseed_soil3"),
            BlockModelPresetBuilder.allSidesSame("enderseed_soil4","block/enderseed_soil4")
        };

        ENDERSEED_SOIL = new RegisteredBlock("enderseed_soil",
        Globals.bootQuery(() ->
            new BasicBlock(Material.SOIL, settings ->
                settings
                .breakByHand(true)
                .breakByTool(FabricToolTags.HOES)
                .sounds(BlockSoundGroup.SOUL_SOIL)
                .hardness(0.7f)
                .resistance(0.8f)
                )
            ),BlockStateBuilder.create().noState(
                variants -> variants
                .addModel(model -> model.setModel(ENDERSEED_SOIL_model[0]))
                .addModel(model -> model.setModel(ENDERSEED_SOIL_model[1]))
                .addModel(model -> model.setModel(ENDERSEED_SOIL_model[2]))
                .addModel(model -> model.setModel(ENDERSEED_SOIL_model[3]))
            ),block-> {
                block.setDrops(lootTable ->
                    lootTable.addPool(pool -> //chance of dropping a ender pearl if mined by a hoe
                        pool.addEntry(item, entry ->
                            entry.setOutput("minecraft:ender_pearl")
                                .condition(droprate_with_enchantment(
                                "minecraft:fortune",
                                25 /* <- no enchant % */, 50, 60, 75, 90 // <- max lvl fortune %
                                ))
                        ).condition(survives_explosion())
                        .condition(matches_tool(filter -> filter.setTag("fabric:hoes")))
                        .condition(invert(silktouch))
                    ).addPool(pool -> //always drop the dirt if no silk touch
                            pool.addEntry(item, entry ->
                                entry.setOutput(MODID + ":end_soil")
                            ).condition(survives_explosion())
                            .condition(invert(silktouch))
                    ).addPool(pool -> //otherwise drop the original block
                            pool.addEntry(item, entry ->
                                entry.setOutput(MODID + ":enderseed_soil")
                            ).condition(survives_explosion())
                            .condition(silktouch)
                    )
                );
            }
        );


        //---------------------------------------------------------------------
        //A block with custom behaviour and a different kind of model.

        final String END_GRASS_model = BlockModelPresetBuilder.TopBottomSide("end_grass","block/end_grass_top","block/end_grass_side","block/end_soil");
        END_GRASS = new RegisteredBlock("end_grass",
            Globals.bootQuery(() -> new ModGrassBlock(Material.SOLID_ORGANIC, END_SOIL.get(), 2,
                settings ->
                settings.breakByHand(true)
                .breakByTool(FabricToolTags.SHOVELS)
                .sounds(BlockSoundGroup.SOUL_SOIL)
                .hardness(0.6f)
                .resistance(0.6f)
                .ticksRandomly())
            ),
            BlockStateBuilder.create().noState(randomRotationVariants(END_GRASS_model)),
            block -> {
                block.setDrops(lootTable ->
                    lootTable.addPool(pool ->
                        pool.addEntry(item, entry ->
                            entry.setOutput(MODID + ":end_soil")
                                 .condition(invert(silktouch))
                        ).addEntry(item, entry ->
                            entry.setOutput(MODID + ":end_grass")
                                 .condition(silktouch)
                        )
                    )
                );
            }
        );

        //---------------------------------------------------------------------
        //A multipart complex block with custom behaviour and 5 states, where items persist block state

        //generate the block state models and the block state
        final String LEYDEN_JAR_model = "custom/leyden_jar";
        final String[] LEYDEN_JAR_ROD_model = new String[5];
        BlockStateBuilder LEYDEN_JAR_STATE = BlockStateBuilder.createMultipart() // <--multipart !!!!
                                        .addPart(model->model.setModel(v->v.setModel(LEYDEN_JAR_model)));

        for(int i =0;i<LEYDEN_JAR_ROD_model.length;i++){
            int finalI = i;
            LEYDEN_JAR_ROD_model[i] =  BlockModelPresetBuilder.customTemplate("block/custom/leyden_jar_rod","leyden_jar_rod"+i,"block/custom/copper_rod"+i);
            //have the rod change based on the state...
            LEYDEN_JAR_STATE.addPart(model->
                model.setModel(v->v.setModel(LEYDEN_JAR_ROD_model[finalI]))
                     .addConditions(cond->cond.set(LeydenJarBlock.CHARGE,finalI))
            );
        }

        //create the block
        LEYDEN_JAR = new RegisteredBlock("leyden_jar",
        Globals.bootQuery(() -> new LeydenJarBlock(Material.GLASS,
            settings ->
                settings.breakByHand(true)
                .breakByTool(FabricToolTags.PICKAXES)
                .sounds(BlockSoundGroup.GLASS)
                .hardness(0.3f)
                .resistance(0.3f)
                .nonOpaque())
            ),
            LEYDEN_JAR_STATE,
            block -> {
                block.setRenderlayer(RenderLayerOptions.CUTOUT);

                block.setBlockitem((id, bitem) ->
                    new RegisteredItem(id, bitem,
                        model -> {
                            model.setModelParent(ModelParent.ITEM_GENERATED).setTextureLayers("item/leyden_jar");
                            for(int i = 1; i <= MAX_CHARGE; i++){
                                int finalI = i;
                                model.addOverride(override -> override.setModel("item/leyden_jar" + (finalI + 1)).addModelPredicate("charge", finalI / (float)MAX_CHARGE));
                            }
                            return model;
                        },
                        item -> item.addPredicate("charge", (itemstack, world, entity, seed) -> {
                            NbtCompound blockEntityTag = itemstack.getOrCreateSubNbt("BlockStateTag");
                            if(blockEntityTag == null){
                                return 0;
                            }
                            return Strings.parseInt(blockEntityTag.getString(LeydenJarBlock.CHARGE.getName())) / (float)MAX_CHARGE;
                        })
                    )
                );

                block.setSettings(ModItems.getSettings((s) -> s.group(ItemGroup.BUILDING_BLOCKS).maxCount(8)));

                block.setDrops(lootTable ->
                    lootTable.addPool(pool ->
                        pool.addEntry(item, entry ->
                            entry.setOutput(MODID + ":leyden_jar")
                                 .addFunction(copy_block_state(MODID + ":leyden_jar", LeydenJarBlock.CHARGE))
                        )
                    )
                );
            }
        );

        //---------------------------------------------------------------------
        //A multipart complex block with custom behaviour and 32 different states, with a inventory and block entity
        final String INFUSER_model = BlockModelPresetBuilder.customTemplate("block/custom/infuser", "infuser", "block/custom/infuser");

        INFUSER = new RegisteredBlock("infuser",
            Globals.bootQuery(() -> new InfuserBlock(Material.STONE,
                settings ->
                settings.breakByHand(false)
                .breakByTool(FabricToolTags.PICKAXES)
                .sounds(BlockSoundGroup.STONE)
                .hardness(1.5f)
                .resistance(7f)
            )),
            BlockStateBuilder.create()
                .addStateVariant(HORIZONTAL_FACING(), Direction.NORTH,variant->variant.addModel(INFUSER_model,0))
                .addStateVariant(HORIZONTAL_FACING(), Direction.EAST,variant->variant.addModel(INFUSER_model,90))
                .addStateVariant(HORIZONTAL_FACING(), Direction.SOUTH,variant->variant.addModel(INFUSER_model,180))
                .addStateVariant(HORIZONTAL_FACING(), Direction.WEST,variant->variant.addModel(INFUSER_model,270))
            ,
            block -> {
                block.setSettings(ModItems.getSettings((s) -> s.group(ItemGroup.DECORATIONS)));
                block.setDrops(lootTable ->
                    lootTable.addPool(pool ->
                    pool.addEntry(item, entry ->
                    entry.setOutput(MODID + ":infuser")
                    )
                ));
            }
        );

        //end region
        //region ENTITIES
        INFUSER_ENTITY = new RegisteredBlockEntity<InfuserEntity>("infuser_entity",InfuserEntity::new,INFUSER);
        INFUSER_ENTITY_RENDERER = new RegisteredBlockEntityRenderer<>(()->INFUSER_ENTITY.get(), InfuserRenderer::new);
        INFUSER_SCREEN =
                Globals.bootQuery( ()->
                                new RegisteredScreenHandler<>(
                                        "infuserscreen",
                                        ScreenHandlerRegistry.registerSimple(INFUSER.getIdentifier(), InfuserScreenHandler::new),
                                        InfuserScreen::new
                                ),
                        null
                );
         Globals.bootRun(()->{try{Class.forName(InfuserRenderer.class.getName());}catch(ClassNotFoundException ignored){}});
        //end region

        new JsonModelProvider(); // yes

    }

    static DirectionProperty HORIZONTAL_FACING(){
        return Globals.bootQuery(()->Properties.HORIZONTAL_FACING,  DirectionProperty.of("facing"));
    }

    static Func<ModelVariantList, ModelVariantList> randomRotationVariants(String modelStr){
        return
        //rotated randomly when placed like dirt
        variants->variants
        .addModel(model->model.setModel(modelStr).setY(0))
        .addModel(model->model.setModel(modelStr).setY(90))
        .addModel(model->model.setModel(modelStr).setY(180))
        .addModel(model->model.setModel(modelStr).setY(270));
    }
    static Func<ModelVariantList, ModelVariantList> oneVariant(String modelStr){
        return
        //rotated randomly when placed like dirt
        variants->variants
        .addModel(model->model.setModel(modelStr));
    }


    public static TexturedModelData test() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild("cube", ModelPartBuilder.create().uv(0, 0).cuboid(-6F, 12F, -6F, 12F, 12F, 12F), ModelTransform.pivot(0F, 0F, 0F));
        return TexturedModelData.of(modelData, 64, 64);
    }
}
