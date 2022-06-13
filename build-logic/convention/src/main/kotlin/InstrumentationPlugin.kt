import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS
import com.android.build.api.instrumentation.InstrumentationParameters
import com.android.build.api.instrumentation.InstrumentationScope.ALL
import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.commons.ClassRemapper
import org.objectweb.asm.commons.SimpleRemapper

class InstrumentationPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.withPlugin("com.android.application") {
            val androidComponentsExtension =
                target.extensions.getByType(AndroidComponentsExtension::class.java)
            androidComponentsExtension.onVariants {
                it.instrumentation.transformClassesWith(
                    InstrumentationVisitorFactory::class.java,
                    ALL
                ) { params ->
                    params.invalidate.set(
                        System.currentTimeMillis()
                    ) // to always run artifact transforms
                }
                it.instrumentation.setAsmFramesComputationMode(
                    COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS
                )
            }
        }
    }
}

abstract class InstrumentationVisitorFactory :
    AsmClassVisitorFactory<InstrumentationVisitorFactory.Params> {

    interface Params : InstrumentationParameters {
        @get:Input
        @get:Optional
        val invalidate: Property<Long>
    }

    override fun isInstrumentable(classData: ClassData): Boolean = true

    override fun createClassVisitor(
        classContext: ClassContext,
        nextClassVisitor: ClassVisitor
    ): ClassVisitor =
        ClassRemapper(nextClassVisitor, SimpleRemapper("", ""))
}
