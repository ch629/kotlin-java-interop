import com.ch629.kotlin_builder.BuilderProcessor
import org.joor.CompileOptions
import org.joor.Reflect
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class BuilderProcessorTests : Spek({
  describe("a builder processor") {
    context("") {
      it("") {
        // TODO: Check if this works with Kotlin, otherwise I need a different library or to write something like this from scratch
        //https://blog.jooq.org/2018/12/07/how-to-unit-test-your-annotation-processor-using-joor/
        val refl = Reflect.compile(
          "", """"
                                        
                """.trimIndent(),
          CompileOptions().processors(BuilderProcessor())
        ).create()


      }
    }
  }
})
