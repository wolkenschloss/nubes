package family.haschka.wolkenschloss.cookbook

@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CLASS,
)
@Retention(value = AnnotationRetention.RUNTIME)
annotation class Aggregate()
