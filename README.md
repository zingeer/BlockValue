# BlockValue

In order for the library to work, you need to place the jar file in the plugins folder or add the following code to your
plugin:

```kotlin
class Plugin : JavaPlugin() {
    override fun onEnable() {
        BlockValueManager.enable(this)
    }
    override fun onDisable() {
        BlockValueManager.disable(this)
    }
}
```  

In order for us to be able to save some kind of variable, first we need to register a converter for this type. The
following types are registered by default:

* Int,
* Double,
* String,
* ItemStack

## Example

Consider the date class:

```kotlin
data class ExampleData(
    val name: String,
    val money: Int
)
```

```kotlin
object ExampleConverter : BlockValueConverter<ExampleData> {
    override fun serialize(src: ExampleData): ByteBuf {
        // Create ByteBuf
        val byteBuf = Unpooled.buffer()
        //Encode name
        val stringArray = src.name.encodeToByteArray()
        //Write line in ByteBuf
        byteBuf.writeInt(stringArray.size)
        byteBuf.writeBytes(stringArray)
        //Let's write coins using the usual ByteBuf method.
        byteBuf.writeInt(src.money)
        //Let's return the value
        return byteBuf
    }

    override fun deserialize(byteBuf: ByteBuf): ExampleData {
        //Read the size of the name.
        val size = byteBuf.readInt()
        //Let's make a string from bytes.
        val name = String(ByteBufUtil.getBytes(byteBuf.readBytes(size)), Charset.forName("UTF-8"))
        //Let's read the number of coins
        val money = byteBuf.readInt()

        return ExampleData(
            name,
            money
        )
    }
}
```

Now we need to add our type to the work area, for this call the line:

```kotlin
    BlockValueFactory.registry(ExampleConverter, ExampleData::class.java)
```

In order to fix some value for `org.bukkit.Location`, you need to get the `value` variable from this location, while the
world should not be `null`.