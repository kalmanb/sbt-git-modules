import sbt._
object PluginDef extends Build {
   override lazy val projects = Seq(root)
   lazy val root = Project("plugins", file(".")) dependsOn( gitModules )
   lazy val gitModules = uri("file:////home/kalmanb/work/sbt-git-modules/")
}
