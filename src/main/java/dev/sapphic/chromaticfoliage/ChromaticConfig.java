package dev.sapphic.chromaticfoliage;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RequiresMcRestart;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.checkerframework.checker.nullness.qual.Nullable;

@EventBusSubscriber(modid = ChromaticFoliage.ID)
public final class ChromaticConfig {
  private ChromaticConfig() {
  }

  @SubscribeEvent
  public static void changed(final ConfigChangedEvent.OnConfigChangedEvent event) {
    if (ChromaticFoliage.ID.equals(event.getModID())) {
      Client.COLORS.preSync();
      ConfigManager.sync(ChromaticFoliage.ID, Type.INSTANCE);
      Client.COLORS.postSync();
    }
  }

  @Config(modid = ChromaticFoliage.ID, name = ChromaticFoliage.ID + "/client", category = "")
  public static final class Client {
    @Name("blocks")
    public static final Blocks BLOCKS = new Blocks();

    @Name("colors")
    public static final Colors COLORS = new Colors();

    @Name("info")
    public static final Info INFO = new Info();

    private Client() {
    }

    public static final class Blocks {
      @Name("snow_layers")
      @Comment("Tint snow layers when above a chromatic grass block")
      @RequiresMcRestart
      public boolean snowLayers = true;

      @Name("grass_plants")
      @Comment("Tint grass plants when above a chromatic grass block")
      @RequiresMcRestart
      public boolean grassPlants = true;

      private Blocks() {
      }
    }

    public static final class Colors {
      @Name("black")
      public int black = 0x1D1D21;

      @Name("red")
      public int red = 0xB02E26;

      @Name("green")
      public int green = 0x5E7C16;

      @Name("brown")
      public int brown = 0x835432;

      @Name("blue")
      public int blue = 0x3C44AA;

      @Name("purple")
      public int purple = 0x8932B8;

      @Name("cyan")
      public int cyan = 0x169C9C;

      @Name("light_gray")
      public int lightGray = 0x9D9D97;

      @Name("gray")
      public int gray = 0x474F52;

      @Name("pink")
      public int pink = 0xF38BAA;

      @Name("lime")
      public int lime = 0x80C71F;

      @Name("yellow")
      public int yellow = 0xFED83D;

      @Name("light_blue")
      public int lightBlue = 0x3AB3DA;

      @Name("magenta")
      public int magenta = 0xC74EBD;

      @Name("orange")
      public int orange = 0xF9801D;

      @Name("white")
      public int white = 0xF9FFFE;

      private @Nullable IntList lastColors;
      private IntList colors = this.asList();

      private Colors() {
      }

      public void preSync() {
        this.lastColors = this.colors;
      }

      public void postSync() {
        this.colors = this.asList();
        if (this.lastColors != this.colors) {
          FMLCommonHandler.instance().reloadRenderers();
        }
      }

      private IntList asList() {
        return new IntArrayList(new int[] {
          this.black,
          this.red,
          this.green,
          this.brown,
          this.blue,
          this.purple,
          this.cyan,
          this.lightGray,
          this.lightGray,
          this.pink,
          this.lime,
          this.yellow,
          this.lightBlue,
          this.magenta,
          this.orange,
          this.white
        });
      }
    }

    public static final class Info {
      @Name("tooltip_color")
      @Comment("Display the color variant of the chromatic block in the item tooltip")
      public boolean tooltipColor = true;

      @Name("tooltip_illuminated")
      @Comment("Display an additional tooltip for illuminated chromatic blocks")
      public boolean tooltipIlluminated = true;

      @Name("waila_color")
      @Comment({
        "Display the color variant of the chromatic block in Hwyla/Waila",
        "This config value is ignored if Hwyla/Waila is not present"
      })
      public boolean wailaColor = true;

      @Name("waila_illuminated")
      @Comment({
        "Display an additional Hwyla/Waila tooltip for illuminated chromatic blocks",
        "This config value is ignored if Hwyla/Waila is not present"
      })
      public boolean wailaIlluminated = true;

      private Info() {
      }
    }
  }

  @Config(modid = ChromaticFoliage.ID, name = ChromaticFoliage.ID + "/general")
  public static final class General {
    @Name("chroma_recoloring")
    @Comment({
      "Register shapeless recipes that allow recoloring of chromatic blocks",
      "In-world recoloring requires the in-world interaction to be enabled"
    })
    @RequiresMcRestart
    public static boolean recolorRecipes = true;

    @Name("chroma_illumination")
    @Comment("Register shapeless recipes that allow the illumination of chromatic blocks")
    @RequiresMcRestart
    public static boolean illuminationRecipes = true;

    @Name("in_world_interaction")
    @Comment({
      "Allow coloring of blocks when right-clicking them with dyes",
      "Only functions on blocks with a chromatic variant",
      "This config does not control in-world illumination"
    })
    public static boolean inWorldInteraction = true;

    @Name("in_world_illumination")
    @Comment({
      "Allow right-clicking glowstone dust on chromatic blocks to illuminated them",
      "Produces a small amount of light, and allows the block to glow in the dark",
      "Illuminated blocks will drop their glowstone dust when broken"
    })
    public static boolean inWorldIllumination = true;

    @Name("grass_spreads_to_dirt")
    @Comment("Chromatic grass blocks spread onto dirt over time")
    public static boolean grassSpreadsToDirt = true;

    @Name("grass_spreads_to_grass")
    @Comment("Chromatic grass blocks spread onto regular grass blocks over time")
    public static boolean grassSpreadsToGrass = false;

    @Name("leaves_spread_to_leaves")
    @Comment("Chromatic leaves spread onto regular leaves over time")
    public static boolean leavesSpreadToLeaves = false;

    @Name("replaceable_vines")
    @Comment({
      "Allow vines to be replaced by other blocks, including themselves",
      "Set value to true for vanilla behaviour"
    })
    @RequiresMcRestart
    public static boolean replaceableVines = false;

    @Name("list_emissive_blocks")
    @Comment("List each emissive block variant alongside chromatic variants in the creative tab")
    public static boolean listEmissiveBlocks = false;

    private General() {
    }
  }
}
